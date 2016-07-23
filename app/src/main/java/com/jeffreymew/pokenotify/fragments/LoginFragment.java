package com.jeffreymew.pokenotify.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jeffreymew.pokenotify.R;
import com.jeffreymew.pokenotify.ResultCodes;
import com.jeffreymew.pokenotify.activities.MainActivity;
import com.pokegoapi.auth.GoogleLogin;
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

/**
 * Created by Mew on 2016-07-21.
 */
public class LoginFragment extends Fragment {

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.loading_spinner)
    CircularProgressView mLoadingSpinner;

    @BindView(R.id.loading_spinner_widget)
    View mLoadingSpinnerWidget;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
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
                    Log.e("Pokemans", e.getLocalizedMessage().toString());
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Login Failed", Snackbar.LENGTH_LONG).show();
                    showLoadingSpinner(false);
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
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Login Failed", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo) {
                        showLoadingSpinner(false);

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(Extras.AUTH_INFO, authInfo);

                        ((MainActivity) getActivity()).onFragmentResult(ResultCodes.SUCCESS, bundle);
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

    public class Extras {
        public static final String AUTH_INFO = "auth_info";
    }
}
