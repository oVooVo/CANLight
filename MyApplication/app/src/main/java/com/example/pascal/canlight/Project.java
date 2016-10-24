package com.example.pascal.canlight;

import android.content.Context;
import android.nfc.Tag;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.realtime.internal.event.ParcelableEventList;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


//TODO youtube video change is applied on exit/enter EditActivity

/**
 * Created by pascal on 02.10.16.
 */
public class Project implements Parcelable {
    private static final String TAG = "Project";
    private final List<Song> mSongs;

    public Project() {
        mSongs = new ArrayList<>();
    }

    public Project(Parcel in) {
        this();
        readFromParcel(in);
    }

    public List<Song> getSongs() {
        return mSongs;
    }

    public int findSong(String name) {
        for (int i = 0; i < mSongs.size(); ++i) {
            if (mSongs.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void addSong(Song song) {
        mSongs.add(song);
        songListChanged();
    }

    public int addSong(String name) {
        Song song = new Song(name);
        mSongs.add(song);
        songListChanged();
        return mSongs.size() - 1;
    }

    public boolean renameSong(int position, String newName) {
        mSongs.get(position).setName(newName);
        songListChanged();
        return true;
    }

    public Song getSong(int position) {
        return mSongs.get(position);
    }
    public int getIndexOf(Song song) {
        for (int i = 0; i < mSongs.size(); ++i) {
            if (mSongs.get(i) == song) {
                return i;
            }
        }
        throw new AssertionFailedError();
    }

    public void removeSong(int position) {
        mSongs.remove(position);
        songListChanged();
    }

    private static final String FILENAME = "project";
    public void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            final String data = toJson().toString();
            fos.write(data.getBytes());
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
    }

    public void load(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);

            final JSONObject o;
            String data;
            try {
                data = IOUtils.toString(fis);
            } catch (IOException e) {
                Log.d(TAG, "tostring failed");
                data = "";
            }
            Log.d(TAG, "data = " + data);
            try {
                Log.d(TAG, "json data: " + data);
                o = new JSONObject(data);
                mSongs.clear();
                fromJson(o);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "read json error");
                //throw new AssertionFailedError();
            }
        } catch (FileNotFoundException e) {
            // that's okay, no file to restore, maybe it's the first start.
        }
    }

    public void setSong(int currentEditPosition, Song song) {
        mSongs.set(currentEditPosition, song);
        songListChanged();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(mSongs.toArray(new Song[mSongs.size()]), flags);
    }

    public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>() {
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    private void readFromParcel(Parcel in) {
        // songs must be final!
        mSongs.clear();
        for (Song song : in.createTypedArray(Song.CREATOR)) {
            mSongs.add(song);
        }
    }

    public JSONObject toJson() {
        boolean[] filter = new boolean[mSongs.size()];
        Arrays.fill(filter, true);
        return toJson(filter);
    }

    public JSONObject toJson(boolean[] filter) {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < mSongs.size(); ++i) {
                if (filter[i]) {
                    array.put(mSongs.get(i).toJson());
                }
            }
            object.put("items", array);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return object;
    }

    public void fromJson(JSONObject object) {
        try {
            final JSONArray array = object.getJSONArray("items");
            for (int i = 0; i < array.length(); ++i) {
                Song s = Song.fromJson(array.getJSONObject(i));
                mSongs.add(s);
            }
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
    }

    public interface OnSongListChangedListener {
        void onSongListChanged();
    }
    private OnSongListChangedListener onSongListChangedListener;
    void setOnSongListChangedListener(OnSongListChangedListener l) {
        onSongListChangedListener = l;
    }
    private void songListChanged() {
        if (onSongListChangedListener != null) {
            onSongListChangedListener.onSongListChanged();
        }
    }
    
    public void renameGroup(final String oldName, final String newName) {
        for (Song song : mSongs) {
            Set<String> groups = song.getGroups();
            if (groups.remove(oldName)) {
                groups.add(newName);
                song.setGroups(groups);
            }
        }
        songListChanged();
    }

}
