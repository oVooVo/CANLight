package com.example.pascal.canlight;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
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

    private static class Adapter extends ArrayAdapter<String> {
        List<String> items;
        public Adapter(Context context, List<String> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            this.items = items;
        }

        public Filter getFilter() {
            return new NullFilter();
        }

        class NullFilter extends Filter {
            protected Filter.FilterResults performFiltering(CharSequence constraint) {
                final FilterResults results = new FilterResults();
                results.values = items;
                return results;
            }

            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                //noinspection unchecked
                items.clear();
                for (Object item : (List) results.values) {
                    items.add((String) item);
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

    private final Adapter adapter;
    public SpotifySpinner(Context context) {
        super(context);
        adapter = new Adapter(context, new ArrayList<String>());
        setAdapter(adapter);
        setMaxLines(1);
        setSingleLine(true);
    }

    private static int requestId = 0;
    private Handler handler;
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (adapter == null) {
            return;
        }

        final String key = text.toString() + "*";

        adapter.clear();
        adapter.notifyDataSetChanged();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = new Handler();
        handler.postDelayed(new Runnable()
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
                            for (Track t : tracksPager.tracks.items) {
                                List<String> artists = new ArrayList<>(t.artists.size());
                                for (ArtistSimple a : t.artists) {
                                    artists.add(a.name);
                                }
                                adapter.add(t.name + " - " + TextUtils.join(", ", artists));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
            }
        }, 500);

    }
}
