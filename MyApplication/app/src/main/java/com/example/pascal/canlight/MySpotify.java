package com.example.pascal.canlight;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Spotify;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;

/**
 * Created by pascal on 09.10.16.
 */
public class MySpotify {
    private static final String TAG = "MySpotify";
    private static SpotifyApi spotifyApi = null;

    private static final String REDIRECT_URI = "canlight-spotify://callback";
    public static com.spotify.sdk.android.player.SpotifyPlayer mPlayer;
    private static boolean mAuthenticated = false;
    private static UserPrivate mMe;

    public static SpotifyApi getSpotifyApi() {
        if (spotifyApi == null) {
            spotifyApi = new SpotifyApi();
        }
        return spotifyApi;
    }

    public static SpotifyService getSpotifyService() {
        return getSpotifyApi().getService();
    }

    public static void spotifyConnectRequest(Activity activity) {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                activity.getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(activity, MainActivity.LOGIN_SPOTIFY_REQUEST, request);
    }

    public static void spotifyDisconnectRequest(Activity activity) {
        AuthenticationClient.clearCookies(activity);
        mPlayer = null;
        mAuthenticated = false;
    }

    private static String getMarket(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getString(R.string.pref_spotifyMarket);
        return preferences.getString(key, "US").toLowerCase();
    }

    public static void searchTracks(Context context, String query, Callback<TracksPager> callback) {
        final Map<String, Object> searchParameters = new HashMap<>();

        searchParameters.put("market", getMarket(context));
        getSpotifyService().searchTracks(query, searchParameters, callback);
    }

    public static void onLoginResponse(Activity activity, int resultCode, Intent intent) {
        AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
        switch (response.getType()) {
            // Response was successful and contains auth token
            case TOKEN:
                // Handle successful response
                getSpotifyApi().setAccessToken(response.getAccessToken());
                break;
            default:
                spotifyApi = null;
                break;
        }

        AuthenticationResponse.Type result = response.getType();

        switch (result.ordinal()) {
            case 1:
                Toast.makeText(activity, R.string.authenticationSucceeded, Toast.LENGTH_SHORT).show();
                initializeSpotifyPlayer(activity, response);
                updateMe();
                mAuthenticated = true;
                break;
            case 2:
                Toast.makeText(activity, R.string.authenticationFailed, Toast.LENGTH_SHORT).show();
                mAuthenticated = false;
                mPlayer = null;
                break;
            case 3:
                Toast.makeText(activity, R.string.authenticationCanceled, Toast.LENGTH_SHORT).show();
                mAuthenticated = false;
                mPlayer = null;
                break;
        }
    }

    private static void initializeSpotifyPlayer(Context context, AuthenticationResponse response) {
        Config playerConfig = new Config(context,
                response.getAccessToken(),
                context.getString(R.string.spotify_client_id));
        Spotify.getPlayer(playerConfig,
                context,
                new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
            }

            @Override
            public void onError(Throwable throwable) {
                mPlayer = null;
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    static class UpdateMeAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mMe = getSpotifyService().getMe();
            return null;
        }
    }
    private static void updateMe() {
        new UpdateMeAsync().execute();
    }

    static boolean isAuthorized() {
        return mAuthenticated;
    }

    static UserPrivate getMe() {
        return mMe;
    }
}
