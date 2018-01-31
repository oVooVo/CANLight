package com.example.pascal.canlight.chordPattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.pascal.canlight.DownloadWebpageTask;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pascal on 28.04.17.
 */

public abstract class ChordPatternImporter {

    abstract static public class OnResult<T> {
        public abstract void onSuccess(T result);
        public void onFail(String error) {}
    }

    String userAgent() {
        return "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0";
    }

    int connectionTimeout() {
        return 2000;
    }

    int readTimeout() {
        return 20000;
    }

    protected void download(Context context, final String url, final OnResult<String> callback) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            DownloadWebpageTask task = new DownloadWebpageTask() {
                // onPostExecute displays the results of the AsyncTask.
                @Override
                protected void onPostExecute(String result) {
                    callback.onSuccess(result);
                }
            };

            task.userAgent = userAgent();
            task.connectTimeout = connectionTimeout();
            task.readTimeout = readTimeout();

            task.execute(url);
        } else {
            callback.onFail("No Internet Connection.");
        }
    }

    public abstract void getSearchResults(final Context context, final String query, final int maxPages,
                                   final OnResult<List<SearchResult>> callback);
    public abstract void getChordPattern(final Context context, final String link,
                                  final OnResult<String> callback);

    static public class SearchResult {
        public String url = "";
        public String name = "";
        public String artist = "";
        public String type = "";

        public SearchResult(JSONObject o) {
            try { url = o.getString("url"); } catch (JSONException e) {}
            try { name = o.getString("name"); } catch (JSONException e) {}
            try { artist = o.getString("artist"); } catch (JSONException e) {}
            try { type = o.getString("type"); } catch (JSONException e) {}
        }

        public SearchResult() {

        }

        public JSONObject toJson() {
            JSONObject o = new JSONObject();
            try {
                o.put("url", url);
                o.put("name", name);
                o.put("artist", artist);
                o.put("type", type);
            } catch (JSONException e) {
                throw new AssertionFailedError();
            }
            return o;
        }
    }
}
