package com.example.pascal.canlight;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

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
public class SpotifySpinner extends AutoCompleteTextView {

    private static int requestId = 0;
    private Handler mHandler;
    private List<String> mIds;
    private List<String> mDisplayNames;
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

    private final Adapter mAdapter;
    public SpotifySpinner(Context context) {
        super(context);

        // we need two dedicated lists for the displayNames
        // since onItemClick will trigger a new search which immediately clear the list
        // then we want to have the result of the (previous) search.
        mDisplayNames = new ArrayList<>();
        mIds = new ArrayList<>();
        mAdapter = new Adapter(context, new ArrayList<String>());
        setAdapter(mAdapter);
        setMaxLines(1);
        setSingleLine(true);
    }

    public static void findTrack(final Song song) {
        MySpotify.getSpotifyService().searchTracks(song.getName(), new Callback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                if (!tracksPager.tracks.items.isEmpty()) {
                    final Track track = tracksPager.tracks.items.get(0);
                    song.setSpotifyTrack(track.id, getTrackDisplayName(track));
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public static String getTrackDisplayName(Track track) {
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
        mHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {

                requestId++;
                final int currentRequestId = requestId;
                MySpotify.getSpotifyService().searchTracks(key, new Callback<TracksPager>() {
                    @Override
                    public void success(TracksPager tracksPager, Response response) {
                        if (currentRequestId == requestId) {
                            mIds.clear();
                            mDisplayNames.clear();
                            for (Track t : tracksPager.tracks.items) {
                                final String trackName = getTrackDisplayName(t);
                                mDisplayNames.add(trackName);
                                mIds.add(t.id);
                                mAdapter.add(trackName);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        }, 300);
    }

    String getId(int position) {
        return mIds.get(position);
    }

    String getDisplayName(int position) {
        return mDisplayNames.get(position);
    }
}
