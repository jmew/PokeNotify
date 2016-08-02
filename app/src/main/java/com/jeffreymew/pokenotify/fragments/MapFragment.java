package com.jeffreymew.pokenotify.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.activities.LoginActivity;
import com.jeffreymew.pokenotify.activities.NotificationActivity;
import com.jeffreymew.pokenotify.models.BasicPokemon;
import com.jeffreymew.pokenotify.models.NotificationDB;
import com.jeffreymew.pokenotify.utils.Utils;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by mew on 2016-07-23.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final int REFRESH_DURATION_IN_MS = 30000; //30 seconds
    private final float DEFAULT_ZOOM_LEVEL = 17.5f;
    private final float MARKER_ZOOM_LEVEL = 18.5f;
    @BindView(R.id.redo_search_button)
    Button mRedoSearchButton;
    @BindView(R.id.loading_spinner)
    CircularProgressView mLoadingSpinner;
    @BindView(R.id.loading_spinner_widget)
    View mLoadingSpinnerWidget;
    private PokemonGo mPokemonClient;
    private GoogleMap mMap;
    private Snackbar mFindingGPSSignalSnackbar;
    private Subscription mNotificationSubscriber;
    private Dialog mLatestDialog;
    private NotificationDB mNotificationDB;
    private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo mAuthInfo;
    private Location mPreviousCenterOfMap;
    private LocationManager mLocationManager;
    private Map<Long, Marker> mMarkers = new HashMap<>();
    private Realm mRealm;

    public static MapFragment newInstance(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        Bundle args = new Bundle();
        args.putSerializable(LoginActivity.Extras.AUTH_INFO, authInfo);

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        ButterKnife.bind(this, view);

        mAuthInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getArguments().get(LoginActivity.Extras.AUTH_INFO);
        mFindingGPSSignalSnackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.gps_loading, Snackbar.LENGTH_INDEFINITE);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getActivity()).build();
        Realm.deleteRealm(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);

        mRealm = Realm.getDefaultInstance();

        setupPokemon();
        setupLocationListeners();
        setupNotificationDB();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Observable<BasicPokemon> subject = ((NotificationActivity) getActivity()).getNotificationObservable();
        mNotificationSubscriber = subject.subscribe(new Action1<BasicPokemon>() {
            @Override
            public void call(BasicPokemon pokemon) {
                if (pokemon != null && mMap != null) {
                    LatLng pokemonLocation = new LatLng(pokemon.getLatitude(), pokemon.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pokemonLocation, MARKER_ZOOM_LEVEL));
                }
            }
        });
    }

    @Override
    public void onPause() {
        mNotificationSubscriber.unsubscribe();
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showEnableLocationDialog();
            return;
        }

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //TODO remove buttons at bottom

        Criteria criteria = new Criteria();
        if (mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false)) != null) {
            Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));
            LatLng lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, DEFAULT_ZOOM_LEVEL));
        } else {
            LatLng defaultLocation = new LatLng(0, 0);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            public void onInfoWindowClick(Marker marker) {
                if (Utils.isPokemonGoInstalled(getActivity())) {
                    Intent launchIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.nianticlabs.pokemongo");
                    if (launchIntent != null) {
                        startActivity(launchIntent);
                    }
                }
            }
        });

        mFindingGPSSignalSnackbar.show();
    }

    @OnClick(R.id.redo_search_button)
    public void onRedoSearchButtonClicked() {
        mRedoSearchButton.setVisibility(View.GONE);
        showLoadingSpinner(true);

        Location newCenterOfMap = new Location(LocationManager.NETWORK_PROVIDER);
        newCenterOfMap.setLatitude(getCenterOfMapLatitude());
        newCenterOfMap.setLongitude(getCenterOfMapLongitude());

        mPreviousCenterOfMap = newCenterOfMap;

        updateMapWithPokemon(newCenterOfMap, false);
    }

    private void setupPokemon() {
        Observable.defer(new Func0<Observable<PokemonGo>>() {
            @Override
            public Observable<PokemonGo> call() {
                try {
                    return Observable.just(new PokemonGo(mAuthInfo, new OkHttpClient()));
                } catch (LoginFailedException | RemoteServerException e) {
                    return Observable.error(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.timeout(10, TimeUnit.SECONDS) //TODO what does this do?
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    int MAX_RETRIES = 3;
                    int retryCount = 0;

                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Object, Observable<?>>() {
                            @Override
                            public Observable<?> call(Object o) {
                                if (++retryCount < MAX_RETRIES) {
                                    return Observable.timer(5, TimeUnit.SECONDS); //5 second delay
                                }
                                return Observable.error((Throwable) o);
                            }
                        });
                    }
                }) //TODO move to compose
                .subscribe(new Subscriber<PokemonGo>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        showNetworkErrorDialog(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                setupPokemon();
                            }
                        });
                    }

                    @Override
                    public void onNext(PokemonGo pokemonGo) {
                        mPokemonClient = pokemonGo;

                        if (mPreviousCenterOfMap != null) {
                            updateMapWithPokemon(mPreviousCenterOfMap, false);
                        }
                    }
                });
    }

    private void updateLocation(double latitude, double longitude) {
        mPokemonClient.setLocation(latitude, longitude, 0);
    }

    private void notifyPokemonNearby(BasicPokemon pokemon) {
        mNotificationDB.deleteExpiredNotifications();

        if (!mNotificationDB.isNotificationShownBefore(pokemon.getEncounterId())) {
            updateNotificationDB(pokemon.getEncounterId(), pokemon.getMillisUntilDespawn());

            Location currentLocation = new Location(LocationManager.NETWORK_PROVIDER);
            currentLocation.setLatitude(mPokemonClient.getLatitude());
            currentLocation.setLongitude(mPokemonClient.getLongitude());

            Utils.createNotification(getContext(), pokemon, currentLocation);
        }
    }

    private void fetchNearbyPokemon(final boolean shouldShowNotification) {
//        List<LatLng> latlngMap = createLatLngMap();
//
//        for (LatLng latlng : latlngMap) {
//            mPokemonClient.setLatitude(latlng.latitude);
//            mPokemonClient.setLongitude(latlng.longitude);
        Observable.defer(new Func0<Observable<CatchablePokemon>>() {
            @Override
            public Observable<CatchablePokemon> call() {
                try {
                    return Observable.from(mPokemonClient.getMap().getCatchablePokemon());
                } catch (LoginFailedException | RemoteServerException e) {
                    return Observable.error(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(3, TimeUnit.SECONDS)
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    int MAX_RETRIES = 3;
                    int retryCount = 0;

                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Object, Observable<?>>() {
                            @Override
                            public Observable<?> call(Object o) {
                                if (++retryCount < MAX_RETRIES) {
                                    return Observable.timer(1, TimeUnit.SECONDS); //1 second delay
                                }
                                return Observable.error((Throwable) o);
                            }
                        });
                    }
                }) //TODO move to compose
                .subscribe(new Subscriber<CatchablePokemon>() {
                    @Override
                    public void onCompleted() {
                        showLoadingSpinner(false);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!arePokemonNearby() && !isDialogShowing()) {
                                    mLatestDialog = Utils.showGenericDialog(getActivity(), "No Pokemon Nearby", "Maybe try looking somewhere else?");
                                }
                            }
                        }, 1000); //delay for map animation
                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingSpinner(false);
                        showNetworkErrorDialog(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                fetchNearbyPokemon(shouldShowNotification);
                            }
                        });
                    }

                    @Override
                    public void onNext(CatchablePokemon catchablePokemon) {
                        BasicPokemon pokemon = new BasicPokemon(catchablePokemon);

                        if (shouldShowNotification && checkIfNotificationForPokemonIsEnabled(catchablePokemon.getPokemonId().getValueDescriptor().getName())) {
                            notifyPokemonNearby(pokemon);
                        }

                        if (mRealm.where(BasicPokemon.class).equalTo("mEncounterId", pokemon.getEncounterId()).count() < 1) {
                            mRealm.beginTransaction();
                            mRealm.copyToRealm(pokemon);
                            mRealm.commitTransaction();
                            mMarkers.put(pokemon.getEncounterId(), addMapMarker(pokemon));
                        }
                    }
                });
        //}
    }

    private void setupLocationListeners() {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mFindingGPSSignalSnackbar.isShown()) {
                    mFindingGPSSignalSnackbar.dismiss();
                    showLoadingSpinner(true);
                    //final LatLng currentLocation = new LatLng(34.008344620842834, -118.49789142608643); //TODO remove hardcode values
                    final LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude()); //Zoom to location for the first time

                    Observable.create(new Observable.OnSubscribe<Void>() {
                        @Override
                        public void call(Subscriber<? super Void> subscriber) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM_LEVEL));
                            subscriber.onNext(null);
                        }
                    }).subscribe(new Action1<Void>() {
                        @Override
                        public void call(Void aVoid) {
                            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                                @Override
                                public void onCameraChange(CameraPosition cameraPosition) {
                                    checkIfMapScrolledTooFar();
                                }
                            });
                        }
                    });

                    mPreviousCenterOfMap = location;
                }

                if (mPokemonClient != null) {
//                    // TODO: remove hardcode values
//                    location.setLatitude(34.008344620842834);
//                    location.setLongitude(-118.49789142608643);

                    updateMapWithPokemon(location, true);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                showEnableLocationDialog();
            }
        };

        // Check if location permissions are enabled
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showEnableLocationDialog();
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REFRESH_DURATION_IN_MS, 0, locationListener);
    }

    private void showEnableLocationDialog() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(getActivity(), 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private boolean checkIfNotificationForPokemonIsEnabled(String pokemonName) {
        SharedPreferences prefs = getActivity().getSharedPreferences(SettingsFragment.Extras.SETTINGS_PREF, Context.MODE_PRIVATE);
        return prefs.getBoolean(pokemonName, true);
    }

    private Marker addMapMarker(BasicPokemon pokemon) {
        if (mMap != null) {
            return mMap.addMarker(createMarker(pokemon));
        }
        return null;
    }

    private void updateMapWithPokemon(Location location, boolean shouldShowNotification) {
        updateLocation(location.getLatitude(), location.getLongitude());
        clearExpiredPokemon(); //Update markers
        updateMarkers();
        fetchNearbyPokemon(shouldShowNotification);
    }

    private MarkerOptions createMarker(BasicPokemon pokemon) {
        LatLng pokemonLocation = new LatLng(pokemon.getLatitude(), pokemon.getLongitude());
        Bitmap pokemonBitmap = BitmapFactory.decodeResource(getResources(), pokemon.getPokemonImage());
        return new MarkerOptions()
                .position(pokemonLocation)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(pokemonBitmap, (int) (pokemonBitmap.getWidth() * 0.8), (int) (pokemonBitmap.getHeight() * 0.8), false)))
                .title(pokemon.getName())
                .snippet(String.format(Locale.getDefault(), "Despawns in %d min and %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(pokemon.getMillisUntilDespawn()),
                        TimeUnit.MILLISECONDS.toSeconds(pokemon.getMillisUntilDespawn()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(pokemon.getMillisUntilDespawn()))));
    }

    private void showNetworkErrorDialog(DialogInterface.OnClickListener retryListener) {
        if (!isDialogShowing()) {
            mLatestDialog = Utils.showErrorDialog(getContext(), "Network Error", "Sorry something went wrong with the network. Maybe the PokemonGo servers are down?", true, retryListener);
        }
    }

    private boolean isDialogShowing() {
        if (mLatestDialog != null) {
            return mLatestDialog.isShowing();
        }
        return false;
    }

    private void setupNotificationDB() {
        mNotificationDB = new NotificationDB(getContext());
    }

    private void updateNotificationDB(long encounterId, long expiryTimeStamp) {
        mNotificationDB.insertNotification(encounterId, expiryTimeStamp);
    }

    private void checkIfMapScrolledTooFar() {
        Location newCenterOfMap = new Location(LocationManager.NETWORK_PROVIDER);
        newCenterOfMap.setLatitude(mMap.getCameraPosition().target.latitude);
        newCenterOfMap.setLongitude(mMap.getCameraPosition().target.longitude);

        if (mPreviousCenterOfMap.distanceTo(newCenterOfMap) > 100) { // 1km
            mRedoSearchButton.setVisibility(View.VISIBLE);
        }
    }

    private double getCenterOfMapLatitude() {
        return mMap.getCameraPosition().target.latitude;
    }

    private double getCenterOfMapLongitude() {
        return mMap.getCameraPosition().target.longitude;
    }

    private List<LatLng> createLatLngMap() {
        List<LatLng> list = new ArrayList<>();

        list.add(getTranslatedLatLng(70, -70)); //NW quadrant
        list.add(getTranslatedLatLng(70, 0)); //N quadrant
        list.add(getTranslatedLatLng(70, 70)); //NE quandrant
        list.add(getTranslatedLatLng(0, -70)); //W quadrant
        list.add(new LatLng(mPokemonClient.getLatitude(), mPokemonClient.getLongitude())); //Center
        list.add(getTranslatedLatLng(0, 70)); //E quadrant
        list.add(getTranslatedLatLng(-70, -70)); //SW quadrant
        list.add(getTranslatedLatLng(-70, 0)); //S quadrant
        list.add(getTranslatedLatLng(-70, 70)); //SE quadrant

        return list;
    }

    private LatLng getTranslatedLatLng(double distanceNorth, double distanceEast) {
        //Earth’s radius, sphere
        double radius = 6378137;

        //Coordinate offsets in radians
        double offsetLat = distanceNorth / radius;
        double offsetLng = distanceEast / (radius * Math.cos(Math.PI * mPokemonClient.getLatitude() / 180));

        //OffsetPosition, decimal degrees
        double newLat = mPokemonClient.getLatitude() + offsetLat * 180 / Math.PI;
        double newLng = mPokemonClient.getLongitude() + offsetLng * 180 / Math.PI;

        return new LatLng(newLat, newLng);
    }

    private void clearExpiredPokemon() {
        final RealmResults<BasicPokemon> expiredPokemon = mRealm.where(BasicPokemon.class).lessThan("mExpirationTimestampMs", System.currentTimeMillis()).findAll();

        for (BasicPokemon pokemon : expiredPokemon) {
            mMarkers.get(pokemon.getEncounterId()).remove();
            mMarkers.remove(pokemon.getEncounterId());
        }

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                expiredPokemon.deleteAllFromRealm();
            }
        });
    }

    private void updateMarkers() {
        for (Map.Entry<Long, Marker> marker : mMarkers.entrySet()) {
            marker.getValue().remove();
            mMarkers.put(marker.getKey(), mMap.addMarker(createMarker(mRealm.where(BasicPokemon.class).equalTo("mEncounterId", marker.getKey()).findAll().get(0))));
        }
    }

    private boolean arePokemonNearby() {
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        double minLat = bounds.southwest.latitude;
        double minLng = bounds.southwest.longitude;
        double maxLat = bounds.northeast.latitude;
        double maxLng = bounds.northeast.longitude;

        return mRealm.where(BasicPokemon.class).between("mLatitude", minLat, maxLat).between("mLongitude", minLng, maxLng).count() > 0;
    }

    private void showLoadingSpinner(boolean show) {
        if (show) {
            mLoadingSpinnerWidget.setVisibility(View.VISIBLE);
            mLoadingSpinner.startAnimation();
        } else {
            mLoadingSpinner.stopAnimation();
            mLoadingSpinnerWidget.setVisibility(View.GONE);
        }
    }

    // Google Callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public class Extras {
        public static final String POKEMON = "pokemon";
    }
}
