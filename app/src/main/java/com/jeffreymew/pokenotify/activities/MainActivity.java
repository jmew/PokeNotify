package com.jeffreymew.pokenotify.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.google.gson.Gson;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.viewpagers.PageAdapter;
import com.pokegoapi.api.map.Pokemon.CatchablePokemon;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getIntent().getSerializableExtra(LoginActivity.Extras.AUTH_INFO);

        CatchablePokemon pokemon = null;
        String pokemonStringObject = getIntent().getStringExtra(MapFragment.Extras.POKEMON);
        if (pokemonStringObject != null) {
            Gson gson = new Gson();
            pokemon = gson.fromJson(pokemonStringObject, CatchablePokemon.class);
        }

        mTabLayout.addTab(mTabLayout.newTab().setText("Map"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Settings"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PageAdapter(getSupportFragmentManager(), mTabLayout.getTabCount(), authInfo, pokemon);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}
