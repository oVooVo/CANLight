package com.example.pascal.canlight;

import android.app.Activity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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
    public static final String CLIENT_ID = "8874e81dddd441fb8854482e4aafc634";
    public static final String REDIRECT_URL = "canlight-spotify://callback";
    private static SpotifyApi spotifyApi = null;

    public static void loginRequest(Activity activity) {
        // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(MySpotify.CLIENT_ID,
                        AuthenticationResponse.Type.TOKEN,
                        MySpotify.REDIRECT_URL);

        builder.setScopes(new String[] { "streaming" });
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(activity,
                MainActivity.LOGIN_SPOTIFY_REQUEST,
                request);

        spotifyApi = new SpotifyApi();
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

    public static SpotifyApi spotifyApi() {
        return spotifyApi;
    }

    public static boolean isLoggedIn() {
        return spotifyApi != null;
    }

    public static void getSuggestions(String key) {
        if (spotifyApi == null) {
            System.out.println("NULL");
        }

        SpotifyService service = spotifyApi().getService();
        service.searchTracks(key, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                for (Track t : tracksPager.tracks.items) {
                    System.out.println("Track: " + t.name + t.type);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}
