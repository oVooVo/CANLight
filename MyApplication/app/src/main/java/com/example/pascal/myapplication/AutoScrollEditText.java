package com.example.pascal.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pascal on 06.10.16.
 */
public class AutoScrollEditText extends EditText {

    Timer timer;
    boolean active = false;
    double scrollBuffer = 0;
    double scrollRate = 2;

    public AutoScrollEditText(final Context context, AttributeSet attrs) {
        super(context, attrs);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (active) {
                    scrollBy(scrollRate);
                    if (getScrollY() >= computeVerticalScrollRange() - getHeight()) {
                        stopAutoScroll();
                    }
                }
            }
        }, 0, 50);
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
        scrollBy(0, integer);
    }
}
