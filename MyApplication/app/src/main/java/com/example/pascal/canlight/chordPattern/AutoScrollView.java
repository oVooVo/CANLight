package com.example.pascal.canlight.chordPattern;

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

import com.example.pascal.canlight.R;

/**
 * Created by pascal on 17.10.16.
 */
public class AutoScrollView extends ScrollView {
    private static final String TAG = "AutoScrollView";

    private boolean mAutoScrollIsActive = false;
    private Handler mHandler;
    private double mScrollRate;
    private TextView textView;
    private static final double FACTOR = 2.0;
    private final Paint mPaint;
    private double mLinePosY = 0;
    private double mVerticalScroll = 0;
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
                    double actualScrollRate = (mScrollRate / FACTOR) * textView.getTextSize() / 18.0;
                    if (!scrollBy(actualScrollRate)) {
                        endAutoScroll();
                        mVerticalScroll = scrollStart();
                    }
                }
                mHandler.postDelayed(this, (int) (50 / FACTOR));
            }
        }.run();
        super.onFinishInflate();
    }

    public void onSizeChanged(int w, int h, int oldW, int oldH) {
	// adjust scrolling in order to avoid jumps
        mVerticalScroll += (oldH - h) / 2;
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

    private boolean scrollBy(double dy) {
        final double max = scrollEnds();
        mVerticalScroll += dy;
        if (mVerticalScroll >= max) {
            mVerticalScroll = max;
        }
        scrollTo(mVerticalScroll);
        return mVerticalScroll < max;
    }

    private double scrollStart() {
        return -computeVerticalScrollExtent() / 2.0;
    }

    private double scrollEnds() {
        return computeVerticalScrollRange() - computeVerticalScrollExtent() / 2.0;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAutoScrollIsActive) {
            canvas.drawLine(0, (int) mLinePosY, canvas.getWidth(), (int) mLinePosY, mPaint);
        }
    }

    public void scrollTo(double y) {
        if (mAutoScrollIsActive) {
            mVerticalScroll = Math.max(scrollStart(), Math.min(scrollEnds(), y));
            y = Math.max(0, Math.min(computeVerticalScrollRange() - computeVerticalScrollExtent(), y));
            mLinePosY = mVerticalScroll + getHeight() / 2.0;
            super.scrollTo(0, (int) y);
            invalidate();
        } else {
            mVerticalScroll = scrollStart();
            super.scrollTo(0, (int) y);
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
        } else {
            // dont't fling. It confuses auto scroll and is counter-intuitive.
        }
    }


}
