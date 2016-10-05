package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by pascal on 02.10.16.
 */
public class Project {

    private ArrayList<String> names;
    private ArrayList<String> patterns;
    private ArrayAdapter<String> itemAdapter;
    private Context context;

    public Project(Context context) {
        names = new ArrayList<>();
        patterns = new ArrayList<>();
        this.context = context;
        itemAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLUE);
                return view;
            }
        };
    }

    public ArrayAdapter<String> itemAdapter() {
        return itemAdapter;
    }

    public int addItem() {
        if (names.contains("")) throw new AssertionFailedError();

        names.add(getDefaultItemName());
        patterns.add("");
        itemAdapter.notifyDataSetChanged();
        return names.size() - 1;
    }

    public boolean renameItem(int position, String newName) {
        if (names.contains(newName)) {
            return false;
        } else {
            final String oldName = name(position);
            names.set(position, newName);
            itemAdapter.notifyDataSetChanged();
            return true;
        }
    }

    public void setPattern(int position, String pattern) {
        patterns.set(position, pattern);
    }

    public String name(int position) {
        return names.get(position);
    }

    public String pattern(int position) {
        return patterns.get(position);
    }

    public void remove(int position) {
        patterns.remove(position);
        names.remove(position);
        itemAdapter.notifyDataSetChanged();
    }

    public String getDefaultItemName() {
        final String template = "New Item";
        String itemName = template;
        int i = 0;
        while (names.contains(itemName)) {
            i++;
            itemName = template + " " + i;
        }

        return itemName;
    }

    public JSONObject toJson() {
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if (patterns.size() != names.size()) {
            throw new AssertionFailedError();
        }

        final int n = patterns.size();

        try {
            for (int i = 0; i < n; ++i) {
                JSONObject o = new JSONObject();
                o.put("name", name(i));
                o.put("pattern", pattern(i));
                array.put(o);
            }
            object.put("items", array);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return object;
    }

    public void fromJson(JSONObject object) {
        names.clear();
        patterns.clear();
        try {
            final JSONArray array = object.getJSONArray("items");
            for (int i = 0; i < array.length(); ++i) {
                JSONObject o2 = array.getJSONObject(i);
                names.add(o2.getString("name"));
                patterns.add(o2.getString("pattern"));
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
}
