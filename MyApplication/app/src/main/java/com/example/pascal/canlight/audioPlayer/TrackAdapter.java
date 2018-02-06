package com.example.pascal.canlight.audioPlayer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.BaseAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
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
    abstract String getServiceName();
    void deinit() {}

    public static class Track implements Parcelable {
        final private String TAG = "TrackAdapter.Track";

        public Track(String label, String service, String id) {
            this.label = label;
            this.service = service;
            this.id = id;
        }
        public Track() {
            this.label = "";
            this.service = "";
            this.id = "";
        }

        public String label;
        public String service;
        public String id;

        @Override
        public int describeContents() {
            return 0;
        }

        Track(Parcel p) {
            label = p.readString();
            service = p.readString();
            id = p.readString();
        }

        public void fromJSON(JSONObject o) {
            try {
                label = o.getString("label");
                service = o.getString("service");
                id = o.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }

        public static final Parcelable.Creator<Track> CREATOR
                = new Parcelable.Creator<Track>() {
            public Track createFromParcel(Parcel in) {
                return new Track(in);
            }

            public Track[] newArray(int size) {
                return new Track[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(label);
            parcel.writeString(service);
            parcel.writeString(id);
        }

        public JSONObject toJson() {
            JSONObject o = new JSONObject();

            try {
                o.put("label", label);
                o.put("service", service);
                o.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Unexpected JSONException.");
                throw new RuntimeException();
            }

            return o;
        }
    }
}
