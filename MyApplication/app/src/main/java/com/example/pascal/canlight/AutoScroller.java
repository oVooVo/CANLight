package com.example.pascal.canlight;

import android.os.Handler;

/**
 * Created by pascal on 06.10.16.
 */
public class AutoScroller {
    private final ChordPatternEdit textView;
    double mScrollBuffer = 0;
    double scrollRate = 2;
    boolean mActive = false;
    final Handler mHandler;
    static final double FACTOR = 2.0;

    public AutoScroller(final ChordPatternEdit view) {
        textView = view;
        mHandler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                if (mActive) {
                    double actualScrollRate = (scrollRate / FACTOR) * textView.getTextSize() / 18.0;
                    actualScrollRate += mScrollBuffer;
                    final int actualScrollRateInteger = (int) actualScrollRate;
                    mScrollBuffer = actualScrollRate - actualScrollRateInteger;
                    int y = view.getVerticalScroll();
                    if (view.scrollEnds() <= y) {
                        stopAutoScroll();
                    } else {
                        y += actualScrollRateInteger;
                        view.scrollTo(view.getScrollX(), y);
                    }
                }
                mHandler.postDelayed(this, (int) (50 / FACTOR));
            }
        }.run();
    }


    public void startAutoScroll() {
        mActive = true;
        textView.setAutoScrollIsActive(true);
    }

    public void stopAutoScroll() {
        mActive = false;
        mScrollBuffer = 0;
        textView.setAutoScrollIsActive(false);
    }

    public void setAutoScrollRate(double dy) {
        scrollRate = dy;
    }

    public boolean isPlaying() {
        return mActive;
    }
}

