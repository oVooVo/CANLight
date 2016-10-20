package com.example.pascal.canlight;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pascal on 20.10.16.
 */
public class YouTubeTrackAdapter extends GetSongDialog.TrackAdapter {
    private final static String TAG = "YouTubeTrackAdapter";

    private final YouTube mYouTube;
    private final Context mContext;
    private final List<SearchResult> mSearchResults;
    final Map<View, YouTubeThumbnailLoader> mLoaders;
    final Map<View, String> mCurrentIds;

    public YouTubeTrackAdapter(Context context) {
        mContext = context;
        mYouTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {

            }
        }).setApplicationName(context.getApplicationInfo().name).build();
        mSearchResults = new ArrayList<>();
        mLoaders = new HashMap<>();
        mCurrentIds = new HashMap<>();
    }

    public void deinit() {
        for (YouTubeThumbnailLoader loader : mLoaders.values()) {
            loader.release();
        }
        mLoaders.clear();
    }

    @Override
    void search(String key) {
        if (mYouTube == null) {
            Log.w(TAG, "YouTube not initialized");
            return;
        }
        new AsyncTask<String, Void, SearchListResponse>() {

            @Override
            protected void onPreExecute() {
                mSearchResults.clear();
                notifyDataSetChanged();
            }

            @Override
            protected SearchListResponse doInBackground(String... params) {
                final String query = params[0];
                final String key = params[1];
                SearchListResponse response = null;
                try {
                    YouTube.Search.List search = mYouTube.search().list("id,snippet");
                    search.setKey(key);
                    search.setQ(query);
                    search.setType("video");
                    search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults((long) 25);
                    response = search.execute();
                } catch (IOException e) {
                    Log.w(TAG, "IOException in search thread");
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(SearchListResponse result) {
                for (SearchResult r : result.getItems()) {
                    if ("youtube#video".equals(r.getId().getKind())) {
                        mSearchResults.add(r);
                    }
                }
                notifyDataSetChanged();
            }
        }.execute(key, mContext.getString(R.string.google_developer_key));
    }

    @Override
    String getId(int position) {
        return mSearchResults.get(position).getId().getVideoId();
    }

    @Override
    String getLabel(int position) {
        return mSearchResults.get(position).getSnippet().getTitle();
    }

    @Override
    public int getCount() {
        return mSearchResults.size();
    }

    private static class ViewHolder {
        YouTubeThumbnailView mThumbnailView;
        TextView mtextView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LinearLayout ll = new LinearLayout(mContext);
            ll.setOrientation(LinearLayout.VERTICAL);

            viewHolder = new ViewHolder();
            viewHolder.mThumbnailView = new YouTubeThumbnailView(mContext);
            viewHolder.mtextView = new TextView(mContext);
            viewHolder.mtextView.setSingleLine(true);

            ll.addView(viewHolder.mtextView);
            ll.addView(viewHolder.mThumbnailView);
            ll.setPadding(0, 0, 0, 10);

            viewHolder.mThumbnailView.initialize(mContext.getString(R.string.google_developer_key),
                    new YouTubeThumbnailView.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                    mLoaders.put(youTubeThumbnailView, youTubeThumbnailLoader);
                    setVideo(youTubeThumbnailView, mSearchResults.get(position).getId().getVideoId());
                }

                @Override
                public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                }
            });

            convertView = ll;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = ((ViewHolder) convertView.getTag());
            setVideo(viewHolder.mThumbnailView, mSearchResults.get(position).getId().getVideoId());
        }

        viewHolder.mtextView.setText(mSearchResults.get(position).getSnippet().getTitle());

        return convertView;
    }

    void setVideo(YouTubeThumbnailView view, String id) {
        if (!id.equals(mCurrentIds.get(view))) {
            mCurrentIds.put(view, id);
            YouTubeThumbnailLoader loader = mLoaders.get(view);
            if (loader != null) {
                view.setImageResource(R.drawable.ic_thumbnail_placeholder);
                loader.setVideo(id);
            }
        }
    }
}
