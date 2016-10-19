package com.example.pascal.canlight;

import android.os.Parcel;
import android.os.Parcelable;
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

    private String name;
    private String pattern = "";
    private double scrollRate = 2;
    private boolean uninitalizedPattern;
    private Set<String> mGroups;
    private String mSpotifyTrackDisplayName;
    private String mSpotifyTrackId;
    public static final double DEFAULT_PATTERN_TEXT_SIZE = 18;
    private double patternTextSize = DEFAULT_PATTERN_TEXT_SIZE;

    public Song(String name) {
        this.name = name;
        this.uninitalizedPattern = true;
        this.mGroups = new HashSet<>();
    }

    public Song(Parcel in) {
        name = in.readString();
        pattern = in.readString();
        scrollRate = in.readDouble();
        uninitalizedPattern = in.readInt() != 0;
        patternTextSize = in.readDouble();
        List<String> groups = new ArrayList<>();
        in.readStringList(groups);
        mGroups = new HashSet<>(groups);
        mSpotifyTrackId = in.readString();
        mSpotifyTrackDisplayName = in.readString();
    }

    public Set<String> getGroups() {
        return mGroups;
    }

    public void setGroups(Set<String> groups) {
        mGroups = groups;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String pattern) {
        this.uninitalizedPattern = (pattern == null);
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
        Song song = new Song("");
        String name;
        String pattern = song.getPattern();
        double patternTextSize = song.getPatternTextSize();
        double scrollRate = song.getScrollRate();
        Set<String> groups = new HashSet<>();
        String spotifyTrackId = song.getSpotifyTrackId();
        String spotifyTrackDisplayName = song.getSpotifyTrackDisplayName();

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
            spotifyTrackId = o.getString("SpotifyTrackId");
            spotifyTrackDisplayName = o.getString("SpotifyTrackDisplayName");
        } catch (JSONException e) {} // ignore. it's okay.

        song.setPattern(pattern);
        song.setScrollRate(scrollRate);
        song.setName(name);
        song.setPatternTextSize(patternTextSize);
        song.setGroups(groups);
        song.setSpotifyTrack(spotifyTrackId, spotifyTrackDisplayName);
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
            o.put("SpotifyTrackDisplayName", getSpotifyTrackDisplayName());
            o.put("SpotifyTrackId", getSpotifyTrackId());
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return o;
    }

    public boolean getPatternIsUninitialized() {
        return uninitalizedPattern;
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
        dest.writeInt(uninitalizedPattern ? 1 : 0);
        dest.writeDouble(patternTextSize);
        dest.writeStringList(new ArrayList<>(mGroups));
        dest.writeString(mSpotifyTrackId);
        dest.writeString(mSpotifyTrackDisplayName);
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

    public void setSpotifyTrack(String id, String displayName) {
        mSpotifyTrackId = id;
        mSpotifyTrackDisplayName = displayName;
    }

    public String getSpotifyTrackId() {
        return mSpotifyTrackId;
    }

    public String getSpotifyTrackDisplayName() {
        return mSpotifyTrackDisplayName;
    }

}
