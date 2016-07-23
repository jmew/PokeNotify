package com.jeffreymew.pokenotify.activities;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jeffreymew.pokenotify.fragments.LoginFragment;
import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.ResultCodes;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.app.FragmentManager fm = getFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, new LoginFragment());
        ft.commit();
    }

    public void onFragmentResult(int resultCode, Bundle bundle) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if (resultCode == ResultCodes.SUCCESS) {
            RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) bundle.get(LoginFragment.Extras.AUTH_INFO);
            ft.replace(R.id.container, MapFragment.newInstance(authInfo)).commit();
        }
    }
}
