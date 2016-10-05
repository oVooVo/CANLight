package com.example.pascal.myapplication;

import junit.framework.AssertionFailedError;

import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by pascal on 04.10.16.
 */
public class ParseSearchResults {

    private ArrayList<Entry> entries;

    public ParseSearchResults(String result) {
        entries = getEntries(result);
    }

    public ArrayList<Entry> entries() { return entries; }

    static public class Entry {
        public String url = "";
        public String name = "";
        public String artist = "";
        public String type = "";
    }

    static private class StringPart {
        public int start;
        public int end;
        private String string;

        StringPart(String string) {
            this.string = string;
        }

        String string() {
            return string.substring(start, end);
        }
    }

    StringPart getPart(String html, int offset, String pre, String post) {
        StringPart sp = new StringPart(html);
        sp.start = html.indexOf(pre, offset);
        if (sp.start < 0) {
            return null;
        } else {
            sp.end = html.indexOf(post, sp.start + 1);
            if (sp.end <= sp.start) {
                return null;
            } else {
                return sp;
            }
        }
    }

    static private String stripHtml(String html) {
        return html.replaceAll("<.*?>", "");
    }

    ArrayList<Entry> getEntries(String html) {
        ArrayList<Entry> entries = new ArrayList<>();

        String artist = "No Artist";

        int offset = 0;
        StringPart part;
        while ((part = getPart(html, offset, "<td", "</td>")) != null) {
            String currentPart = part.string();
            if (currentPart.startsWith("<td><a onclick=\"window.trackCorrected('ARTIST')\"")) {
                // we found an artist!
                artist = parseArtist(part.string());
            } else if (currentPart.startsWith("<td class=\"search-version")) {
                StringPart ignoreMe = getPart(html, part.end + 1, "<td>", "</td>");
                StringPart typePart = getPart(html, ignoreMe.end + 1, "<td><strong>", "</strong></td>");
                if (typePart != null) {
                    final String type = stripHtml(typePart.string());
                    final Entry e = parseEntry(part.string(), artist, type);
                    if (e != null) {
                        entries.add(e);
                    } else {
                        // "Found unreadable entry.";
                    }
                } else {
                    // "Did not find type");
                }
            } else {
                // unknown part. ignore.
            }
            offset = part.end + 1;
        }
        return entries;
    }

    private String parseArtist(String html) {
        return stripHtml(html).trim();
    }

    private Entry parseEntry(String html, String artist, String type) {
        final StringPart linkAndNamePart = getPart(html, 0, "<a onclick=\"window.trackCorrected('TAB')\"", "</a>");
        if (linkAndNamePart == null) {
            return null;
        }
        final String linkAndName = linkAndNamePart.string();
        final String name = stripHtml(linkAndName);
        final StringPart linkPart = getPart(linkAndName, 0, "href=\"", "\" class=\"");
        if (linkPart == null) {
            return null;
        }
        Entry e = new Entry();
        e.name = name.trim();
        e.url = linkPart.string().substring("href=\"".length()).trim();
        e.artist = artist;
        e.type = type;
        return e;
    }
}
