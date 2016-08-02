package com.jeffreymew.pokenotify.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jeffreymew.pokenotify.R;

import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mew on 2016-07-23.
 */
public class SettingsFragment extends Fragment {

    @BindView(R.id.settings_list)
    ListView mSettingsList;

    private SharedPreferences mPrefs;
    private HashMap<String, Boolean> mPokemonMap;
    private SparseArray<String> checkmarkToPokemonName;
    private boolean[] checkedPokemon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.settings_list_item, Arrays.asList("Pokemon Notification Preferences"));
        mSettingsList.setAdapter(adapter);
        mSettingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0:
                        onPokemonSelectorClicked();
                        break;
//                    case 1:
//                        onGPSSettingClicked();
//                        break;
                }
            }
        });

        setupSharedPrefs();

        return view;
    }

    private void setupSharedPrefs() {
        String[] pokemonNames = getActivity().getResources().getStringArray(R.array.pokemon_list);
        checkedPokemon = new boolean[getResources().getStringArray(R.array.pokemon_list).length];
        mPokemonMap = new HashMap<>(); //Maps whether a pokemon (string) is checked or not

        mPrefs = getActivity().getSharedPreferences(Extras.SETTINGS_PREF, Context.MODE_PRIVATE);
        for (int i = 0; i < pokemonNames.length; i++) { //
            checkedPokemon[i] = mPrefs.getBoolean(pokemonNames[i].toUpperCase(), true);
            mPokemonMap.put(pokemonNames[i].toUpperCase(), checkedPokemon[i]); //Since pokemon name are all upper case in object
        }

        // Create mapping between index on list to pokemon name
        checkmarkToPokemonName = new SparseArray<>();
        for (int i = 0; i < pokemonNames.length; i++) {
            checkmarkToPokemonName.put(i, pokemonNames[i].toUpperCase());
        }
    }

    private void onPokemonSelectorClicked() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose which pokemon to recieve notifications for")
                .setMultiChoiceItems(R.array.pokemon_list, checkedPokemon, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index, boolean isChecked) {
                        checkedPokemon[index] = isChecked;
                        mPokemonMap.put(checkmarkToPokemonName.get(index), isChecked);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateSharedPrefs();
                    }
                })
                .setNegativeButton("Add All", null)
                .setNeutralButton("Clear All", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int index = 0; index < checkedPokemon.length; index++) {
                    checkedPokemon[index] = true;
                    dialog.getListView().setItemChecked(index, true);
                    mPokemonMap.put(checkmarkToPokemonName.get(index), true);
                }
                updateSharedPrefs();
            }

        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int index = 0; index < checkedPokemon.length; index++) {
                    checkedPokemon[index] = false;
                    dialog.getListView().setItemChecked(index, false);
                    mPokemonMap.put(checkmarkToPokemonName.get(index), false);
                }
                updateSharedPrefs();
            }
        });
    }

//    private void onGPSSettingClicked() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        final AlertDialog dialog = builder.setView(R.layout.number_picker)
//                .setTitle("Choose the refresh interval for GPS and fetching new Pokemon")
//                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                    }
//                })
//                .setNeutralButton("Cancel", null)
//                .create();
//
//        dialog.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int refreshInterval = ((NumberPicker) view).getValue();
//                SharedPreferences.Editor editor = mPrefs.edit();
//                editor.putInt("RefreshInterval", refreshInterval);
//                editor.apply();
//            }
//        }); //TODO check if this works
//
//        dialog.show();
//
//        NumberPicker numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);
//        numberPicker.setMaxValue(90);
//        numberPicker.setValue(mPrefs.getInt("RefreshInterval", 30));
//        numberPicker.setMinValue(15);
//    }

    private void updateSharedPrefs() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.clear();

        for (String pokemonName : mPokemonMap.keySet()) {
            editor.putBoolean(pokemonName, mPokemonMap.get(pokemonName));
        }

        editor.apply();
    }

    public class Extras {
        public static final String SETTINGS_PREF = "settings_preferences";
    }
}
