package com.example.pascal.canlight;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Created by pascal on 02.10.16.
 */
public class Project implements Parcelable {
    private static final String TAG = "Project";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 43;
    private final List<Song> mSongs = new ArrayList<>();

    public Project() {
    }

    public Project(Parcel in) {
        readFromParcel(in);
    }

    public List<Song> getSongs() {
        return mSongs;
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
                data = "";
            }
            try {
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

    private static final File createUniqueFile(Activity activity) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(activity, "Cannot write external storage.", Toast.LENGTH_LONG);
            return null;
        } else {
            ActivityCompat.requestPermissions(activity, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            final String nameTemplate = String.format("Collection_%s_%%04d.can", new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()));

            if (directory.list() == null) {
                Log.w(TAG, "Cannot read or write in " + directory + ". Maybe not sufficient permissions?");
                return null;
            }
            List<String> files = Arrays.asList(directory.list());

            int i = 0;
            String candidate = "";
            do {
                candidate = String.format(nameTemplate, i);
                i++;
            } while (files.contains(candidate));

            File file = new File(directory, candidate);
            try {
                if (!file.createNewFile()) {
                    Log.w(TAG, "Cannot access file");
                    Toast.makeText(activity, "Cannot access file " + file.getAbsolutePath(), Toast.LENGTH_LONG);
                    return null;
                } else {
                    Log.w(TAG, "Created file");
                    return file;
                }
            } catch (IOException e) {
                Log.w(TAG, "Something went wrong.");
                return null;
            }
        }
    }

    void exportProject(Activity activity) {
        Log.i(TAG, "Save file in external storage");
        File file = createUniqueFile(activity);

        if (file != null) {
            Log.i(TAG, "file name: " + file.getAbsolutePath());
            try {
                boolean j = file.mkdirs();
                Log.i(TAG, "" + file.canWrite() + ", " + j);

                FileOutputStream fos = new FileOutputStream(file);

                try {
                    fos.write(toJson().toString(4).getBytes());
                    Log.i(TAG, "Wrote data to file");
                } catch (JSONException e) {
                    Log.w(TAG, "Unexpected JSON error.");
                    throw new AssertionFailedError();
                } catch (IOException e) {
                    Log.w(TAG, "Writing data failed.");
                    Toast.makeText(activity, "Cannot write file.", Toast.LENGTH_LONG).show();
                } finally {
                    try {
                        fos.close();
                        Toast.makeText(activity, "Wrote file: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Closed file output stream");
                    } catch (IOException e) {
                        Log.wtf(TAG, "Cannot close file output stream.");
                        throw new AssertionFailedError();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();

                Log.w(TAG, "Cannot find file " + file.getAbsolutePath());
                Toast.makeText(activity, "Cannot find file " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, "Could not get unique filename.");
        }
    }

    void importProject(Activity activity) {
        Log.i(TAG, "Load file from external storage");
        final String filename = "/storage/emulated/0/Documents/importme.can";
        File file = new File(filename);

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot find file: " + e.getMessage());
            Toast.makeText(activity, "The file \"" + filename + "\" cannot be found.", Toast.LENGTH_LONG).show();
            return;
        }
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot read file: " + e.getMessage());
            Toast.makeText(activity, "Cannot read file \"" + filename + "\".", Toast.LENGTH_LONG).show();
        }

        JSONObject json;
        try {
            json = new JSONObject(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load JSON");
            Toast.makeText(activity, "The file \"" + filename + "\" is not valid.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Add songs from json");
        Toast.makeText(activity, "Add songs from \"" + filename + "\" to this project.", Toast.LENGTH_LONG).show();
        fromJson(json);
    }
}
