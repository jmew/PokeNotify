package com.jeffreymew.pokenotify;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.auth.GoogleLogin;
import com.pokegoapi.auth.PTCLogin;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.RemoteServerException;

import POGOProtos.Networking.Envelopes.RequestEnvelopeOuterClass;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;

/**
 * Created by Mew on 2016-07-21.
 */
public class LoginFragment extends Fragment {

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.password)
    EditText mPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.login_button)
    public void onLoginClicked() {
        OkHttpClient httpClient = new OkHttpClient();
        PTCLogin pokemonTrainerClubLogin = new PTCLogin(httpClient);

//        String username = mUsername.getText().toString();
//        String password = mPassword.getText().toString();

        String username = "xtremeqa";
        String password = "Xtreme234";

        RequestEnvelopeOuterClass.RequestEnvelope.AuthInfo authInfo = null;
        try {
            authInfo = pokemonTrainerClubLogin.login(username, password);
        } catch (LoginFailedException e) {
            Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_SHORT).show();

        PokemonGo go = new PokemonGo(authInfo, httpClient);

        go.setLocation(43.610836, -79.551253, 0);

        try {
            Log.e("Pokemon" ,go.getMap().getNearbyPokemon().get(0).getPokemonId().getDescriptorForType().getFullName());
        } catch (RemoteServerException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            e.printStackTrace();
        }
    }
}
