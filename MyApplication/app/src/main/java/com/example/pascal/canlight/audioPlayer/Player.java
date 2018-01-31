package com.example.pascal.canlight.audioPlayer;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.pascal.canlight.Project;
import com.example.pascal.canlight.R;
import com.spotify.sdk.android.player.Metadata;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by pascal on 20.10.16.
 */
public abstract class Player {
    private static final String TAG = "Player";

    interface OnSongChangeListener {
        void onSongChange(String label, long duration);
    }
    interface OnPlayStateChangeListener {
        void onPlayStateChange(boolean isPlaying);
    }
    interface OnCurrentPositionChangeListener {
        void onCurrentPositionChange(long pos, String elapsed, String remaining);
    }

    private OnSongChangeListener mOnSongChange;
    private OnPlayStateChangeListener mOnPlayStateChange;
    private OnCurrentPositionChangeListener mOnCurrentPositionChange;
    private final Handler mHandler;
    private final Context mContext;

    public Player(Context context) {
        mContext = context;
        mHandler = new Handler();
    }

    public void setOnSongLabelChangeListener(OnSongChangeListener c)
    {
        mOnSongChange = c;
    }

    public void setOnPlayStateChangeListener(OnPlayStateChangeListener c)
    {
        mOnPlayStateChange = c;
    }

    public void setOnCurrentPositionChangeListener(OnCurrentPositionChangeListener c)
    {
        mOnCurrentPositionChange = c;
    }

    protected String formatTime(long ms) {
        boolean isNegative = false;
        if (ms < 0) {
            isNegative = true;
            ms = -ms;
        }
        long sec = ms / 1000;
        long min = sec / 60;
        sec %= 60;
        ms %= 1000;

        if (isNegative) {
            min = -min;
        }

        // ms / 100 is correct enough.
        return String.format(mContext.getString(R.string.player_time_format), min, sec, ms / 100);
    }

    protected void updateSong(@Nullable String label) {
        if (mOnSongChange != null) {
            if (label == null) {
                mOnSongChange.onSongChange(mContext.getString(R.string.no_song), 0);
            } else {
                mOnSongChange.onSongChange(label, getDuration());
            }
        }
    }

    protected void updatePlayState(boolean isPlaying) {
        if (mOnPlayStateChange != null) {
            mOnPlayStateChange.onPlayStateChange(isPlaying);
        }
    }

    protected void updateCurrentPosition(long pos) {
        if (mOnCurrentPositionChange != null) {
            final int duration = getDuration();
            mOnCurrentPositionChange.onCurrentPositionChange(pos,
                    formatTime(pos),
                    formatTime(pos - duration));
        }
    }

    public abstract void pause();
    public abstract void togglePlayPause();
    public abstract void seekLastPosition();
    public abstract void seek(long pos);
    public abstract void init(String id, long position);
    public abstract int getCurrentPosition();
    public abstract int getDuration();

    public void init() {
        new Runnable() {
            @Override
            public void run() {
                final int ms = getCurrentPosition();
                if (ms >= 0) {
                    updateCurrentPosition(ms);
                }
                mHandler.postDelayed(this, 20);
            }
        }.run();
    }

    public void deinit() {
        mHandler.removeCallbacksAndMessages(null);
    }
}
