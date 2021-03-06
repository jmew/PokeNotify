package com.jeffreymew.pokenotify.utils;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.activities.MainActivity;
import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.models.BasicPokemon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mew on 2016-07-22.
 */
public class Utils {

    public static int STATIC_MAP_WIDTH = 500;
    public static int STATIC_MAP_HEIGHT = 375;

    public static void createNotification(final Context context, final BasicPokemon pokemon, final Location currentLocation) {
        String STATIC_MAPS_URL = "http://maps.google.com/maps/api/staticmap?center=" + pokemon.getLatitude() + "," + pokemon.getLongitude() + "&zoom=17&scale=2&size=" + STATIC_MAP_WIDTH + "x" +
                STATIC_MAP_HEIGHT + "&maptype=roadmap&markers=color:red%7C" + pokemon.getLatitude() + "," + pokemon.getLongitude() + "&key=" + context.getString(R.string.google_maps_key);


        Glide.with(context.getApplicationContext()).load(STATIC_MAPS_URL).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                createNotification(context, pokemon, currentLocation, bitmap);
            }
        });
    }

    public static Dialog showErrorDialog(final Context context, String title, String message, boolean shouldShowServerStatus, DialogInterface.OnClickListener retryListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", retryListener)
                .setNegativeButton("Cancel", null);

        if (shouldShowServerStatus) {
            builder.setNeutralButton("Server Status", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://ispokemongodownornot.com"));
                    context.startActivity(intent);
                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static Dialog showGenericDialog(final Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message);

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    public static boolean isPokemonGoInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.nianticlabs.pokemongo", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private static void createNotification(final Context context, final BasicPokemon pokemon, final Location currentLocation, final Bitmap staticMap) {
        Location pokemonLocation = new Location(LocationManager.NETWORK_PROVIDER);
        pokemonLocation.setLatitude(pokemon.getLatitude());
        pokemonLocation.setLongitude(pokemon.getLongitude());

        Date date = new Date(pokemon.getExpirationTimestampMs());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(pokemon.getName() + " Nearby!")
                .setContentText(Math.round(currentLocation.distanceTo(pokemonLocation)) + " meters away. Until " + simpleDateFormat.format(date))
                .setTicker(pokemon.getName() + " Nearby!")
                .setSmallIcon(pokemon.getPokemonImage())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), pokemon.getPokemonImage()))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(staticMap))
                .setVibrate(new long[]{500, 500, 500})
                .setLights(Color.CYAN, 2000, 2000)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true);

        if (isPokemonGoInstalled(context)) {
            PendingIntent launchPokemonGoIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage("com.nianticlabs.pokemongo"), 0);
            builder.addAction(R.drawable.ic_pokemon_go_logo, "Launch Pokemon Go", launchPokemonGoIntent);
        }

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(MapFragment.Extras.POKEMON, pokemon);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, (int) pokemon.getEncounterId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) pokemon.getEncounterId(), builder.build());

        // Clear notification after pokemon despawns
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                notificationManager.cancel((int) pokemon.getEncounterId());
            }
        }, pokemon.getMillisUntilDespawn());
    }
}
