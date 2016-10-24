package com.example.pascal.canlight;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.pascal.canlight.midi.MidiProgram;

import junit.framework.AssertionFailedError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by pascal on 06.10.16.
 */
public class Song implements Parcelable {
    private static final String TAG = "Song";
    private String mName;
    private String mPattern = "";
    private double mScrollRate = 2;
    private boolean mUninitalizedPattern;
    private Set<String> mGroups;
    private String mTrackLabel;
    private String mTrackId;
    private String mTrackService;
    private MidiProgram mMidiProgram;
    public static final double DEFAULT_PATTERN_TEXT_SIZE = 18;
    private double patternTextSize = DEFAULT_PATTERN_TEXT_SIZE;

    public Song(String name) {
        mName = name;
        mUninitalizedPattern = true;
        mGroups = new HashSet<>();
        mMidiProgram = new MidiProgram();
    }

    public Song(Parcel in) {
        mName = in.readString();
        mPattern = in.readString();
        mScrollRate = in.readDouble();
        mUninitalizedPattern = in.readInt() != 0;
        patternTextSize = in.readDouble();
        List<String> groups = new ArrayList<>();
        in.readStringList(groups);
        mGroups = new HashSet<>(groups);
        mTrackService = in.readString();
        mTrackId = in.readString();
        mTrackLabel = in.readString();
        mMidiProgram = in.readParcelable(MidiProgram.class.getClassLoader());
    }

    public Set<String> getGroups() {
        return mGroups;
    }

    public void setGroups(Set<String> groups) {
        mGroups = groups;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPattern(String pattern) {
        mUninitalizedPattern = (pattern == null);
        mPattern = pattern;
    }

    public void setScrollRate(double scrollRate) {
        mScrollRate = scrollRate;
    }

    public String getName() {
        return mName;
    }

    public String getPattern() {
        return mPattern;
    }

    public double getScrollRate() {
        return mScrollRate;
    }

    public MidiProgram getMidiProgram() {
        return mMidiProgram;
    }

    public void setMidiCommand(MidiProgram midiCommand) {
        mMidiProgram = midiCommand;
    }

    public static Song fromJson(JSONObject o) {
        Song song = new Song("");
        String name;
        String pattern = song.getPattern();
        double patternTextSize = song.getPatternTextSize();
        double scrollRate = song.getScrollRate();
        Set<String> groups = new HashSet<>();
        String trackId = song.getTrackId();
        String trackLabel = song.getTrackLabel();
        String trackService = song.getTrackService();
        MidiProgram midiCommand = song.getMidiProgram();

        try { name = o.getString("name"); } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        try { pattern = o.getString("pattern"); } catch (JSONException e) {} // ignore. it's okay.
        try { scrollRate = o.getDouble("scrollRate"); } catch (JSONException e) {} // ignore. it's okay.
        try { patternTextSize = o.getDouble("patternTextSize"); } catch (JSONException e) {} // ignore. it's okay.
        try {
            final JSONArray groupArray = o.getJSONArray("groups");
            for (int i = 0; i < groupArray.length(); ++i) {
                groups.add(groupArray.getString(i));
            }
        } catch (JSONException e) {} // ignore. it's okay.
        try {
            trackService = o.getString("TrackService");
            trackId = o.getString("TrackId");
            trackLabel = o.getString("TrackLabel");
        } catch (JSONException e) {} // ignore. it's okay.
        try {
            midiCommand.fromJson(o.getJSONObject("MidiProgram"));
        } catch (JSONException e) {} // ignore. it's okay.

        song.setPattern(pattern);
        song.setScrollRate(scrollRate);
        song.setName(name);
        song.setPatternTextSize(patternTextSize);
        song.setGroups(groups);
        song.setTrack(trackService, trackId, trackLabel);
        song.setMidiCommand(midiCommand);
        Log.d(TAG, "get midi command: " + midiCommand.toJson().toString());
        return song;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("name", getName());
            o.put("pattern", getPattern());
            o.put("scrollRate", getScrollRate());
            o.put("patternTextSize", getPatternTextSize());
            final JSONArray groupArray = new JSONArray();
            for (String groupName : getGroups()) {
                groupArray.put(groupName);
            }
            o.put("groups", groupArray);
            o.put("TrackService", getTrackService());
            o.put("TrackId", getTrackId());
            o.put("TrackLabel", getTrackLabel());
            o.put("MidiProgram", getMidiProgram().toJson());
            Log.d(TAG, "set midi command: " + getMidiProgram().toJson().toString());
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return o;
    }

    public boolean getPatternIsUninitialized() {
        return mUninitalizedPattern;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mPattern);
        dest.writeDouble(mScrollRate);
        dest.writeInt(mUninitalizedPattern ? 1 : 0);
        dest.writeDouble(patternTextSize);
        dest.writeStringList(new ArrayList<>(mGroups));
        dest.writeString(mTrackService);
        dest.writeString(mTrackId);
        dest.writeString(mTrackLabel);
        dest.writeParcelable(mMidiProgram, 0);
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

    public void setPatternTextSize(double s) {
        patternTextSize = s;
    }

    public double getPatternTextSize() {
        return patternTextSize;
    }

    public void setTrack(String service, String id, String label) {
        mTrackId = id;
        mTrackLabel = label;
        mTrackService = service;
    }

    public String getTrackId() {
        return mTrackId;
    }

    public String getTrackLabel() {
        return mTrackLabel;
    }

    public String getTrackService() {
        return mTrackService;
    }

}
