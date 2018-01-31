package com.example.pascal.canlight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;

/**
 * Created by pascal on 09.10.16.
 */
public class MySpotify {
    private static SpotifyApi spotifyApi = null;

    private static final String REDIRECT_URI = "canlight-spotify://callback";
    private static boolean mLoggedIn = false;

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


    static AuthenticationResponse.Type onLoginResponse(AuthenticationResponse r) {
        switch (r.getType()) {
            // Response was successful and contains auth token
            case TOKEN:
                // Handle successful response
                getSpotifyApi().setAccessToken(r.getAccessToken());
                mLoggedIn = true;
                break;
            default:
                spotifyApi = null;
                mLoggedIn = false;
                break;
        }
        return r.getType();
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

}
