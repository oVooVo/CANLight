package com.example.pascal.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import junit.framework.AssertionFailedError;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pascal on 06.10.16.
 */
public class Importer {
    static public class SearchResult {
        public String url = "";
        public String name = "";
        public String artist = "";
        public String type = "";
    }

    private Importer() {

    }

    //TODO save hash over session end
    static protected HashMap<String, SearchResult[]> searchResultCache = new HashMap<>();
    static protected HashMap<String, String> patternCache = new HashMap<>();

    public static abstract class SearchResults {
        private static final String URL_PATTERN = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=";
        public SearchResults(String key, Context context) {
            if (searchResultCache.containsKey(key)) {
                onSearchResultsArrived(searchResultCache.get(key));
            } else {
                final String fKey = key;
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    final String url;
                    try {
                        url = URL_PATTERN + URLEncoder.encode(key, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionFailedError();
                    }
                    new DownloadWebpageTask() {
                        // onPostExecute displays the results of the AsyncTask.
                        @Override
                        protected void onPostExecute(String result) {
                            ParseSearchResults parser = new ParseSearchResults(result);
                            ArrayList<SearchResult> srs = parser.entries();
                            SearchResult[] sar = srs.toArray(new SearchResult[srs.size()]);
                            if (sar.length > 0) {
                                searchResultCache.put(fKey, sar);
                            }
                            onSearchResultsArrived(srs.toArray(sar));
                        }
                    }.execute(url);
                } else {
                    onSearchResultsArrived(new SearchResult[0]);
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
            }
        }

        abstract void onSearchResultsArrived(SearchResult[] results);
    }

    public static abstract class Pattern {
        public Pattern(String url, Context context) {
            final String fUrl = url;
            final Context fContext = context;
            if (patternCache.containsKey(url)) {
                onPatternArrived(patternCache.get(url));
            } else {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadWebpageTask() {
                        // onPostExecute displays the results of the AsyncTask.
                        @Override
                        protected void onPostExecute(String result) {
                            ParsePatternResult parser = new ParsePatternResult(result);
                            final String pattern = parser.pattern();
                            if (pattern == null) {
                                Toast.makeText(fContext, R.string.download_pattern_failed, Toast.LENGTH_SHORT).show();
                            } else {
                                patternCache.put(fUrl, pattern);
                            }
                            onPatternArrived(pattern);
                        }
                    }.execute(url);
                } else {
                    onPatternArrived(null);
                    Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
            }
        }

        abstract void onPatternArrived(String pattern);

    }
}
