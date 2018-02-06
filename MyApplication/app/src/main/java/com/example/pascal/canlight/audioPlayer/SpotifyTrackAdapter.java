package com.example.pascal.canlight.audioPlayer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pascal.canlight.MySpotify;
import com.example.pascal.canlight.R;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pascal on 20.10.16.
 */
public class SpotifyTrackAdapter extends TrackAdapter {
    private final static String TAG = "SpotifyTrackAdapter";
    private final List<kaaes.spotify.webapi.android.models.Track> mTracks;
    private final Context mContext;

    SpotifyTrackAdapter(Context context) {
        mTracks = new LinkedList<>();
        mContext = context;
    }

    @Override
    public boolean readyToUse() {
        return true;
    }

    private static String getLabel(kaaes.spotify.webapi.android.models.Track track) {
        List<String> artists = track.artists.stream().map(artist -> artist.name).collect(Collectors.toList());
        return track.name
                + " • " + TextUtils.join(", ", artists);
    }

    @Override
    void search(String key) {
        MySpotify.searchTracks(mContext, key, new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                setTracks(tracksPager.tracks.items);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.w(TAG, "search tracks: failure " + error.toString());
                setTracks(new LinkedList<>());
            }

            private void setTracks(List<kaaes.spotify.webapi.android.models.Track> tracks) {
                mTracks.clear();
                mTracks.addAll(tracks);
                onResultsArrived(mTracks);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    int getIcon() {
        return R.drawable.ic_spotify;
    }

    public static final String SERVICE_NAME = "Spotify";

    @Override
    String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public Track getItem(int i) {
        final kaaes.spotify.webapi.android.models.Track track = mTracks.get(i);
        return new Track(getLabel(track), getServiceName(), track.id);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        class SpotifyTrackResultView extends LinearLayout {
            private TextView mTitleView;
            private TextView mSubtitleView;

            public SpotifyTrackResultView(Context context) {
                super(context);
                mTitleView = new TextView(context);
                mSubtitleView = new TextView(context);
                addView(mTitleView);
                addView(mSubtitleView);
            }

            void setTrack(kaaes.spotify.webapi.android.models.Track track) {
                final String title = track.name;
                final List<String> artists = track.artists.stream().map(artist -> artist.name).collect(Collectors.toList());
                final String subtitle =
                        TextUtils.join(", ", artists)
                        + " • " + track.album.name;
                mTitleView.setText(title);
                mTitleView.setTextSize(100);
                mTitleView.setTextColor(mContext.getColor(R.color.colorTextPrimary));

                mSubtitleView.setText(subtitle);
                mSubtitleView.setTextColor(mContext.getColor(R.color.colorTextSecondary));
            }
        }

        SpotifyTrackResultView srv = (SpotifyTrackResultView) convertView;
        if (convertView == null) {
            srv = new SpotifyTrackResultView(mContext);
        }

        srv.setTrack(mTracks.get(position));

        return srv;
    }

}
