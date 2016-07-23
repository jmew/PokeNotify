package com.jeffreymew.pokenotify.fragments;

import android.Manifest;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.activities.MainActivity;
import com.jeffreymew.pokenotify.utils.Utils;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.Map;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import java.util.List;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MapFragment extends Fragment {

    private final int REFRESH_DURATION_IN_MS = 500;

    private PokemonGo mPokemonGo;

    public static MapFragment newInstance(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        Bundle args = new Bundle();
        args.putSerializable(LoginFragment.Extras.AUTH_INFO, authInfo);

        MapFragment fragment = new MapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getArguments().get(LoginFragment.Extras.AUTH_INFO);

        setupPokemon(authInfo);

        setupLocation();
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

    private void notifyPokemonNearby(List<CatchablePokemon> pokemon) {
        NotificationCompat.Builder builder = Utils.createNotification(getActivity(), pokemon);

        Intent resultIntent = new Intent(getActivity(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        getActivity(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
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
                        getActivity().findViewById(R.id.test).setVisibility(View.VISIBLE);
                        ((TextView) getActivity().findViewById(R.id.test)).setText("GPS Ready");
                        notifyPokemonNearby(catchablePokemons);
                    }
                });
    }

    private void setupLocation() {
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
                //showEnableLocationDialog();
            }
        };

        // Check if location permissions are enabled
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //showEnableLocationDialog();
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, REFRESH_DURATION_IN_MS, 0, locationListener);
    }

//    private void showEnableLocationDialog() {
//        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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