package com.example.pascal.canlight.audioPlayer;

import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by pascal on 20.10.16.
 */
public abstract class TrackAdapter extends BaseAdapter {

    public abstract boolean readyToUse();

    interface OnResultsArrivedListener {
        void onResultsArrived(List<?> results);
    }

    private OnResultsArrivedListener mOnResultsArrivedListener;

    abstract void search(String key);
    abstract String getId(int position);
    abstract String getLabel(int position);


    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    void setOnResultsArrivedListener(OnResultsArrivedListener c) {
        mOnResultsArrivedListener = c;
    }

    void onResultsArrived(List<?> results) {
        if (mOnResultsArrivedListener != null) {
            mOnResultsArrivedListener.onResultsArrived(results);
        }
    }

    abstract int getIcon();
    abstract String getName();
    void deinit() {}


}
