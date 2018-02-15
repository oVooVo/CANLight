package com.example.pascal.canlight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Formatter;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private SettingsFragment mSettingsFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        mSettingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSettingsFragment)
                .commit();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // SettingsFragment.onActivityResult will not be called since the activity is started using
        // activity.startActivityForResult (see https://stackoverflow.com/a/11011686
        // this is in Spotify's code, hence not changeable)
        // super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        switch (requestCode) {
            case MainActivity.LOGIN_SPOTIFY_REQUEST:
                MySpotify.onLoginResponse(this, resultCode, intent);
                mSettingsFragment.updateSpotifyLoginPreference();
                break;
        }
    }


}
