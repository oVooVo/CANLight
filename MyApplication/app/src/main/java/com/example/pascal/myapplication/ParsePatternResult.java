package com.example.pascal.myapplication;

/**
 * Created by pascal on 05.10.16.
 */
public class ParsePatternResult {

    private String pattern;

    public ParsePatternResult(String text) {
        parse(text);
    }

    String pattern() {
        return pattern;
    }


    void parse(String text) {
        final String startSequence = "<pre class=\"js-tab-content\">";
        final String endSequence = "</pre>";
        int startIndex = text.indexOf(startSequence);
        int endIndex = text.indexOf(endSequence, startIndex + 1);
        if (startIndex < 0 || endIndex <= startIndex) {
            pattern = null;
        } else {
            startIndex += startSequence.length();
            pattern = text.substring(startIndex, endIndex);
            pattern = pattern.replace("<span>", "");
            pattern = pattern.replace("</span>", "");
        }
    }
}