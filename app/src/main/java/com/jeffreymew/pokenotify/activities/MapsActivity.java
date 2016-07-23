package com.jeffreymew.pokenotify.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jeffreymew.pokenotify.R;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int REFRESH_DURATION_IN_MS = 30000; //30 seconds

    private PokemonGo mPokemonGo;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getIntent().getSerializableExtra(LoginActivity.Extras.AUTH_INFO);

        setupPokemon(authInfo);

        setupLocationListeners();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng defaultLocation = new LatLng(0, 0);
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
    }

    @Override
    public void onPause() {
        super.onPause();
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
                        Snackbar.make(findViewById(android.R.id.content), "Failed to fetch account profile", Snackbar.LENGTH_LONG);
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

    private void notifyPokemonNearby(List<CatchablePokemon> pokemon) {
        NotificationCompat.Builder builder = Utils.createNotification(this, pokemon);

        Intent resultIntent = new Intent(this, LoginActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    private void getPokemonNearby() {
        Observable<List<CatchablePokemon>> observable = Observable.defer(new Func0<Observable<List<CatchablePokemon>>>() {
            @Override
            public Observable<List<CatchablePokemon>> call() {
                try {
                    return Observable.just(mPokemonGo.getMap().getCatchablePokemon());
                } catch (LoginFailedException | RemoteServerException e) {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to fetch nearby Pokemon", Snackbar.LENGTH_LONG);
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
                        Snackbar.make(findViewById(android.R.id.content), "Failed to fetch nearby Pokemon", Snackbar.LENGTH_LONG);
                    }

                    @Override
                    public void onNext(List<CatchablePokemon> catchablePokemons) {
                        //TODO to list operator?
                        notifyPokemonNearby(catchablePokemons);
                    }
                });
    }

    private void setupLocationListeners() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
                //showEnableLocationDialog();
            }
        };

        // Check if location permissions are enabled
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //showEnableLocationDialog();
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REFRESH_DURATION_IN_MS, 0, locationListener);
    }

//    private void showEnableLocationDialog() {
//        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//        dialog.setTitle("Enable Location")
//                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to use this app")
//                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(myIntent);
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                    }
//                });
//        dialog.show();
//    }
}
