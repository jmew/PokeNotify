package com.jeffreymew.pokenotify.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;

import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import java.util.List;

/**
 * Created by mew on 2016-07-22.
 */
public class Utils {

    public static NotificationCompat.Builder createNotification(Context context, List<CatchablePokemon> pokemonList) {
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("List of Pokemon Nearby");

        StringBuilder stringBuilder = new StringBuilder();
        for (CatchablePokemon pokemon : pokemonList) {
            stringBuilder.append(pokemon.getPokemonId().getValueDescriptor().getName());
            stringBuilder.append(" for ");
            stringBuilder.append(pokemon.getExpirationTimestampMs() / 1000); // Convert to seconds
            stringBuilder.append(" more seconds!");
            inboxStyle.addLine(stringBuilder);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Pokemon Nearby!")
                .setContentText(pokemonList.get(0).getPokemonId().getValueDescriptor().getName())
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setStyle(inboxStyle)
                .setVibrate(new long[] { 500, 500, 500 })
                .setLights(Color.RED, 3000, 3000);
                //TODO set notification ringtone
                //builder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"))
        return builder;
    }
}
