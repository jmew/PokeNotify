package com.jeffreymew.pokenotify.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.jeffreymew.pokenotify.R;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by mew on 2016-07-22.
 */
public class Utils {

    public static NotificationCompat.Builder createNotification(Context context, CatchablePokemon pokemon) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(pokemon.getPokemonId().getValueDescriptor().getName());
        stringBuilder.append(" for ");
        stringBuilder.append(String.format(Locale.getDefault(), "%d min and %d sec",
                TimeUnit.MILLISECONDS.toMinutes(pokemon.getExpirationTimestampMs()),
                TimeUnit.MILLISECONDS.toSeconds(pokemon.getExpirationTimestampMs()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(pokemon.getExpirationTimestampMs()))
        ));
        stringBuilder.append("\n");

        PendingIntent launchPokemonGoIntent = PendingIntent.getActivity(context, 0, new Intent("com.nianticlabs.pokemongo"), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Pokemon Nearby!")
                .setContentText(pokemon.getPokemonId().getValueDescriptor().getName())
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(stringBuilder))
                .setVibrate(new long[] { 500, 500, 500 })
                .setLights(Color.RED, 3000, 3000)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_pokemon_go_logo, "Launch Pokemon Go", launchPokemonGoIntent);

        return builder;
    }
}
