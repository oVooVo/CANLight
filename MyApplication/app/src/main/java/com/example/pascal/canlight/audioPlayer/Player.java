package com.example.pascal.canlight.audioPlayer;

import android.view.View;
import android.widget.Button;

import com.example.pascal.canlight.Project;
import com.spotify.sdk.android.player.Metadata;

import java.util.Locale;

/**
 * Created by pascal on 20.10.16.
 */
public abstract class Player {

    interface OnTimeLabelsTextChangeListener {
        void onTimeLabelsTextChange(String elapsed, String remaining);
    }
    interface OnSongChangeListener {
        void onSongChange(String label, long duration);
    }
    interface OnPlayStateChangeListener {
        void onPlayStateChange(boolean isPlaying);
    }
    interface OnCurrentPositionChangeListener {
        void onCurrentPositionChange(long pos);
    }

    OnTimeLabelsTextChangeListener mOnTimeLabelsTextChange;
    OnSongChangeListener mOnSongChange;
    OnPlayStateChangeListener mOnPlayStateChange;
    OnCurrentPositionChangeListener mOnCurrentPositionChange;

    public void setOnTimeLabelsTextChangeListener(OnTimeLabelsTextChangeListener c)
    {
        mOnTimeLabelsTextChange = c;
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
        return String.format(Locale.getDefault(), "%2d:%02d.%01d", min, sec, ms / 100);
    }

    protected void updateTimeLabels(long position, long durationMs) {
        if (mOnTimeLabelsTextChange != null) {
            mOnTimeLabelsTextChange.onTimeLabelsTextChange(
                    formatTime(position),
                    formatTime(position - durationMs));
        }
    }

    protected void updateSong(String label, long duration) {
        if (mOnSongChange != null) {
            if (label == null) {
                label = "No Song";
            }
            mOnSongChange.onSongChange(label, duration);
        }
    }

    protected void updatePlayState(boolean isPlaying) {
        if (mOnPlayStateChange != null) {
            mOnPlayStateChange.onPlayStateChange(isPlaying);
        }
    }

    protected void updateCurrentPosition(long pos) {
        if (mOnCurrentPositionChange != null) {
            mOnCurrentPositionChange.onCurrentPositionChange(pos);
        }
    }

    public abstract void pause();
    public abstract void togglePlayPause();
    public abstract void seekLastPosition();
    public abstract void seek(long pos);
    public abstract void init(String id, long position);
    public abstract void deinit();
}
