package com.example.pascal.canlight;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.util.SortedList;
import android.support.v7.widget.SearchView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import junit.framework.AssertionFailedError;

import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pascal on 09.10.16.
 */
public class MySpotify {
    private static SpotifyApi spotifyApi = null;

    public static SpotifyApi getSpotifyApi() {
        if (spotifyApi == null) {
            spotifyApi = new SpotifyApi();
        }
        return spotifyApi;
    }

    public static SpotifyService getSpotifyService() {
        return getSpotifyApi().getService();
    }

    public static AuthenticationResponse.Type onLoginResponse(AuthenticationResponse r) {
        switch (r.getType()) {
            // Response was successful and contains auth token
            case TOKEN:
                // Handle successful response
                spotifyApi.setAccessToken(r.getAccessToken());
                break;
            default:
                spotifyApi = null;
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
