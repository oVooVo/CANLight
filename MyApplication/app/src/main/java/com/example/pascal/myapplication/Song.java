package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by pascal on 06.10.16.
 */
public class Song implements Parcelable {
    private String name;
    private String pattern;
    private double scrollRate;

    public Song(String name) {
        this.name = name;
        this.pattern = "";
        this.scrollRate = 1;
    }

    public Song(String name, String pattern, double scrollRate) {
        this.name = name;
        this.pattern = pattern;
        this.scrollRate = scrollRate;
    }

    public Song(Parcel in) {
        name = in.readString();
        pattern = in.readString();
        scrollRate = in.readDouble();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setScrollRate(double scrollRate) {
        this.scrollRate = scrollRate;
    }

    public String getName() {
        return this.name;
    }

    public String getPattern() {
        return this.pattern;
    }

    public double getScrollRate() {
        return this.scrollRate;
    }

    public static Song fromJson(JSONObject o) {
        String name;
        String pattern;
        double scrollRate;
        try {
            name = o.getString("name");
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        try {
            pattern = o.getString("pattern");
        } catch (JSONException e) {
            // ignore. its okay.
            pattern = "";
        }
        try {
            scrollRate = o.getDouble("scrollRate");
        } catch (JSONException e) {
            // ignore. its okay.
            scrollRate = 1;
        }
        return new Song(name, pattern, scrollRate);
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("name", getName());
            o.put("pattern", getPattern());
            o.put("scrollRate", getScrollRate());
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return o;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(pattern);
        dest.writeDouble(scrollRate);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static class SongAdapter extends ArrayAdapter<Song> {
        SongAdapter(Context context, int res, List<Song> list) {
            super(context, res, list);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setTextColor(Color.BLUE);
            textView.setText(getItem(position).getName());
            return view;
        }
    }
}
