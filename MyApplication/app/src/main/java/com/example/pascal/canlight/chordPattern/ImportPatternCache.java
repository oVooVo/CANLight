package com.example.pascal.canlight.chordPattern;

import junit.framework.AssertionFailedError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by pascal on 07.10.16.
 */
public class ImportPatternCache {


    static private HashMap<String, PatternImporter.SearchResult[]> searchResultCache = new HashMap<>();
    static private HashMap<String, String> patternCache = new HashMap<>();
    public static boolean isPatternCached(String url) {
        return patternCache.containsKey(url);
    }
    public static boolean isSearchCached(String key) {
        return searchResultCache.containsKey(key);
    }
    public static PatternImporter.SearchResult[] searchResults(String key) {
        return searchResultCache.get(key);
    }
    public static String pattern(String url) {
        return patternCache.get(url);
    }
    public static void putPattern(String url, String pattern) {
        patternCache.put(url, pattern);
    }
    public static void putSearchResults(String key, PatternImporter.SearchResult[] searchResults) {
        searchResultCache.put(key, searchResults);
    }
    public static void load() {
    }

    public static void save() {
    }

    public static void clear() {
        patternCache.clear();
        searchResultCache.clear();
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
        JSONObject patternsObject = new JSONObject(patternCache);
        JSONObject searchResultsObject = new JSONObject();
        try {
            for (String key : searchResultCache.keySet()) {
                searchResultsObject.put(key, toJsonArray(searchResultCache.get(key)));
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

        patternCache.clear();
        Iterator<?> urls = patternObject.keys();
        while (urls.hasNext()) {
            final String url = (String) urls.next();
            try {
                patternCache.put(url, patternObject.getString(url));
            } catch (JSONException e) {
                throw new AssertionFailedError();
            }
        }

        searchResultCache.clear();
        Iterator<?> keys = searchResultObject.keys();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            try {
                JSONArray array = patternObject.getJSONArray(key);
                searchResultCache.put(key, fromJsonArray(array));
            } catch (JSONException e) {
                throw new AssertionFailedError();
            }
        }
    }

    public static int computeSizeInKB() {
        return toJson().toString().getBytes().length / 1000;
    }

    public static int numberOfItems() {
        return patternCache.size() + searchResultCache.size();
    }

}
