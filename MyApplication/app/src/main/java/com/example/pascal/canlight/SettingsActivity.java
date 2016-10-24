package com.example.pascal.canlight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Arrays;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        switch (requestCode) {
            case MainActivity.LOGIN_SPOTIFY_REQUEST:
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
                AuthenticationResponse.Type result = MySpotify.onLoginResponse(response);
                if (AuthenticationResponse.Type.ERROR.equals(result)) {
                    Toast.makeText(this, R.string.cannot_authorize, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.authorize_successfull, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }}
