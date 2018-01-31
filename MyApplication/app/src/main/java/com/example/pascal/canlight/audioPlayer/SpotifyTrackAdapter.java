package com.example.pascal.canlight.audioPlayer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pascal.canlight.MySpotify;
import com.example.pascal.canlight.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pascal on 20.10.16.
 */
public class SpotifyTrackAdapter extends TrackAdapter {
    private final static String TAG = "SpotifyTrackAdapter";
    private final List<String> mIds;
    private final List<String> mLabels;
    private final Context mContext;

    SpotifyTrackAdapter(Context context) {
        mIds = new LinkedList<>();
        mLabels = new LinkedList<>();
        mContext = context;
    }

    private static String getTrackLabel(Track track) {
        if (track == null) {
            return "";
        } else {
            List<String> artists = new ArrayList<>(track.artists.size());
            for (ArtistSimple a : track.artists) {
                artists.add(a.name);
            }
            return track.name + " - " + TextUtils.join(", ", artists);
        }
    }

    @Override
    public boolean readyToUse() {
        return true;
    }

    @Override
    void search(String key) {
        MySpotify.searchTracks(mContext, key, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                mLabels.clear();
                mIds.clear();
                for (Track track : tracksPager.tracks.items) {
                    mLabels.add(getTrackLabel(track));
                    mIds.add(track.id);
                }
                onResultsArrived(mIds);
                notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {
                mLabels.clear();
                mIds.clear();

                Log.w(TAG, "search tracks: failure " + error.toString());
                notifyDataSetChanged();
                onResultsArrived(mIds);
            }
        });
    }

    @Override
    String getId(int position) {
        return mIds.get(position);
    }

    @Override
    String getLabel(int position) {
        return mLabels.get(position);
    }

    @Override
    int getIcon() {
        return R.drawable.ic_spotify;
    }

    public static final String NAME = "Spotify";
    @Override
    String getName() {
        return NAME;
    }

    @Override
    public int getCount() {
        return mIds.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new TextView(mContext);
        }

        TextView textView = (TextView) convertView;
        textView.setText(mLabels.get(position));
        return textView;
    }
}
