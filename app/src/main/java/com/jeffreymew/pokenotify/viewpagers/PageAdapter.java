package com.jeffreymew.pokenotify.viewpagers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.fragments.SettingsFragment;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;

/**
 * Created by pivotal on 2016-07-23.
 */
public class PageAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    @Nullable
    private CatchablePokemon mPokemon;
    private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo mAuthInfo;

    public PageAdapter(FragmentManager fm, int numOfTabs, RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo, @Nullable CatchablePokemon pokemon) {
        super(fm);
        mNumOfTabs = numOfTabs;
        mAuthInfo = authInfo;
        mPokemon = pokemon;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MapFragment.newInstance(mAuthInfo, mPokemon);
            case 1:
                return new SettingsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
