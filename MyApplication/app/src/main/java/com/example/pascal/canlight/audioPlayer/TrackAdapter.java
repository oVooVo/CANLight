package com.example.pascal.canlight.audioPlayer;

import android.widget.BaseAdapter;

/**
 * Created by pascal on 20.10.16.
 */
public abstract class TrackAdapter extends BaseAdapter {
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
}
