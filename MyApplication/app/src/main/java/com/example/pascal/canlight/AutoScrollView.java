package com.example.pascal.canlight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by pascal on 17.10.16.
 */
public class AutoScrollView extends ScrollView {

    private boolean mAutoScrollIsActive = false;
    private Handler mHandler;
    private double mScrollRate = 10;
    private TextView textView;
    private static final double FACTOR = 2.0;
    private double mScrollBuffer = 0.0;
    private final Paint mPaint;
    private int mLinePosY = 0;
    private int mVerticalScroll = 0;
    private final GestureDetector mGestureDetector;
    private OnAutoScrollStoppedListener mOnAutoScrollStoppedListener;

    public AutoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        setSmoothScrollingEnabled(false);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (mAutoScrollIsActive) {
                    if (Math.abs(distanceY) < 150) {
                        // use own fancy-red line implementation
                        scrollBy((int) distanceY);
                    } else {
                        // ghost event. no idea where it comes from but it distracts the scroll very much
                        // since it's distanceY is enormous (thogh some good events have distanceY > 150,
                        // unfortunately they're ignored but that's okay.
                    }
                    return true;
                } else {
                    // use default implementation
                    return false;
                }
            }
        });
    }

    public void onFinishInflate() {
        textView = (TextView) findViewById(R.id.editText);
        mHandler = new Handler();
        new Runnable() {
            @Override
            public void run() {
                if (mAutoScrollIsActive) {
                    mScrollBuffer = 0;
                    double actualScrollRate = (mScrollRate / FACTOR) * textView.getTextSize() / 18.0;
                    actualScrollRate += mScrollBuffer;
                    final int actualScrollRateInteger = (int) actualScrollRate;
                    mScrollBuffer = actualScrollRate - actualScrollRateInteger;
                    if (!scrollBy(actualScrollRateInteger)) {
                        endAutoScroll();
                    }
                }
                mHandler.postDelayed(this, (int) (50 / FACTOR));
            }
        }.run();
        super.onFinishInflate();
    }

    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        mVerticalScroll = scrollStart();
        super.onSizeChanged(w, h, oldW, oldH);
    }

    public boolean onTouchEvent(MotionEvent event) {
        // GestureDetector.onTouchEvent shall always be called regardless mAutoScrollIsActive's value.
        if (mGestureDetector.onTouchEvent(event) && mAutoScrollIsActive) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void startAutoScroll() {
        mAutoScrollIsActive = true;
    }

    public void endAutoScroll() {
        mAutoScrollIsActive = false;
        mVerticalScroll = scrollStart();
        if (mOnAutoScrollStoppedListener != null) {
            mOnAutoScrollStoppedListener.onAutoScrollStopped();
        }
    }

    public boolean isActive() {
        return mAutoScrollIsActive;
    }

    public void setScrollRate(double rate) {
        mScrollRate = rate;
    }

    private boolean scrollBy(int dy) {
        final int max = scrollEnds();
        mVerticalScroll += dy;
        if (mVerticalScroll >= max) {
            mVerticalScroll = max;
        }
        scrollTo(0, mVerticalScroll);
        return mVerticalScroll < max;
    }

    private int scrollStart() {
        return -computeVerticalScrollExtent() / 2;
    }

    private int scrollEnds() {
        return computeVerticalScrollRange() - computeVerticalScrollExtent() / 2;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAutoScrollIsActive) {
            canvas.drawLine(0, mLinePosY, canvas.getWidth(), mLinePosY, mPaint);
        }
    }

    public void scrollTo(int x, int y) {
        if (mAutoScrollIsActive) {
            mVerticalScroll = Math.max(scrollStart(), Math.min(scrollEnds(), y));
            y = Math.max(0, Math.min(computeVerticalScrollRange() - computeVerticalScrollExtent(), y));
            mLinePosY = mVerticalScroll + getHeight() / 2;
            super.scrollTo(0, y);
            //Log.d("scroll", "scroll to " + mVerticalScroll);
            invalidate();
        } else {
            mVerticalScroll = scrollStart();
            super.scrollTo(0, y);
        }
    }

    interface OnAutoScrollStoppedListener {
        void onAutoScrollStopped();
    }
    public void setOnAutoScrollStoppedListener(OnAutoScrollStoppedListener listener) {
        mOnAutoScrollStoppedListener = listener;
    }

    public void fling(int velocityY) {
        if (!mAutoScrollIsActive) {
            super.fling(velocityY);
        }
    }


}
