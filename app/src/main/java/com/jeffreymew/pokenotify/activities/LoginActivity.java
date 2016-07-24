package com.jeffreymew.pokenotify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jeffreymew.pokenotify.R;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.loading_spinner)
    CircularProgressView mLoadingSpinner;

    @BindView(R.id.loading_spinner_widget)
    View mLoadingSpinnerWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login_button)
    public void onLoginClicked() {
//        String username = mUsername.getText().toString();
//        String password = mPassword.getText().toString();

        final String username = "xtremeqa";
        final String password = "Xtreme234";

        Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> loginObservable = Observable.defer(new Func0<Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>>() {
            @Override
            public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> call() {
                OkHttpClient httpClient = new OkHttpClient();
                PTCLogin ptcLogin = new PTCLogin(httpClient);

                try {
                    return Observable.just(ptcLogin.login(username, password));
                } catch (LoginFailedException e) {
                    showLoadingSpinner(false);
                    Log.e("PokeNotify", e.getLocalizedMessage());
                    Snackbar.make(findViewById(android.R.id.content), "Login Failed", Snackbar.LENGTH_LONG).show();
                    return Observable.empty();
                }
            }
        });

        showLoadingSpinner(true);

        loginObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingSpinner(false);
                        Snackbar.make(findViewById(android.R.id.content), "Login Failed", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        showLoadingSpinner(false);
                        launchMapActivity(authInfo);
                    }
                });
    }

    private void showLoadingSpinner(boolean show) {
        if (show) {
            mLoadingSpinnerWidget.setVisibility(View.VISIBLE);
            mLoadingSpinner.startAnimation();
        } else {
            mLoadingSpinner.stopAnimation();
            mLoadingSpinnerWidget.setVisibility(View.GONE);
        }
    }

    private void launchMapActivity(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.AUTH_INFO, authInfo);
        startActivity(intent);
        finish();
    }

    public class Extras {
        public static final String AUTH_INFO = "auth_info";
    }
}
