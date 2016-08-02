package com.jeffreymew.pokenotify.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.utils.Utils;
import com.pokegoapi.auth.PtcLogin;
import com.pokegoapi.exceptions.LoginFailedException;

import java.util.concurrent.TimeUnit;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.sign_up_link)
    TextView mSignUpLink;

    @BindView(R.id.loading_spinner)
    CircularProgressView mLoadingSpinner;

    @BindView(R.id.loading_spinner_widget)
    View mLoadingSpinnerWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        checkIfCredentialsCached();
    }

    @OnClick(R.id.sign_up_link)
    public void onSignUpLinkClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://club.pokemon.com/us/pokemon-trainer-club/sign-up/"));
        startActivity(intent);
    }

    @OnClick(R.id.login_button)
    public void onLoginClicked() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        Utils.hideSoftKeyboard(mPassword);

        if (!isLoginValid(username, password)) {
            return;
        }

        showLoadingSpinner(true, false);

        login(username, password);
    }

    @OnEditorAction(R.id.password)
    public boolean onNextKeypress(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onLoginClicked();
            return true;
        }
        return false;
    }

    private boolean isLoginValid(String username, String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_login_required_field));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setError(getString(R.string.error_login_required_field));
            valid = false;
        }

        return valid;
    }

    private void login(final String username, final String password) {
        Observable.defer(new Func0<Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>>() {
            @Override
            public Observable<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo> call() {
                OkHttpClient httpClient = new OkHttpClient();
                PtcLogin ptcLogin = new PtcLogin(httpClient);

                try {
                    return Observable.just(ptcLogin.login(username, password));
                } catch (LoginFailedException e) {
                    return Observable.error(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(8, TimeUnit.SECONDS) //TODO do i need?
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    int MAX_RETRIES = 2;
                    int retryCount = 0;

                    @Override
                    public Observable<?> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Object, Observable<?>>() {
                            @Override
                            public Observable<?> call(Object o) {
                                if (++retryCount < MAX_RETRIES) {
                                    return Observable.timer(2, TimeUnit.SECONDS); //2 second delay
                                }
                                return Observable.error((Throwable) o);
                            }
                        });
                    }
                }) //TODO move to compose
                .subscribe(new Subscriber<RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingSpinner(false, false);
                        showLoginErrorDialog();
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        showLoadingSpinner(false, true);
                        storeUsernamePasswordInSharedPrefs(username, password);
                        launchMapActivity(authInfo);
                    }
                });
    }

    private void showLoadingSpinner(boolean show, boolean success) {
        if (show) {
            mLoadingSpinnerWidget.setVisibility(View.VISIBLE);
            mLoadingSpinner.startAnimation();
        } else {
            mLoadingSpinner.stopAnimation();
            if (!success) {
                mLoadingSpinnerWidget.setVisibility(View.GONE);
            }
        }
    }

    private void launchMapActivity(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Extras.AUTH_INFO, authInfo);
        startActivity(intent);
        finish();
    }

    private void showLoginErrorDialog() {
        Utils.showErrorDialog(this, "Login Failed", "Sorry login failed. Maybe the PokemonGo servers are down?", true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onLoginClicked();
            }
        });
    }

    private void storeUsernamePasswordInSharedPrefs(final String username, final String password) {
        SharedPreferences prefs = getSharedPreferences(Extras.LOGIN_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Extras.USERNAME, username);
        editor.putString(Extras.PASSWORD, password);
        editor.putLong(Extras.TIMESTAMP, System.currentTimeMillis());
        editor.apply();
    }

    private void checkIfCredentialsCached() {
        SharedPreferences prefs = getSharedPreferences(Extras.LOGIN_PREFS, Context.MODE_PRIVATE);
        long cachedTime = System.currentTimeMillis() - prefs.getLong(Extras.TIMESTAMP, 90);

        // Cache credentials for 60 minutes
        if (TimeUnit.MILLISECONDS.toMinutes(cachedTime) < 60) {
            mUsername.setText(prefs.getString(Extras.USERNAME, ""));
            mPassword.setText(prefs.getString(Extras.PASSWORD, ""));
            onLoginClicked();
        } else {
            prefs.edit().clear().apply();
        }
    }

    public class Extras {
        public static final String AUTH_INFO = "auth_info";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String TIMESTAMP = "timestamp";
        public static final String LOGIN_PREFS = "login_preferences";
    }
}
