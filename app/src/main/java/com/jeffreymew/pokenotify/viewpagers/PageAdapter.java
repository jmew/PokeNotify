package com.jeffreymew.pokenotify.viewpagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.fragments.SettingsFragment;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;

/**
 * Created by mew on 2016-07-23.
 */
public class PageAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo mAuthInfo;

    public PageAdapter(FragmentManager fm, int numOfTabs, RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        super(fm);
        mNumOfTabs = numOfTabs;
        mAuthInfo = authInfo;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return MapFragment.newInstance(mAuthInfo);
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
