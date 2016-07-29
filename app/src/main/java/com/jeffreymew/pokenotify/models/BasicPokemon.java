package com.jeffreymew.pokenotify.models;

import android.support.annotation.DrawableRes;

import com.jeffreymew.pokenotify.R;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;

import java.io.Serializable;

/**
 * Created by mew on 2016-07-24.
 */
public class BasicPokemon implements Serializable {

    private final String mSpawnpointId;
    private final String mName;
    private final int mPokedexId;
    private final long mEncounterId;
    private final long mExpirationTimestampMs;
    private final double mLatitude;
    private final double mLongitude;
    private boolean mEncountered;
    private @DrawableRes int mPokemonImage;

    public BasicPokemon(CatchablePokemon catchablePokemon) {
        mSpawnpointId = catchablePokemon.getSpawnPointId();
        String nameAllCaps = catchablePokemon.getPokemonId().getValueDescriptor().getName();
        mName = nameAllCaps.substring(0, 1) + nameAllCaps.substring(1).toLowerCase();
        mPokedexId = catchablePokemon.getPokemonId().getNumber();
        mEncounterId = catchablePokemon.getEncounterId();
        mExpirationTimestampMs = catchablePokemon.getExpirationTimestampMs();
        mLatitude = catchablePokemon.getLatitude();
        mLongitude = catchablePokemon.getLongitude();
        mEncountered = catchablePokemon.isEncountered();

        setPokemonImage();
    }

    public String getSpawnpointId() {
        return mSpawnpointId;
    }

    public String getName() {
        if (mName.equals("Nidoran_male")) return "Nidoran(M)";
        if (mName.equals("Nidoran_female")) return "Nidoran(F)";
        return mName;
    }

    public int getPokedexId() {
        return mPokedexId;
    }

    public long getEncounterId() {
        return mEncounterId;
    }

    public long getMillisUntilDespawn() {
        return mExpirationTimestampMs - System.currentTimeMillis();
    }

    public long getExpirationTimestampMs() {
        return mExpirationTimestampMs;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public boolean isEncountered() {
        return mEncountered;
    }

    public @DrawableRes int getPokemonImage() {
        return mPokemonImage;
    }

    private void setPokemonImage() {
        switch (mPokedexId) {
            case 1:
                mPokemonImage = R.drawable.pokemon_bulbasaur;
                break;
            case 2:
                mPokemonImage = R.drawable.pokemon_ivysaur;
                break;
            case 3:
                mPokemonImage = R.drawable.pokemon_venusaur;
                break;
            case 4:
                mPokemonImage = R.drawable.pokemon_charmander;
                break;
            case 5:
                mPokemonImage = R.drawable.pokemon_charmeleon;
                break;
            case 6:
                mPokemonImage = R.drawable.pokemon_charizard;
                break;
            case 7:
                mPokemonImage = R.drawable.pokemon_squirtle;
                break;
            case 8:
                mPokemonImage = R.drawable.pokemon_wartortle;
                break;
            case 9:
                mPokemonImage = R.drawable.pokemon_blastoise;
                break;
            case 10:
                mPokemonImage = R.drawable.pokemon_caterpie;
                break;
            case 11:
                mPokemonImage = R.drawable.pokemon_metapod;
                break;
            case 12:
                mPokemonImage = R.drawable.pokemon_butterfree;
                break;
            case 13:
                mPokemonImage = R.drawable.pokemon_weedle;
                break;
            case 14:
                mPokemonImage = R.drawable.pokemon_kakuna;
                break;
            case 15:
                mPokemonImage = R.drawable.pokemon_beedrill;
                break;
            case 16:
                mPokemonImage = R.drawable.pokemon_pidgey;
                break;
            case 17:
                mPokemonImage = R.drawable.pokemon_pidgeotto;
                break;
            case 18:
                mPokemonImage = R.drawable.pokemon_pidgeot;
                break;
            case 19:
                mPokemonImage = R.drawable.pokemon_rattata;
                break;
            case 20:
                mPokemonImage = R.drawable.pokemon_raticate;
                break;
            case 21:
                mPokemonImage = R.drawable.pokemon_spearow;
                break;
            case 22:
                mPokemonImage = R.drawable.pokemon_fearow;
                break;
            case 23:
                mPokemonImage = R.drawable.pokemon_ekans;
                break;
            case 24:
                mPokemonImage = R.drawable.pokemon_arbok;
                break;
            case 25:
                mPokemonImage = R.drawable.pokemon_pikachu;
                break;
            case 26:
                mPokemonImage = R.drawable.pokemon_raichu;
                break;
            case 27:
                mPokemonImage = R.drawable.pokemon_sandshrew;
                break;
            case 28:
                mPokemonImage = R.drawable.pokemon_sandslash;
                break;
            case 29:
                mPokemonImage = R.drawable.pokemon_nidoran_f;
                break;
            case 30:
                mPokemonImage = R.drawable.pokemon_nidorina;
                break;
            case 31:
                mPokemonImage = R.drawable.pokemon_nidoqueen;
                break;
            case 32:
                mPokemonImage = R.drawable.pokemon_nidoran_m;
                break;
            case 33:
                mPokemonImage = R.drawable.pokemon_nidorino;
                break;
            case 34:
                mPokemonImage = R.drawable.pokemon_nidoking;
                break;
            case 35:
                mPokemonImage = R.drawable.pokemon_clefairy;
                break;
            case 36:
                mPokemonImage = R.drawable.pokemon_clefable;
                break;
            case 37:
                mPokemonImage = R.drawable.pokemon_vulpix;
                break;
            case 38:
                mPokemonImage = R.drawable.pokemon_ninetales;
                break;
            case 39:
                mPokemonImage = R.drawable.pokemon_jigglypuff;
                break;
            case 40:
                mPokemonImage = R.drawable.pokemon_wigglytuff;
                break;
            case 41:
                mPokemonImage = R.drawable.pokemon_zubat;
                break;
            case 42:
                mPokemonImage = R.drawable.pokemon_golbat;
                break;
            case 43:
                mPokemonImage = R.drawable.pokemon_oddish;
                break;
            case 44:
                mPokemonImage = R.drawable.pokemon_gloom;
                break;
            case 45:
                mPokemonImage = R.drawable.pokemon_vileplume;
                break;
            case 46:
                mPokemonImage = R.drawable.pokemon_paras;
                break;
            case 47:
                mPokemonImage = R.drawable.pokemon_parasect;
                break;
            case 48:
                mPokemonImage = R.drawable.pokemon_venonat;
                break;
            case 49:
                mPokemonImage = R.drawable.pokemon_venomoth;
                break;
            case 50:
                mPokemonImage = R.drawable.pokemon_diglett;
                break;
            case 51:
                mPokemonImage = R.drawable.pokemon_dugtrio;
                break;
            case 52:
                mPokemonImage = R.drawable.pokemon_meowth;
                break;
            case 53:
                mPokemonImage = R.drawable.pokemon_persian;
                break;
            case 54:
                mPokemonImage = R.drawable.pokemon_psyduck;
                break;
            case 55:
                mPokemonImage = R.drawable.pokemon_golduck;
                break;
            case 56:
                mPokemonImage = R.drawable.pokemon_mankey;
                break;
            case 57:
                mPokemonImage = R.drawable.pokemon_primeape;
                break;
            case 58:
                mPokemonImage = R.drawable.pokemon_growlithe;
                break;
            case 59:
                mPokemonImage = R.drawable.pokemon_arcanine;
                break;
            case 60:
                mPokemonImage = R.drawable.pokemon_poliwag;
                break;
            case 61:
                mPokemonImage = R.drawable.pokemon_poliwhirl;
                break;
            case 62:
                mPokemonImage = R.drawable.pokemon_poliwrath;
                break;
            case 63:
                mPokemonImage = R.drawable.pokemon_abra;
                break;
            case 64:
                mPokemonImage = R.drawable.pokemon_kadabra;
                break;
            case 65:
                mPokemonImage = R.drawable.pokemon_alakazam;
                break;
            case 66:
                mPokemonImage = R.drawable.pokemon_machop;
                break;
            case 67:
                mPokemonImage = R.drawable.pokemon_machoke;
                break;
            case 68:
                mPokemonImage = R.drawable.pokemon_machamp;
                break;
            case 69:
                mPokemonImage = R.drawable.pokemon_bellsprout;
                break;
            case 70:
                mPokemonImage = R.drawable.pokemon_weepinbell;
                break;
            case 71:
                mPokemonImage = R.drawable.pokemon_victreebel;
                break;
            case 72:
                mPokemonImage = R.drawable.pokemon_tentacool;
                break;
            case 73:
                mPokemonImage = R.drawable.pokemon_tentacruel;
                break;
            case 74:
                mPokemonImage = R.drawable.pokemon_geodude;
                break;
            case 75:
                mPokemonImage = R.drawable.pokemon_graveler;
                break;
            case 76:
                mPokemonImage = R.drawable.pokemon_golem;
                break;
            case 77:
                mPokemonImage = R.drawable.pokemon_ponyta;
                break;
            case 78:
                mPokemonImage = R.drawable.pokemon_rapidash;
                break;
            case 79:
                mPokemonImage = R.drawable.pokemon_slowpoke;
                break;
            case 80:
                mPokemonImage = R.drawable.pokemon_slowbro;
                break;
            case 81:
                mPokemonImage = R.drawable.pokemon_magnemite;
                break;
            case 82:
                mPokemonImage = R.drawable.pokemon_magneton;
                break;
            case 83:
                mPokemonImage = R.drawable.pokemon_farfetchd;
                break;
            case 84:
                mPokemonImage = R.drawable.pokemon_doduo;
                break;
            case 85:
                mPokemonImage = R.drawable.pokemon_dodrio;
                break;
            case 86:
                mPokemonImage = R.drawable.pokemon_seel;
                break;
            case 87:
                mPokemonImage = R.drawable.pokemon_dewgong;
                break;
            case 88:
                mPokemonImage = R.drawable.pokemon_grimer;
                break;
            case 89:
                mPokemonImage = R.drawable.pokemon_muk;
                break;
            case 90:
                mPokemonImage = R.drawable.pokemon_shellder;
                break;
            case 91:
                mPokemonImage = R.drawable.pokemon_cloyster;
                break;
            case 92:
                mPokemonImage = R.drawable.pokemon_gastly;
                break;
            case 93:
                mPokemonImage = R.drawable.pokemon_haunter;
                break;
            case 94:
                mPokemonImage = R.drawable.pokemon_gengar;
                break;
            case 95:
                mPokemonImage = R.drawable.pokemon_onix;
                break;
            case 96:
                mPokemonImage = R.drawable.pokemon_drowzee;
                break;
            case 97:
                mPokemonImage = R.drawable.pokemon_hypno;
                break;
            case 98:
                mPokemonImage = R.drawable.pokemon_krabby;
                break;
            case 99:
                mPokemonImage = R.drawable.pokemon_kingler;
                break;
            case 100:
                mPokemonImage = R.drawable.pokemon_voltorb;
                break;
            case 101:
                mPokemonImage = R.drawable.pokemon_electrode;
                break;
            case 102:
                mPokemonImage = R.drawable.pokemon_exeggcute;
                break;
            case 103:
                mPokemonImage = R.drawable.pokemon_exeggutor;
                break;
            case 104:
                mPokemonImage = R.drawable.pokemon_cubone;
                break;
            case 105:
                mPokemonImage = R.drawable.pokemon_marowak;
                break;
            case 106:
                mPokemonImage = R.drawable.pokemon_hitmonlee;
                break;
            case 107:
                mPokemonImage = R.drawable.pokemon_hitmonchan;
                break;
            case 108:
                mPokemonImage = R.drawable.pokemon_lickitung;
                break;
            case 109:
                mPokemonImage = R.drawable.pokemon_koffing;
                break;
            case 110:
                mPokemonImage = R.drawable.pokemon_weezing;
                break;
            case 111:
                mPokemonImage = R.drawable.pokemon_rhyhorn;
                break;
            case 112:
                mPokemonImage = R.drawable.pokemon_rhydon;
                break;
            case 113:
                mPokemonImage = R.drawable.pokemon_chansey;
                break;
            case 114:
                mPokemonImage = R.drawable.pokemon_tanglea;
                break;
            case 115:
                mPokemonImage = R.drawable.pokemon_kangaskhan;
                break;
            case 116:
                mPokemonImage = R.drawable.pokemon_horsea;
                break;
            case 117:
                mPokemonImage = R.drawable.pokemon_seadra;
                break;
            case 118:
                mPokemonImage = R.drawable.pokemon_goldeen;
                break;
            case 119:
                mPokemonImage = R.drawable.pokemon_seaking;
                break;
            case 120:
                mPokemonImage = R.drawable.pokemon_staryu;
                break;
            case 121:
                mPokemonImage = R.drawable.pokemon_starmie;
                break;
            case 122:
                mPokemonImage = R.drawable.pokemon_mrmime;
                break;
            case 123:
                mPokemonImage = R.drawable.pokemon_scyther;
                break;
            case 124:
                mPokemonImage = R.drawable.pokemon_jynx;
                break;
            case 125:
                mPokemonImage = R.drawable.pokemon_electabuzz;
                break;
            case 126:
                mPokemonImage = R.drawable.pokemon_magmar;
                break;
            case 127:
                mPokemonImage = R.drawable.pokemon_pinsir;
                break;
            case 128:
                mPokemonImage = R.drawable.pokemon_tauros;
                break;
            case 129:
                mPokemonImage = R.drawable.pokemon_magikarp;
                break;
            case 130:
                mPokemonImage = R.drawable.pokemon_gyarados;
                break;
            case 131:
                mPokemonImage = R.drawable.pokemon_lapras;
                break;
            case 132:
                mPokemonImage = R.drawable.pokemon_ditto;
                break;
            case 133:
                mPokemonImage = R.drawable.pokemon_eevee;
                break;
            case 134:
                mPokemonImage = R.drawable.pokemon_vaporeon;
                break;
            case 135:
                mPokemonImage = R.drawable.pokemon_jolteon;
                break;
            case 136:
                mPokemonImage = R.drawable.pokemon_flareon;
                break;
            case 137:
                mPokemonImage = R.drawable.pokemon_porygon;
                break;
            case 138:
                mPokemonImage = R.drawable.pokemon_omanyte;
                break;
            case 139:
                mPokemonImage = R.drawable.pokemon_omastar;
                break;
            case 140:
                mPokemonImage = R.drawable.pokemon_kabuto;
                break;
            case 141:
                mPokemonImage = R.drawable.pokemon_kabutops;
                break;
            case 142:
                mPokemonImage = R.drawable.pokemon_aerodactyl;
                break;
            case 143:
                mPokemonImage = R.drawable.pokemon_snorlax;
                break;
            case 144:
                mPokemonImage = R.drawable.pokemon_articuno;
                break;
            case 145:
                mPokemonImage = R.drawable.pokemon_zapdos;
                break;
            case 146:
                mPokemonImage = R.drawable.pokemon_moltres;
                break;
            case 147:
                mPokemonImage = R.drawable.pokemon_dratini;
                break;
            case 148:
                mPokemonImage = R.drawable.pokemon_dragonair;
                break;
            case 149:
                mPokemonImage = R.drawable.pokemon_dragonite;
                break;
            case 150:
                mPokemonImage = R.drawable.pokemon_mewtwo;
                break;
            case 151:
                mPokemonImage = R.drawable.pokemon_mew;
                break;
            default:
                mPokemonImage = R.drawable.ic_pokemon_go_logo;
        }
    }
}
