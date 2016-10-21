package com.example.pascal.canlight.chordPattern;

import android.content.Context;
import android.util.Log;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by pascal on 07.10.16.
 */
public class ImportPatternCache {

    private static final String TAG = "ImportPatternCache";
    private static final String FILENAME = "ImportPatternCache";

    static private HashMap<String, PatternImporter.SearchResult[]> mSearchResultCache = new HashMap<>();
    static private HashMap<String, String> mPatternCache = new HashMap<>();

    public static boolean isPatternCached(String url) {
        return mPatternCache.containsKey(url);
    }
    public static boolean isSearchCached(String key) {
        return mSearchResultCache.containsKey(key);
    }
    public static PatternImporter.SearchResult[] searchResults(String key) {
        return mSearchResultCache.get(key);
    }
    public static String pattern(String url) {
        return mPatternCache.get(url);
    }
    public static void putPattern(String url, String pattern) {
        mPatternCache.put(url, pattern);
    }
    public static void putSearchResults(String key, PatternImporter.SearchResult[] searchResults) {
        mSearchResultCache.put(key, searchResults);
    }
    public static void load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);

            final JSONObject o;
            try {
                final String data = IOUtils.toString(fis);
                o = new JSONObject(data);
                mSearchResultCache.clear();
                mPatternCache.clear();
                fromJson(o);
            } catch (Exception e) {
                throw new AssertionFailedError();
            }
        } catch (FileNotFoundException e) {
            // that's okay, no file to restore, maybe it's the first start.
        }
    }

    public static void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            final String data = toJson().toString();
            fos.write(data.getBytes());
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
    }

    public static void clear() {
        mPatternCache.clear();
        mSearchResultCache.clear();
    }

    private static JSONArray toJsonArray(PatternImporter.SearchResult[] srs) {
        JSONArray array = new JSONArray();
        for (PatternImporter.SearchResult sr : srs) {
            array.put(sr.toJson());
        }
        return array;
    }

    private static PatternImporter.SearchResult[] fromJsonArray(JSONArray array) {
        PatternImporter.SearchResult[] srs = new PatternImporter.SearchResult[array.length()];
        try {
            for (int i = 0; i < array.length(); ++i) {
                srs[i] = new PatternImporter.SearchResult(array.getJSONObject(i));
            }
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return srs;
    }

    private static JSONObject toJson() {
        JSONObject patternsObject = new JSONObject(mPatternCache);
        JSONObject searchResultsObject = new JSONObject();
        try {
            for (String key : mSearchResultCache.keySet()) {
                searchResultsObject.put(key, toJsonArray(mSearchResultCache.get(key)));
            }
            for (String key : mPatternCache.keySet()) {
                patternsObject.put(key, mPatternCache.get(key));
            }
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        JSONObject o = new JSONObject();
        try {
            o.put("patterns", patternsObject);
            o.put("searchResults", searchResultsObject);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }

        return o;
    }

    private static void fromJson(JSONObject o) {
        JSONObject patternObject;
        JSONObject searchResultObject;
        try {
            patternObject = o.getJSONObject("patterns");
            searchResultObject = o.getJSONObject("searchResults");
        } catch (JSONException e ) {
            patternObject = new JSONObject();
            searchResultObject = new JSONObject();
        }

        mPatternCache.clear();
        Iterator<?> urls = patternObject.keys();
        while (urls.hasNext()) {
            final String url = (String) urls.next();
            try {
                mPatternCache.put(url, patternObject.getString(url));
            } catch (JSONException e) {
                //throw new AssertionFailedError();
            }
        }

        mSearchResultCache.clear();
        Iterator<?> keys = searchResultObject.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            try {
                JSONArray array = searchResultObject.getJSONArray(key);
                mSearchResultCache.put(key, fromJsonArray(array));
            } catch (JSONException e) {
                throw new AssertionFailedError();
            }
        }
    }

    public static int computeSizeInKB() {
        return toJson().toString().getBytes().length / 1000;
    }

    public static int numberOfItems() {
        return mPatternCache.size() + mSearchResultCache.size();
    }

}
