package com.example.pascal.canlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class NewGroupActivity extends Activity {
    private static final String TAG = "NewGroupActivity";
    private Project mProject;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        mProject = getIntent().getParcelableExtra("project");
        List<String> songNames = new ArrayList<>(mProject.getSongs().size());
        for (Song s : mProject.getSongs()) {
            songNames.add(s.getName());
        }

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_checked,
                songNames) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                return v;
            }
        };
        mAdapter.setNotifyOnChange(true);

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(mAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getGroupName().isEmpty()) {
                    Toast.makeText(NewGroupActivity.this,
                            R.string.group_name_must_not_be_empty, Toast.LENGTH_SHORT).show();
                } else if (getSongs().length == 0) {
                    Toast.makeText(NewGroupActivity.this,
                            R.string.no_song_selected, Toast.LENGTH_SHORT).show();
                } else {
                    if (isDuplicate()) {
                        Toast.makeText(NewGroupActivity.this,
                                R.string.added_selected, Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("groupName", getGroupName());
                    intent.putExtra("songs", getSongs());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private boolean isDuplicate() {
        final String groupName = getGroupName();
        for (Song s : mProject.getSongs()) {
            if (s.getGroups().contains(groupName)) {
                return true;
            }
        }
        return false;
    }

    private int[] getSongs() {
        ListView lv = (ListView) findViewById(R.id.listView);
        List<Integer> songs = new ArrayList<>();
        for (int i = 0; i < mAdapter.getCount(); ++i) {
            if (lv.isItemChecked(i)) {
                songs.add(i);
            }
        }

        int[] ret = new int[songs.size()];
        for (int i = 0; i < songs.size(); ++i) {
            ret[i] = songs.get(i);
        }
        return ret;
    }

    private String getGroupName() {
        EditText et = (EditText) findViewById(R.id.editText);
        return et.getText().toString();
    }
}
