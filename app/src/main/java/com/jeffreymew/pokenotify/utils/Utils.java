package com.jeffreymew.pokenotify.utils;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.activities.MainActivity;
import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.models.BasicPokemon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by mew on 2016-07-22.
 */
public class Utils {

    public static int STATIC_MAP_WIDTH = 500;
    public static int STATIC_MAP_HEIGHT = 375;

    public static void createNotification(final Context context, final BasicPokemon pokemon, final Location currentLocation) {
        String STATIC_MAPS_URL = "http://maps.google.com/maps/api/staticmap?center=" + pokemon.getLatitude() + "," + pokemon.getLongitude() + "&zoom=17&scale=2&size=" + STATIC_MAP_WIDTH + "x" +
                STATIC_MAP_HEIGHT + "&maptype=roadmap&markers=color:red%7C" + pokemon.getLatitude() + "," + pokemon.getLongitude() + "&key=" + context.getString(R.string.google_maps_key);

        Glide.with(context).load(STATIC_MAPS_URL).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                createNotification(context, pokemon, currentLocation, resource.getCurrent());
            }
        });
    }

    public static Dialog showErrorDialog(final Context context, String title, String message, boolean shouldShowServerStatus, DialogInterface.OnClickListener retryListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Retry", retryListener);
        builder.setNegativeButton("Cancel", null);

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

    private static void createNotification(final Context context, final BasicPokemon pokemon, final Location currentLocation, final Drawable map) {
        Location pokemonLocation = new Location(LocationManager.NETWORK_PROVIDER);
        pokemonLocation.setLatitude(pokemon.getLatitude());
        pokemonLocation.setLongitude(pokemon.getLongitude());

        Date date = new Date(pokemon.getExpirationTimestampMs());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

//        final RemoteViews customMapNotificationView = new RemoteViews(context.getPackageName(), R.layout.map_notification);
//        customMapNotificationView.setImageViewResource(R.id.map_image, R.mipmap.ic_launcher);
//        customMapNotificationView.setImageViewResource(R.id.button_icon, R.drawable.ic_pokemon_go_logo);
//        customMapNotificationView.setTextViewText(R.id.button_text, "Launch Pokemon Go");
//        customMapNotificationView.setTextColor(R.id.button_text, context.getResources().getColor(android.R.color.black));

        PendingIntent launchPokemonGoIntent = PendingIntent.getActivity(context, 0, context.getPackageManager().getLaunchIntentForPackage("com.nianticlabs.pokemongo"), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(pokemon.getName() + " Nearby!")
                .setContentText(Math.round(currentLocation.distanceTo(pokemonLocation)) + " meters away. Until " + simpleDateFormat.format(date))
                //.setCustomBigContentView(customMapNotificationView)
                .setTicker(pokemon.getName() + " Nearby!")
                .setSmallIcon(pokemon.getPokemonImage())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), pokemon.getPokemonImage()))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(((GlideBitmapDrawable) map).getBitmap()))
                .setVibrate(new long[]{500, 500, 500})
                .setLights(Color.YELLOW, 2000, 2000)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_pokemon_go_logo, "Launch Pokemon Go", launchPokemonGoIntent);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra(MapFragment.Extras.POKEMON, pokemon);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, (int) pokemon.getEncounterId(), resultIntent, PendingIntent.FLAG_UPDATE_CURRENT); //TODO check if long to int conversion fails
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
        }, pokemon.getMillisUntilDespawn()); //TODO redo with RX

        // Load static map image into notification
//        NotificationTarget notificationTarget = new NotificationTarget(context, customMapNotificationView, R.id.map_image, notification, (int) Math.abs(pokemon.getEncounterId()));
//        Glide.with(context.getApplicationContext()) // safer!
//                .load(STATIC_MAPS_URL)
//                .asBitmap()
//                .into(notificationTarget);
    }
}
