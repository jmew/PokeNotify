package com.jeffreymew.pokenotify.activities;

import com.jeffreymew.pokenotify.models.BasicPokemon;

import rx.Observable;

/**
 * Created by mew on 2016-07-26.
 */
public interface NotificationActivity {
    Observable<BasicPokemon> getNotificationObservable();
}
