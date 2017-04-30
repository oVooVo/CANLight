package com.example.pascal.canlight.chordPattern;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.pascal.canlight.BuildConfig;

import junit.framework.AssertionFailedError;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pascal on 28.04.17.
 */

class UltimateGuitarChordPatternImporter extends ChordPatternImporter {

    private static final String TAG = "UltimateGuitarCPI";

    private void getSearchPage(Context context, String query, int pageNum, OnResult<String> callback) {
        String url = "https://www.ultimate-guitar.com/search.php|search.php3?search_type=title&order=&value=%s&page=%s";
        try {
            url = String.format(url, URLEncoder.encode(query, "UTF-8"), pageNum);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported.");
        }

        Log.i(TAG, "Search URL = " + url);
        download(context, url, callback);
    }

    static private int getNumSearchPages(String searchResults) {
        Document document = Jsoup.parse(searchResults);
        List<Element> paginations = document.select("ul.pagination");
        if (paginations.size() == 0) {
            // no pagination => just this site.
            return 1;
        } else {
            if (BuildConfig.DEBUG && !(paginations.size() == 1)) {
                throw new AssertionFailedError();
            }
            List<Element> pageLinks = paginations.get(0).select("a");

            // last link is 'next', we don't want to count it.
            return pageLinks.size() - 1;
        }
    }

    private void getAllSearchPages(final Context context,
                                           final String query,
                                           final int maxPages,
                                           final OnResult<List<String>> callback) {
        getSearchPage(context, query, 1, new OnResult<String>() {
            @Override
            public void onSuccess(String result) {
                int n_ = getNumSearchPages(result);
                Log.i(TAG, "Number of result pages: max(" + n_ + ", " + maxPages + ")");
                final int n = Math.min(maxPages, n_);

                if (n == 0) {
                    // n is never 0. Even if there is no result, a result page will be returned, however, it won't be parsable.
                    throw new AssertionFailedError();
                } else {
                    final String[] pages = new String[n];
                    final boolean[] finished = new boolean[n];

                    pages[0] = result;
                    finished[0] = true;

                    for (int i_ = 2; i_ < n + 1; ++i_) {
                        final int i = i_;
                        final int index = i - 1;
                        Log.i(TAG, "Download page " + i + "/" + n);
                        getSearchPage(context, query, i, new OnResult<String>() {
                            @Override
                            public void onSuccess(String result) {
                                pages[index] = result;
                                finished[index] = true;

                                Log.i(TAG, "Page " + i + "/" + n + " finished.");
                                callCallbackIfFinished(finished, pages, callback);
                            }

                            @Override
                            public void onFail(String error) {
                                finished[index] = true;
                                Log.w(TAG, "Page " + i + "/" + n + " failed.");
                                callCallbackIfFinished(finished, pages, callback);
                            }
                        });
                    }
                    // if there is just one page...
                    callCallbackIfFinished(finished, pages, callback);
                }
            }

            @Override
            public void onFail(String error) {
                callback.onFail(error);
            }

            private void callCallbackIfFinished(boolean[] finished, String[] pages,
                                                OnResult<List<String>> callback) {
                if (BuildConfig.DEBUG && pages.length != finished.length) {
                    throw new AssertionFailedError();
                }

                for (int i = 0; i < finished.length; ++i) {
                    if (!finished[i]) {
                        Log.i(TAG, "Waiting for download " + i);
                        return;
                    }
                }
                Log.i(TAG, "Finished all Downloads.");
                callback.onSuccess(Arrays.asList(pages));
            }
        });
    }

    static private String getFirstColumn(List<Element> columns) {
        for (final Element td : columns) {
            List<Element> artists = td.select("a.song.search_art.js-search-spelling-artist");
            if (artists.size() > 0) {
                if (BuildConfig.DEBUG && !(artists.size() == 1)) {
                    throw new AssertionFailedError();
                }
                return artists.get(0).text();
            }
        }
        return "";
    }

    static private ChordPatternImporter.SearchResult getSecondColumn(List<Element> columns) {
        for (final Element td : columns) {
            List<Element> divs = td.select("div.search-version--link.js-tooltip");
            if (divs.size() > 0) {
                if (BuildConfig.DEBUG && !(divs.size() == 1)) {
                    throw new AssertionFailedError();
                }

                final Element a = divs.get(0).select("a.song.result-link.js-search-spelling-link").get(0);
                SearchResult e = new SearchResult();
                e.url  = a.attr("href");
                e.name = a.text().trim().replace("\u00A0", " ");
                return e;
            }
        }
        return null;
    }

    static private String getLastColumn(List<Element> columns) {
        for (final Element td : columns) {
            List<Element> strongs = td.select("strong");
            if (strongs.size() > 0) {
                if (BuildConfig.DEBUG && !(strongs.size() == 1)) {
                    throw new AssertionFailedError();
                }
                return strongs.get(0).text();
            }
        }

        // unreachable.
        throw new AssertionFailedError();
    }

    static List<SearchResult> parseSearchPage(final String page) {
        Document doc = Jsoup.parse(page);
        List<Element> resultTables = doc.select("table.tresults");
        Log.i(TAG, "Found " + resultTables.size() + " result tables");

        List<SearchResult> searchResults = new ArrayList<>();
        if (resultTables.size() > 0) {
            if (BuildConfig.DEBUG && !(resultTables.size() == 1)) {
                Log.i(TAG, Integer.toString(resultTables.size()));
                throw new AssertionFailedError();
            }
            List<Element> rows = resultTables.get(0).select("tr");

            String currentArtist = "";
            for (final Element row : rows) {
                List<Element> columns = row.select("td");

                final String firstColumn = getFirstColumn(columns);
                if (!firstColumn.isEmpty()) {
                    currentArtist = firstColumn;
                }

                SearchResult e = getSecondColumn(columns);
                if (e != null) {
                    e.artist = currentArtist;
                    e.type = getLastColumn(columns);
                    searchResults.add(e);
                }
            }
        }
        return searchResults;
    }

    @Override
    protected void getSearchResults(Context context, String query, int maxPages,
                          final OnResult<List<SearchResult>> callback) {
        getAllSearchPages(context, query, maxPages, new OnResult<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                List<SearchResult> searchResults = new ArrayList<>();
                for (final String page : result) {
                    List<SearchResult> pageResults = parseSearchPage(page);
                    searchResults.addAll(pageResults);
                }
                if (searchResults.isEmpty()) {
                    callback.onFail("No Results");
                } else {
                    callback.onSuccess(searchResults);
                }
            }

            @Override
            public void onFail(String error) {
                callback.onFail(error);
            }
        });
    }

    @Override
    protected void getChordPattern(final Context context, String url,
                           final OnResult<String> callback) {
        download(context, url, new OnResult<String>() {
            @Override
            public void onSuccess(String result) {
                Document doc = Jsoup.parse(result);
                String pattern = doc.select("div.tb_ct").select("pre.js-tab-content").text();
                callback.onSuccess(pattern);
            }

            @Override
            public void onFail(String error) {
                callback.onFail(error);
            }
        });
    }

}
