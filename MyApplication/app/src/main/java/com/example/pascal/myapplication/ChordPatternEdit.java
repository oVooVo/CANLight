package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

/**
 * Created by pascal on 03.10.16.
 */
public class ChordPatternEdit extends EditText {

    private Handler updateHighlightsHandler;

    public ChordPatternEdit(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.LEFT | Gravity.TOP);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (StyleSpan ss : getText().getSpans(0, getText().length() - 1, StyleSpan.class)) {
                    getText().removeSpan(ss);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (updateHighlightsHandler != null) {
                    updateHighlightsHandler.removeCallbacksAndMessages(null);
                }
                updateHighlightsHandler = new Handler();
                updateHighlightsHandler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        updateHighlights();
                    }
                }, 1000);
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

    public void transpose(int transpose)
    {
        String text = getText().toString();
        int i = 0;
        for (String line : text.split("\n", -1)) {
            Chord.Line cLine = Chord.parseLine(line);
            for (String token : cLine.tokens)
            {
                int additional = 0;
                Chord chord = Chord.chordFromString(token);
                if (chord.isValid() && cLine.isChordLine)
                {
                    chord.transpose(transpose);
                    String c = chord.toString();
                    final String before = i >= 0 ? text.substring(0, i) : "";
                    final int endIndex = i + token.length();
                    final String after =  endIndex < text.length() ? text.substring(endIndex) : "";
                    text = before + c + after;
                    additional = c.length() - token.length();
                }
                i += token.length() + additional + 1;
            }
        }
        setText(text);
        updateHighlights();
    }
}
