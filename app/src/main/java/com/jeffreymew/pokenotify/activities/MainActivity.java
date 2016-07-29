package com.jeffreymew.pokenotify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.fragments.MapFragment;
import com.jeffreymew.pokenotify.models.BasicPokemon;
import com.jeffreymew.pokenotify.viewpagers.PageAdapter;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.ReplaySubject;

public class MainActivity extends FragmentActivity implements NotificationActivity {

    private ReplaySubject<BasicPokemon> mOnNewIntentSubject = ReplaySubject.create();

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = (RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo) getIntent().getSerializableExtra(LoginActivity.Extras.AUTH_INFO);

        mTabLayout.addTab(mTabLayout.newTab().setText("Map"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Settings"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PagerAdapter adapter = new PageAdapter(getSupportFragmentManager(), mTabLayout.getTabCount(), authInfo);
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTabLayout.getTabAt(0).select();

        BasicPokemon pokemon = (BasicPokemon) intent.getSerializableExtra(MapFragment.Extras.POKEMON);
        mOnNewIntentSubject.onNext(pokemon);
    }

    @Override
    public Observable<BasicPokemon> getNotificationObservable() {
        return mOnNewIntentSubject.asObservable();
    }
}
