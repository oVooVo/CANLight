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

    private ArrayList<URL> urls;
    private ArrayList<String> items;

    public ParseSearchResults(String result) {
        urls = new ArrayList<>();
        items = new ArrayList<>();
        parse(result);
    }

    public ArrayList<String> items() { return items; }
    public ArrayList<URL> urls() { return urls; }

    static final Pattern ENTRY_PATTERN = Pattern.compile(
              Pattern.quote("<span>[ <b class=\"ratdig\">")
            + "[0-9]*"
            + Pattern.quote("</b> ]</span></td>")
            + "\\s*"
            + Pattern.quote("<td><strong>")
            + "(chords|tab)"
            + Pattern.quote("</strong></td>") );
    static final Pattern CHORD_TAB_PATTERN = Pattern.compile(
              Pattern.quote("<td><strong>")
            + "(tab|chords)"
            + Pattern.quote("</strong></td>"));
    static final Pattern END_OF_NAME = Pattern.compile(
              Pattern.quote("</div>")
            + "|"
            + Pattern.quote("<div")
            + ".*"
            + Pattern.quote(">") );

    void handleUnexpectedHTML() throws AssertionFailedError {
        System.err.println("Malformed HTML.");
        throw new AssertionFailedError();
    }

    void parse(String text) {
        int i = text.indexOf("<div class=\"content\">");
        if (i < 0) {
            return;
        } else {
            text = text.substring(i);
        }

        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);
        Matcher matcher = ENTRY_PATTERN.matcher(text);
        while (matcher.find()) {
            indices.add(matcher.end(matcher.groupCount() - 1));
        }
        ArrayList<String> encodedEntries = new ArrayList<>();
        for (int j = 1; j < indices.size(); ++j) {
            final int start = indices.get(j - 1);
            final int end =   indices.get(j);
            encodedEntries.add(text.substring(start, end));
        }

        for (String entry : encodedEntries) {
            final Matcher chordTabPatternMatcher = CHORD_TAB_PATTERN.matcher(entry);
            final String type;
            if (chordTabPatternMatcher.find()) {
                final boolean isTab = chordTabPatternMatcher.group().contains("tab");
                type = isTab ? "Tab" : "Chord";
            } else {
                type = "";
                handleUnexpectedHTML();
            }

            final String urlStartSequence = "https://";
            final String urlEndSequence = "\" class";
            final int urlStart = entry.indexOf(urlStartSequence);
            if (urlStart < 0) {
                throw new AssertionFailedError();
                //continue;
            }
            final int urlEnd = entry.indexOf(urlEndSequence, urlStart + 1);
            if (urlEnd <= urlStart) {
                handleUnexpectedHTML();
            }
            final String url_ = entry.substring(urlStart, urlEnd - 1);
            URL url = null;
            try {
                url = new URL(url_);
            } catch (MalformedURLException e) {
                handleUnexpectedHTML();
            }

            final String nameStartSequence = "class=\"song result-link\">";
            final int nameStart = entry.indexOf(nameStartSequence) + nameStartSequence.length();
            if (nameStart < 0) {
                handleUnexpectedHTML();
            }
            final Matcher endOfNameMatcher = END_OF_NAME.matcher(entry);
            endOfNameMatcher.find(nameStart + 1);
            final int nameEnd = endOfNameMatcher.start();
            if (nameEnd <= nameStart) {
                handleUnexpectedHTML();
            }

            String name = entry.substring(nameStart, nameEnd - 1);
            name = name.replaceAll("<.*?>", "");
            name = name.trim();

            final String label = type + ": " + name;
            items.add(label);
            urls.add(url);
        }
    }
}
