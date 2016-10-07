package com.example.pascal.myapplication;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by pascal on 04.10.16.
 */
public class ParseSearchResults {

    private ArrayList<Importer.SearchResult> entries;

    public ParseSearchResults(String result) {
        entries = getEntries(result);
        for (Iterator<Importer.SearchResult> iterator = entries.iterator(); iterator.hasNext();) {
            Importer.SearchResult s = iterator.next();
            if (!filterResult(s)) {
                iterator.remove();
            }
        }
    }

    static boolean filterResult(Importer.SearchResult r) {
        final List<String> typeWhitelist = Arrays.asList("tab", "chords", "ukulele chords");
        if (typeWhitelist.contains(r.type)) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Importer.SearchResult> entries() { return entries; }

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
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("<.*?>", "");
        return html;
    }

    ArrayList<Importer.SearchResult> getEntries(String html) {
        ArrayList<Importer.SearchResult> entries = new ArrayList<>();

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
                    final Importer.SearchResult e = parseEntry(part.string(), artist, type);
                    if (e != null) {
                        entries.add(e);
                    } else {
                        // "Found unreadable entry.";
                    }
                } else {
                    // "Did not find type";
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

    private Importer.SearchResult parseEntry(String html, String artist, String type) {
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
        Importer.SearchResult e = new Importer.SearchResult();
        e.name = name.trim();
        e.url = linkPart.string().substring("href=\"".length()).trim();
        e.artist = artist;
        e.type = type;
        return e;
    }
}
