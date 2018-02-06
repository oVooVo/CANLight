package com.example.pascal.canlight;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import com.example.pascal.canlight.audioPlayer.TrackAdapter;
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
    private MidiProgram mMidiProgram;
    private int mColor;
    public static final double DEFAULT_PATTERN_TEXT_SIZE = 18;
    private double patternTextSize = DEFAULT_PATTERN_TEXT_SIZE;
    private TrackAdapter.Track mTrack;

    public Song(String name) {
        mName = name;
        mUninitalizedPattern = true;
        mGroups = new HashSet<>();
        mMidiProgram = new MidiProgram();
        mColor = 0;
        mTrack = new TrackAdapter.Track();
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
        mTrack = in.readParcelable(TrackAdapter.Track.class.getClassLoader());
        mMidiProgram = in.readParcelable(MidiProgram.class.getClassLoader());
        mColor = in.readInt();
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
        TrackAdapter.Track track = song.getTrack();
        MidiProgram midiCommand = song.getMidiProgram();
        int color = song.getColor();

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
            track.fromJSON(o.getJSONObject("track"));
        } catch (JSONException e) {} // ignore. it's okay.
        try {
            midiCommand.fromJSON(o.getJSONObject("MidiProgram"));
        } catch (JSONException e) {} // ignore. it's okay.
        try {
            color = o.getInt("color");
            System.out.println("Restored Color: " + color);
        } catch (JSONException e) { } // ignore. it's okay.

        song.setPattern(pattern);
        song.setScrollRate(scrollRate);
        song.setName(name);
        song.setPatternTextSize(patternTextSize);
        song.setGroups(groups);
        song.setTrack(track);
        song.setMidiCommand(midiCommand);
        song.setColor(color);
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
            o.put("track", mTrack);
            o.put("MidiProgram", getMidiProgram().toJSON());
            o.put("color", getColor());
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
        dest.writeParcelable(mTrack, 0);
        dest.writeParcelable(mMidiProgram, 0);
        dest.writeInt(mColor);
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

    public void setTrack(TrackAdapter.Track track) {
        mTrack = track;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public TrackAdapter.Track getTrack() {
        return mTrack;
    }
}
