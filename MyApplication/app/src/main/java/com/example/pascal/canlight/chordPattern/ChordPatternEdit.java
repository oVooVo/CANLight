package com.example.pascal.canlight.chordPattern;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by pascal on 03.10.16.
 */
public class ChordPatternEdit extends EditText {

    private Handler mUpdateHighlightsHandler;
    private boolean mEditLoopKillerFlag = true;
    private boolean mIsEditable;

    public ChordPatternEdit(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.LEFT | Gravity.TOP);

        AssetManager am = context.getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am, "fonts/" + "nk57-monospace-no-sb.ttf");
        setTypeface(typeface);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (StyleSpan ss : getText().getSpans(0, getText().length() - 1, StyleSpan.class)) {
                    getText().removeSpan(ss);
                }
                for (BackgroundColorSpan ss : getText().getSpans(0, getText().length() - 1, BackgroundColorSpan.class)) {
                    getText().removeSpan(ss);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditLoopKillerFlag) {
                    if (mUpdateHighlightsHandler != null) {
                        mUpdateHighlightsHandler.removeCallbacksAndMessages(null);
                    }
                    mUpdateHighlightsHandler = new Handler();
                    mUpdateHighlightsHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEditLoopKillerFlag = false;
                            updateHighlights();
                            mEditLoopKillerFlag = true;

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

    public void setIsEditable(boolean isEditable) {
        mIsEditable = isEditable;
        setFocusable(isEditable);
        setFocusableInTouchMode(isEditable);
        if (!isEditable) {
	    //hide keyboard
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private static final Pattern HEADLINE_PATTERN = Pattern.compile(
            "^\\W*(" +
                    "((pre|post)?\\W*(verse|refrain|chorus|bridge|intro|outro)(\\W|[0-9_IVX])*)" +
                    "|" +
                    "(\\[.*\\]))" +
                    "\\W*$",
            Pattern.CASE_INSENSITIVE);

    private void updateHighlights() {
        String text = getText().toString();
        String[] lines = text.split("\n");

        //encode stuff with html, then use HTML.fromHtml() seems to be faster.
        //  However, handling whitespaces makes trouble. Also editing is not easy.

        Spannable span = getText();
        int position = 0;
        for (String line : lines) {
            Chord.Line cLine = Chord.parseLine(line);
            if (HEADLINE_PATTERN.matcher(line).matches()) {
                BackgroundColorSpan headline = new BackgroundColorSpan(Color.BLUE);
                span.setSpan(headline, position, position + line.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
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

    public void transpose(int transpose) {
        String text = getText().toString();
        int i = 0;
        for (String line : text.split("\n", -1)) {
            Chord.Line cLine = Chord.parseLine(line);
            for (String token : cLine.tokens) {
                int additional = 0;
                Chord chord = Chord.chordFromString(token);
                if (chord.isValid() && cLine.isChordLine) {
                    chord.transpose(transpose);
                    String c = chord.toString();
                    final String before = i >= 0 ? text.substring(0, i) : "";
                    final int endIndex = i + token.length();
                    String after = endIndex < text.length() ? text.substring(endIndex) : "";
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

    public int addEmptyLinesBeforeChords() {
        final String[] lines = getText().toString().split("\n", -1);
        List<String> newLines = new ArrayList<>();

        int addedLines = 0;
        boolean lastLineWasEmpty = true;
        for (String line : lines) {
            if (!lastLineWasEmpty && Chord.parseLine(line).isChordLine) {
                newLines.add("");
                addedLines++;
            }
            newLines.add(line);
            lastLineWasEmpty = line.isEmpty();
        }

        setText(TextUtils.join("\n", newLines));
        return addedLines;
    }

    private boolean multipleSubsequentEmptyLines(String[] lines) {
        boolean lastLineWasEmpty = false;
        for (String line : lines) {
            if (line.isEmpty() && lastLineWasEmpty) {
                return true;
            }
            lastLineWasEmpty = line.isEmpty();
        }
        return false;
    }

    private boolean keepLine(boolean currentLineIsEmpty, boolean lastLineWasEmpty, boolean removeSingleEmptyLines) {
        if (!currentLineIsEmpty) {
            return true;
        } else if (removeSingleEmptyLines) {
            return false;
        } else if (lastLineWasEmpty) {
            return false;
        } else {
            return true;
        }
    }

    public int eliminateEmptyLines() {
        final String[] lines = getText().toString().split("\n", -1);
        final boolean removeSingleEmptyLines = !multipleSubsequentEmptyLines(lines);
        List<String> newLines = new ArrayList<>();

        int removedLines = 0;
        boolean lastLineWasEmpty = false;
        for (String line : lines) {
            if (keepLine(line.isEmpty(), lastLineWasEmpty, removeSingleEmptyLines)) {
                newLines.add(line);
            } else {
                removedLines++;
            }
            lastLineWasEmpty = line.isEmpty();
        }

        setText(TextUtils.join("\n", newLines));
        return removedLines;
    }

    private boolean isInScrollView() {
        return (getParent() instanceof ScrollView);
    }

    public void scrollTo(int x, int y) {
        if (isInScrollView()) {
            //ignore all scroll attempts.
        } else {
            super.scrollTo(x, y);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (mIsEditable || !isInScrollView()) {
            return super.onTouchEvent(event);
        } else {
            //Ignore touches, however return false to give parent (e.g. ScrollView) oportunity to handle e.g. scroll.
            return false;
        }
    }
}
