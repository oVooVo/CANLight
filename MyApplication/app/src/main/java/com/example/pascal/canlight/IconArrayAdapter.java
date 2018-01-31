package com.example.pascal.canlight;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by pascal on 21.10.16.
 */
public abstract class IconArrayAdapter extends ArrayAdapter<String> {
    public IconArrayAdapter(Context context, List<String> items) {
        super(context, R.layout.list_item_icon, R.id.textView, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ImageView imageView = view.findViewById(R.id.iconView);
        setIcon(imageView, position);
        return view;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    protected abstract void setIcon(ImageView view, int position);
}
