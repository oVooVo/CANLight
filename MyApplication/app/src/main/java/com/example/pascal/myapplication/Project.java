package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by pascal on 02.10.16.
 */
public class Project {
    private ArrayList<Song> songs;
    private Song.SongAdapter songsAdapter;
    private Context context;

    public Project(Context context) {
        songs = new ArrayList<>();
        this.context = context;
        songsAdapter = new Song.SongAdapter(context, android.R.layout.simple_list_item_1, songs);
    }

    public Song.SongAdapter itemAdapter() {
        return songsAdapter;
    }

    public int addItem() {
        Song song = new Song(getDefaultItemName());
        songs.add(song);
        songsAdapter.notifyDataSetChanged();
        return songs.size() - 1;
    }

    public boolean renameItem(int position, String newName) {
        songs.get(position).setName(newName);
        songsAdapter.notifyDataSetChanged();
        return true;
    }

    public Song getSong(int position) {
        return songs.get(position);
    }

    public void remove(int position) {
        songs.remove(position);
        songsAdapter.notifyDataSetChanged();
    }

    public String getDefaultItemName() {
        return context.getString(R.string.default_song_name);
    }

    public JSONObject toJson() {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        try {
            for (Song s : songs) {
                array.put(s.toJson());
            }
            object.put("items", array);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return object;
    }

    public void fromJson(JSONObject object) {
        songs.clear();
        try {
            final JSONArray array = object.getJSONArray("items");
            for (int i = 0; i < array.length(); ++i) {
                Song s = Song.fromJson(array.getJSONObject(i));
                songs.add(s);
            }
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
    }

    private static final String FILENAME = "project";
    public void save() {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            final String data = toJson().toString();
            fos.write(data.getBytes());
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
    }

    public void load() {
        try {
            FileInputStream fis = context.openFileInput(FILENAME);

            final JSONObject o;
            try {
                final String data = IOUtils.toString(fis);
                o = new JSONObject(data);
                fromJson(o);
            } catch (Exception e) {
                throw new AssertionFailedError();
            }
        } catch (FileNotFoundException e) {
            // that's okay, no file to restore, maybe it's the first start.
        }
    }

    public void setSong(int currentEditPosition, Song song) {
        songs.set(currentEditPosition, song);
    }
}
