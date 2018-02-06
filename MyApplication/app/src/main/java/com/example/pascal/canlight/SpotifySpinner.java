package com.example.pascal.canlight;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.example.pascal.canlight.audioPlayer.SpotifyTrackAdapter;
import com.example.pascal.canlight.audioPlayer.TrackAdapter;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by pascal on 10.10.16.
 */
public class SpotifySpinner extends android.support.v7.widget.AppCompatAutoCompleteTextView {
    private static final String TAG = "SpotifySpinner";
    private static int requestId = 0;
    private Handler mHandler;
    private List<TrackAdapter.Track> mTracks;
    private Adapter mAdapter;

    private static class Adapter extends ArrayAdapter<String> {
        List<String> mItems;
        public Adapter(Context context, List<String> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            mItems = items;
        }

        public Filter getFilter() {
            return new NullFilter();
        }

        class NullFilter extends Filter {
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                results.values = mItems;
                return results;
            }

            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                //noinspection unchecked
                mItems.clear();
                for (Object item : (List) results.values) {
                    mItems.add((String) item);
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            setNotifyOnChange(false);
        }
    }

    public SpotifySpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SpotifySpinner(Context context) {
        super(context);
        init();
    }

    private void init() {
        // we need two dedicated lists for the displayNames
        // since onItemClick will trigger a new search which immediately clear the list
        // then we want to have the result of the (previous) search.
        mTracks = new ArrayList<>();
        mAdapter = new Adapter(getContext(), new ArrayList<>());
        setAdapter(mAdapter);
        setMaxLines(1);
        setSingleLine(true);
    }

    public interface OnTrackFoundListener {
        void onTrackFound(TrackAdapter.Track track);
    }

    public static void findTrack(Context context, final Song song, final OnTrackFoundListener l) {
        MySpotify.searchTracks(context, song.getName(), new Callback<TracksPager>() {
                    @Override
            public void success(TracksPager tracksPager, Response response) {
                if (!tracksPager.tracks.items.isEmpty()) {
                    final Track track = tracksPager.tracks.items.get(0);
                    song.setTrack(new TrackAdapter.Track(getTrackLabel(track), SpotifyTrackAdapter.SERVICE_NAME, track.id));
                    if (l != null) {
                        l.onTrackFound(song.getTrack());
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public static String getTrackLabel(Track track) {
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

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (mAdapter == null) {
            return;
        }
        final String key = text.toString() + "*";
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = new Handler();
        mHandler.postDelayed(() -> {

            requestId++;
            final int currentRequestId = requestId;
            MySpotify.searchTracks(getContext(), key, new Callback<TracksPager>() {
                @Override
                public void success(TracksPager tracksPager, Response response) {
                    if (currentRequestId == requestId) {
                        mTracks.clear();
                        for (Track t : tracksPager.tracks.items) {
                            final String trackName = getTrackLabel(t);
                            mTracks.add(new TrackAdapter.Track(getTrackLabel(t), SpotifyTrackAdapter.SERVICE_NAME, t.id));
                            mAdapter.add(trackName);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }, 300);
    }

    public TrackAdapter.Track getTrack(int position) {
        return mTracks.get(position);
    }
}
