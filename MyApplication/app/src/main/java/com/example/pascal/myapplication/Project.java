package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import junit.framework.AssertionFailedError;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by pascal on 02.10.16.
 */
public class Project {

    private ArrayList<String> names;
    private Map<String, String> patterns;
    private ArrayAdapter<String> itemAdapter;

    public Project(Context context) {
        names = new ArrayList<>();
        patterns = new TreeMap<>();
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
        patterns.put("", "");
        itemAdapter.notifyDataSetChanged();
        return names.size() - 1;
    }

    public boolean renameItem(int position, String newName) {
        if (names.contains(newName)) {
            return false;
        } else {
            final String oldName = name(position);
            final String pattern = patterns.get(oldName);
            names.set(position, newName);
            patterns.remove(oldName);
            patterns.put(newName, pattern);
            itemAdapter.notifyDataSetChanged();
            return true;
        }
    }

    public void setPattern(int position, String pattern) {
        patterns.put(name(position), pattern);
    }

    public String name(int position) {
        return names.get(position);
    }

    public String pattern(int position) {
        return patterns.get(name(position));
    }

    public void remove(int position) {
        patterns.remove(name(position));
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
}
