package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by pascal on 03.10.16.
 */
public class ChordPatternEdit extends EditText {

    public ChordPatternEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.LEFT | Gravity.TOP);
        addTextChangedListener(new TextWatcher() {


            class Lines {
                String[] lines;
                int offset;
            }

            Lines getLines(String s, int start, int length) {
                String[] lines = s.split("\n", -1);
                ArrayList<String> linesOfInterest = new ArrayList<>();

                final int end = start + length - 1;

                int position = 0;
                int offset = -1;
                for (String line : lines) {
                    final int lineStart = position;
                    final int lineEnd = position + line.length();

                    if ( (start <= lineStart + 1 && end >= lineStart)
                      || (start >= lineStart - 1 && start <= lineEnd) ) {
                        if (offset < 0) {
                            offset = lineStart;
                        }
                        linesOfInterest.add(line);
                    }

                    position = lineEnd + 1;
                }

                Lines result = new Lines();
                result.lines = linesOfInterest.toArray(new String[linesOfInterest.size()]);
                result.offset = offset;
                return result;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (StyleSpan ss : getText().getSpans(start, start + count - 1, StyleSpan.class)) {
                    getText().removeSpan(ss);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Spannable span = getText();
                Lines lines = getLines(span.toString(), start, count);
                int position = lines.offset;
                for (String line : lines.lines) {
                    Chord.Line cLine = Chord.parseLine(line);
                    for (String token : cLine.tokens) {
                        Chord chord = Chord.chordFromString(token);
                        if (cLine.isChordLine && chord.isValid()) {
                            StyleSpan bold = new StyleSpan(Typeface.BOLD);
                            span.setSpan(bold, position, position + token.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                        position = position + token.length() + 1;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //updateHighlights();
            }
        });
    }

    private void updateHighlights() {
        Spannable span = getText();
        String text = getText().toString();
        String[] lines = text.split("\n");

        int position = 0;
        for (String line : lines) {
            Chord.Line cLine = Chord.parseLine(line);
            for (String token : cLine.tokens) {
                Chord chord = Chord.chordFromString(token);
                if (cLine.isChordLine && chord.isValid()) {
                    StyleSpan bold = new StyleSpan(Typeface.BOLD);
                    span.setSpan(bold, position, position + token.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                position = position + token.length() + 1;
            }
        }
    }
}
