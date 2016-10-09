package com.example.pascal.canlight;

import android.os.Handler;
import android.widget.TextView;

/**
 * Created by pascal on 06.10.16.
 */
public class AutoScroller {
    private final TextView textView;
    double scrollBuffer = 0;
    double scrollRate = 2;
    boolean active = false;
    final Handler handler;

    public AutoScroller(TextView view) {
        textView = view;
        handler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                if (active) {
                    final double actualScrollRate = scrollRate * textView.getTextSize() / 18.0;
                    scrollBy(actualScrollRate);
                    if (!textView.canScrollVertically(1)) {
                        stopAutoScroll();
                    }
                }
                handler.postDelayed(this, 50);
            }
        }.run();
    }


    public void startAutoScroll() {
        active = true;
    }

    public void stopAutoScroll() {
        active = false;
        scrollBuffer = 0;
    }

    public void setAutoScrollRate(double dy) {
        scrollRate = dy;
    }

    void scrollBy(double y) {
        scrollBuffer += y;
        int integer = (int) scrollBuffer;
        scrollBuffer -= integer;
        textView.scrollBy(0, integer);
    }

    public boolean isPlaying() {
        return active;
    }

}