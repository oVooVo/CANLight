package com.example.pascal.canlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pascal.canlight.chordPattern.EditActivity;
import com.example.pascal.canlight.chordPattern.ImportPatternCache;
import com.example.pascal.canlight.googleDrive.ReceiverActivity;
import com.example.pascal.canlight.midi.Midi;

import junit.framework.AssertionFailedError;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final int PATTERN_REQUEST = 0;
    public static final int IMPORT_PATTERN_REQUEST = 2;
    public static final int IMPORT_PATTERN_PREVIEW_REQUEST = 3;
    public static final int LOGIN_SPOTIFY_REQUEST = 4;
    public static final int RETURN_IMPORT_REQUEST = 6;
    public static final int NEW_GROUP_REQUEST = 7;

    private ExpandableSongListAdapter mSongListAdapter;

    public static final int GET_TRACK_REQUEST = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProject = new Project();
        mProject.load(getApplicationContext());
        ImportPatternCache.load(getApplicationContext());
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        listView.setClickable(true);
        registerForContextMenu(listView);
        listView.setOnChildClickListener( new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final Song song = getSong(groupPosition, childPosition);
                final int projectIndex = song == null ? -1 : mProject.getIndexOf(song);
                openEditMode(projectIndex);
                return false;
            }
        });
        setProject(mProject);

        ((SearchView) findViewById(R.id.searchView)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSongListAdapter.filter(newText);
                return true;
            }
        });

        Midi.init(this);
    }

    int currentEditPosition = -1;
    private Project mProject;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listview_context_menu, menu);

        setTitle(R.string.app_name);
        final ExpandableListView.ExpandableListContextMenuInfo acmi = (ExpandableListView.ExpandableListContextMenuInfo) info;

        final int cpos = ExpandableListView.getPackedPositionChild(acmi.packedPosition);
        final int gpos = ExpandableListView.getPackedPositionGroup(acmi.packedPosition);
        final Song song = getSong(gpos, cpos);
        final boolean isGroupContextMenu = song == null;
        final int projectIndex = isGroupContextMenu ? -1 : mProject.getIndexOf(song);
        final boolean isNoGroupItem = (gpos == mSongListAdapter.getGroupCount() - 1);

        final MenuItem deleteSongItem = menu.findItem(R.id.menu_delete_song);
        final MenuItem renameSongItem = menu.findItem(R.id.menu_rename_song);
        final MenuItem editGroupItem = menu.findItem(R.id.menu_edit_song_group);
        final MenuItem renameGroupItem = menu.findItem(R.id.menu_rename_group);
        final MenuItem removeGroupItem = menu.findItem(R.id.menu_remove_group);

        deleteSongItem.setVisible(!isGroupContextMenu);
        deleteSongItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(String.format(getString(R.string.confirm_deletition_string),
                                mProject.getSong(projectIndex).getName()))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mProject.removeSong(projectIndex);
                            }})
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
        renameSongItem.setVisible(!isGroupContextMenu);
        renameSongItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                editSongName(projectIndex, false);
                return true;
            }
        });
        editGroupItem.setVisible(!isGroupContextMenu);
        editGroupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                editSongGroup(projectIndex);
                return true;
            }
        });

        renameGroupItem.setVisible(isGroupContextMenu);
        if (isNoGroupItem) {
            renameGroupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    getString((String) mSongListAdapter.getGroup(gpos), new OnStringDialogOk() {
                        @Override
                        public void onStringDialogOk(String result) {
                            Set<String> groups = new HashSet<>();
                            groups.add(result);
                            for (Song song : mProject.getSongs()) {
                                song.setGroups(groups);
                            }
                            mSongListAdapter.notifyDataSetChanged();
                            save();
                        }
                    });
                    return true;
                }
            });
        } else {
            renameGroupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    editGroupName(gpos);
                    return true;
                }
            });
        }

        removeGroupItem.setVisible(isGroupContextMenu && !isNoGroupItem);
        removeGroupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                removeGroup(gpos);
                return true;
            }
        });
    }

    private void save() {
        mProject.save(getApplicationContext());
        ImportPatternCache.save(getApplicationContext());
    }

    private interface OnStringDialogOk {
        void onStringDialogOk(String result);
    }

    private void getString(final String defaultValue, final OnStringDialogOk onOk) {
        final EditText editName = new EditText(this);
        editName.setText(defaultValue);
        new AlertDialog(this) {
            {
                final View view = editName;
                setView(view);

                editName.setMaxLines(1);
                editName.setText(defaultValue);
                editName.selectAll();
                editName.setImeOptions(EditorInfo.IME_ACTION_DONE);
                editName.setSingleLine(true);

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOk.onStringDialogOk(editName.getText().toString());
                    }
                });
                final AlertDialog d = this;
                editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            onOk.onStringDialogOk(editName.getText().toString());
                            d.cancel();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) { }
                        });
            }
        }.show();
    }

    private void editGroupName(final int position) {
        final String oldGroupName = (String) mSongListAdapter.getGroup(position);
        getString(oldGroupName, new OnStringDialogOk() {
            @Override
            public void onStringDialogOk(String result) {
                mProject.renameGroup(oldGroupName, result);
            }
        });
    }

    private Song getSong(int groupPosition, int childPosition) {
        final boolean isGroupContextMenu = childPosition < 0;
        return isGroupContextMenu ? null : (Song) mSongListAdapter.getChild(groupPosition, childPosition);
    }

    private void editSongGroup(final int position) {
        final ListView listView = new ListView(this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked,
                mSongListAdapter.getGroupNames());
        listView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final Song song = mProject.getSong(position);
        for (int i = 0; i < mSongListAdapter.getGroupCount(); ++i) {
            final String groupName = (String) mSongListAdapter.getGroup(i);
            listView.setItemChecked(i, song.getGroups().contains(groupName));
        }
        AlertDialog d = new AlertDialog(this) {
            {
                setView(listView);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final Set<String> groups = new HashSet<>();
                                final int n = mSongListAdapter.getGroupCount();
                                for (int i = 0; i < n - 1; ++i) {
                                    if (listView.isItemChecked(i)) {
                                        groups.add((String) mSongListAdapter.getGroup(i));
                                    }
                                }
                                song.setGroups(groups);
                                mSongListAdapter.notifyDataSetChanged();
                                save();
                            }
                        });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) { }
                        });
            }
        };
        d.setTitle(String.format(getString(R.string.groups_containing), song.getName()));
        d.show();
    }

    private void editSongName(final int position, boolean itemIsNew) {
        final SpotifySpinner editName = new SpotifySpinner(this);
        editName.setMaxLines(1);
        editName.setText(mProject.getSong(position).getName());
        editName.selectAll();

        // show keyboard
        editName.requestFocus();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(editName, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);

        final boolean fItemIsNew = itemIsNew;
        new AlertDialog(this) {
            {
                final View view = editName;
                setView(view);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final String newName = editName.getText().toString();
                                mProject.renameSong(position, newName);
                            }
                        });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (fItemIsNew) {
                                    mProject.removeSong(position);
                                }
                            }
                        });
                final AlertDialog d = this;
                editName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int itemPosition, long id) {
                        final String label = editName.getLabel(itemPosition);
                        mProject.renameSong(position, label);
                        mProject.getSong(position).setTrack(editName.getService(), editName.getId(itemPosition), label);
                        d.cancel();
                    }
                });
            }
        }.show();
    }

    void setProject(final Project project) {
        mProject = project;
        mSongListAdapter = new ExpandableSongListAdapter(this, project);
        project.setOnSongListChangedListener(new Project.OnSongListChangedListener() {
            @Override
            public void onSongListChanged() {
                mSongListAdapter.notifyDataSetChanged();
                save();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = project.addSong(getString(R.string.default_song_name));
                editSongName(position, true);
            }
        });
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        listView.setAdapter(mSongListAdapter);
    }

    private void openEditMode(int position) {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("song", mProject.getSong(position));
        if (currentEditPosition >= 0) throw new AssertionFailedError();
        currentEditPosition = position;
        MainActivity.this.startActivityForResult(intent, PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PATTERN_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (currentEditPosition < 0) throw new AssertionFailedError();

                    if (data.getExtras() != null && data.getExtras().containsKey("song")) {
                        // if EditActivity was not read-only.
                        Song song = data.getExtras().getParcelable("song");
                        mProject.setSong(currentEditPosition, song);
                    }
                }
                currentEditPosition = -1;
                break;
            case RETURN_IMPORT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Project project = data.getParcelableExtra("MergedProject");
                    setProject(project);
                }
                break;
            case NEW_GROUP_REQUEST:
                if (resultCode == RESULT_OK) {
                    final String groupName = data.getStringExtra("groupName");
                    for (int p : data.getIntArrayExtra("songs")) {
                        final Song s = mProject.getSong(p);
                        Set<String> groups = s.getGroups();
                        groups.add(groupName);
                        s.setGroups(groups);
                    }
                    mSongListAdapter.notifyDataSetChanged();
                    save();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivityForResult(intent, PATTERN_REQUEST);
                return true;
            }
        });
        menu.findItem(R.id.menu_share_all).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startExport();
                return true;
            }
        });
        menu.findItem(R.id.menu_new_group).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                newGroup();
                return true;
            }
        });
        return true;
    }

    private void startExport() {
        Intent intent = new Intent(MainActivity.this, ImportExportActivity.class);
        intent.putExtra("project", mProject);
        intent.putExtra("export", true);
        startActivity(intent);
    }


    private void startImport(String id) {
        Intent intent = new Intent(MainActivity.this, ReceiverActivity.class);
        intent.putExtra("project", mProject);
        intent.putExtra("export", false);
        intent.putExtra("id", id);
        startActivityForResult(intent, RETURN_IMPORT_REQUEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (handleImportIntent(getIntent())) {
            setIntent(new Intent());
        }
        save();
    }

    private boolean handleImportIntent(Intent intent) {
        final Uri uri = intent.getData();
        if (uri != null) {
            if (uri.getPathSegments().size() < 2) {
                Toast.makeText(getApplicationContext(), "Could not get link", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    final String id = URLDecoder.decode(uri.getPathSegments().get(1), "utf-8");
                    startImport(id);
                    return true;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionFailedError();
                }
            }
        }
        return false;
    }

    protected void onNewIntent(Intent intent) {
        // manifest: android:launchMode= "singleInstance"
        super.onNewIntent(intent);
        handleImportIntent(intent);
    }

    private void removeGroup(int gpos) {
        final String group = (String) mSongListAdapter.getGroup(gpos);
        for (Song s : mProject.getSongs()) {
            Set<String> groups = s.getGroups();
            groups.remove(group);
            s.setGroups(groups);
        }
        mSongListAdapter.notifyDataSetChanged();
        save();
    }

    private void newGroup() {
        Intent intent = new Intent(MainActivity.this, NewGroupActivity.class);
        intent.putExtra("project", mProject);
        MainActivity.this.startActivityForResult(intent, NEW_GROUP_REQUEST);
    }
}
