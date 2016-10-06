package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

import java.util.regex.Pattern;

/**
 * Created by pascal on 03.10.16.
 */
public class ChordPatternEdit extends EditText {

    private Handler updateHighlightsHandler;
    private boolean editLoopKillerFlag = true;


    public ChordPatternEdit(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.LEFT | Gravity.TOP);
        setHorizontallyScrolling(true);
        setTypeface(Typeface.MONOSPACE);
        setMovementMethod(ScrollingMovementMethod.getInstance());

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (StyleSpan ss : getText().getSpans(0, getText().length() - 1, StyleSpan.class)) {
                    getText().removeSpan(ss);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editLoopKillerFlag) {
                    if (updateHighlightsHandler != null) {
                        updateHighlightsHandler.removeCallbacksAndMessages(null);
                    }
                    updateHighlightsHandler = new Handler();
                    updateHighlightsHandler.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            editLoopKillerFlag = false;
                            updateHighlights();
                            editLoopKillerFlag = true;

                        }
                    }, 1000);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                //updateHighlights();
            }
        });
    }

    @SuppressWarnings("deprecation")
    static Spanned spannableFromHTML(String html) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    private void updateHighlights() {
        String text = getText().toString();
        String[] lines = text.split("\n");

        //TODO encode stuff with html, then use HTML.fromHtml() seems to be faster.
        //TODO   However, handling whitespaces makes trouble. Also editing is not easy.

        Spannable span = getText();
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

    public void setText(String text) {
        super.setText(text);
        updateHighlights();
    }


    static final Pattern CAN_REMOVE_FIRST_CHARACTER_PATTERN = Pattern.compile("^\\s(" + Chord.CHORD_SPLIT_PATTERN + ").*", Pattern.DOTALL);
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
                    String after =  endIndex < text.length() ? text.substring(endIndex) : "";
                    additional = c.length() - token.length();

                    // expand c to match length of token (if token is longer than c)
                    while (!after.startsWith("\n") && additional < 0) {
                        c += " ";
                        additional++;
                    }
                    // try to trim after to match length of token (if c is longer than token)
                    while (!after.startsWith("\n") && additional > 0 && CAN_REMOVE_FIRST_CHARACTER_PATTERN.matcher(after).matches()) {
                        additional -= 1;
                        after = after.substring(1);
                    }

                    text = before + c + after;
                }
                i += token.length() + additional + 1;
            }
        }
        setText(text);
        updateHighlights();
    }
}
