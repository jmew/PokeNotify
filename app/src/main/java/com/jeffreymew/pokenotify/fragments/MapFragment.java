package com.jeffreymew.pokenotify.fragments;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.activities.LoginActivity;
import com.jeffreymew.pokenotify.activities.MainActivity;
import com.jeffreymew.pokenotify.utils.Utils;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;


/**
 * Created by pivotal on 2016-07-23.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final int REFRESH_DURATION_IN_MS = 30000; //30 seconds

    private PokemonGo mPokemonGo;
    @Nullable
    private CatchablePokemon mPokemon;

    private GoogleMap mMap;

    public static MapFragment newInstance(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo, @Nullable CatchablePokemon pokemon) {
        Bundle args = new Bundle();
        args.putSerializable(LoginActivity.Extras.AUTH_INFO, authInfo);

        if (pokemon != null) {
            Gson gson = new Gson();
            String pokemonStringObject = gson.toJson(pokemon);
            args.putString(Extras.POKEMON, pokemonStringObject);
        }

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getArguments().get(LoginActivity.Extras.AUTH_INFO);

        String pokemonStringObject = getArguments().getString(Extras.POKEMON);

        if (pokemonStringObject != null) {
            Gson gson = new Gson();
            mPokemon = gson.fromJson(pokemonStringObject, CatchablePokemon.class);
        }

        setupPokemon(authInfo);

        setupLocationListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mPokemon != null) {
            LatLng pokemonLocation = new LatLng(mPokemon.getLatitude(), mPokemon.getLongitude());
            mMap.addMarker(new MarkerOptions().position(pokemonLocation).title("Pokemon Here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pokemonLocation, 14.0f));

        } else {
            LatLng defaultLocation = new LatLng(0, 0);
            //TODO get last location
            mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getIntent();
    }

    private void setupPokemon(final RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        Observable<PokemonGo> observable = Observable.defer(new Func0<Observable<PokemonGo>>() {
            @Override
            public Observable<PokemonGo> call() {
                return Observable.just(new PokemonGo(authInfo, new OkHttpClient()));
            }
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PokemonGo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to fetch account profile", Snackbar.LENGTH_LONG);
                    }

                    @Override
                    public void onNext(PokemonGo pokemonGo) {
                        mPokemonGo = pokemonGo;
                    }
                });
    }

    private void updateLocation(double latitude, double longitude) {
        mPokemonGo.setLocation(latitude, longitude, 0);
    }

    private void notifyPokemonNearby(CatchablePokemon pokemon) {
        NotificationCompat.Builder builder = Utils.createNotification(getContext(), pokemon);

        Intent resultIntent = new Intent(getContext(), MapFragment.class);

        Gson gson = new Gson();
        String pokemonStringObject = gson.toJson(pokemon);

        resultIntent.putExtra(Extras.POKEMON, pokemonStringObject);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) pokemon.getEncounterId(), builder.build());
    }

    private void getPokemonNearby() {
        Observable<List<CatchablePokemon>> observable = Observable.defer(new Func0<Observable<List<CatchablePokemon>>>() {
            @Override
            public Observable<List<CatchablePokemon>> call() {
                try {
                    return Observable.just(mPokemonGo.getMap().getCatchablePokemon());
                } catch (LoginFailedException | RemoteServerException e) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to fetch nearby Pokemon", Snackbar.LENGTH_LONG);
                }
                return Observable.empty();
            }
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<CatchablePokemon>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to fetch nearby Pokemon", Snackbar.LENGTH_LONG);
                    }

                    @Override
                    public void onNext(List<CatchablePokemon> catchablePokemons) {
                        //TODO to list operator?
                        for (CatchablePokemon pokemon : catchablePokemons) {
                            notifyPokemonNearby(pokemon);
                        }
                    }
                });
    }

    private void setupLocationListeners() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mPokemonGo != null) {
                    updateLocation(location.getLatitude(), location.getLongitude());
                    getPokemonNearby();
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
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REFRESH_DURATION_IN_MS, 0, locationListener);
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

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

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
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
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
