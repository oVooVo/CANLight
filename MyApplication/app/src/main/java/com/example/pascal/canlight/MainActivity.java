package com.example.pascal.canlight;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pascal.canlight.audioPlayer.TrackAdapter;
import com.example.pascal.canlight.chordPattern.EditActivity;
import com.example.pascal.canlight.chordPattern.ImportPatternCache;
import com.example.pascal.canlight.midi.Midi;

import junit.framework.AssertionFailedError;

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
    public static final int CHOOSE_COLOR_REQUEST = 8;

    private ExpandableSongListAdapter mSongListAdapter;

    public static final int GET_TRACK_REQUEST = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProject = new Project();
        mProject.load(getApplicationContext());
        ImportPatternCache.load(getApplicationContext());
        final ExpandableListView listView = findViewById(R.id.listView);
        listView.setClickable(true);
        registerForContextMenu(listView);
        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            final Song song = getSong(groupPosition, childPosition);
            final int projectIndex = song == null ? -1 : mProject.getIndexOf(song);
            openEditMode(projectIndex);
            return false;
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
                expandNonEmptyGroups();
                return true;
            }
        });

        Midi.init(this);

        MySpotify.spotifyConnectRequest(this);
    }

    private void expandNonEmptyGroups() {
        ExpandableListView songsView = findViewById(R.id.listView);
        ExpandableListAdapter adapter = songsView.getExpandableListAdapter();
        for (int i = 0; i < adapter.getGroupCount(); ++i) {
            if (adapter.getChildrenCount(i) >= 0) {
                songsView.expandGroup(i);
            } else {
                songsView.collapseGroup(i);
            }
        }
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
        final int songIndex = isGroupContextMenu ? -1 : mProject.getIndexOf(song);
        final boolean isNoGroupItem = (gpos == mSongListAdapter.getGroupCount() - 1);

        final MenuItem deleteSongItem = menu.findItem(R.id.menu_delete_song);
        final MenuItem renameSongItem = menu.findItem(R.id.menu_rename_song);
        final MenuItem editGroupItem = menu.findItem(R.id.menu_edit_song_group);
        final MenuItem renameGroupItem = menu.findItem(R.id.menu_rename_group);
        final MenuItem removeGroupItem = menu.findItem(R.id.menu_remove_group);

        deleteSongItem.setVisible(!isGroupContextMenu);
        deleteSongItem.setOnMenuItemClickListener(item -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(String.format(getString(R.string.confirm_deletion_string),
                            mProject.getSong(songIndex).getName()))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> mProject.removeSong(songIndex))
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        });
        renameSongItem.setVisible(!isGroupContextMenu);
        renameSongItem.setOnMenuItemClickListener(item -> {
            editSongName(songIndex, false);
            return true;
        });
        editGroupItem.setVisible(!isGroupContextMenu);
        editGroupItem.setOnMenuItemClickListener(item -> {
            editSongGroup(songIndex);
            return true;
        });

        renameGroupItem.setVisible(isGroupContextMenu);
        if (isNoGroupItem) {
            renameGroupItem.setOnMenuItemClickListener(item -> {
                getString((String) mSongListAdapter.getGroup(gpos), result -> {
                    Set<String> groups = new HashSet<>();
                    groups.add(result);
                    for (Song song1 : mProject.getSongs()) {
                        song1.setGroups(groups);
                    }
                    mSongListAdapter.notifyDataSetChanged();
                    save();
                });
                return true;
            });
        } else {
            renameGroupItem.setOnMenuItemClickListener(item -> {
                editGroupName(gpos);
                return true;
            });
        }

        removeGroupItem.setVisible(isGroupContextMenu && !isNoGroupItem);
        removeGroupItem.setOnMenuItemClickListener(item -> {
            removeGroup(gpos);
            return true;
        });
    }

    private void save() {
        mProject.save(this);
        ImportPatternCache.save(this);
    }

    private void exportProject() {
        mProject.exportProject(this);
    }

    private void importProject() {
        mProject.importProject(this);
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
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok), (dialog, which) -> onOk.onStringDialogOk(editName.getText().toString()));
                final AlertDialog d = this;
                editName.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        onOk.onStringDialogOk(editName.getText().toString());
                        d.cancel();
                        return true;
                    } else {
                        return false;
                    }
                });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        (dialog, whichButton) -> {
                        });
            }
        }.show();
    }

    private void editGroupName(final int position) {
        final String oldGroupName = (String) mSongListAdapter.getGroup(position);
        getString(oldGroupName, result -> mProject.renameGroup(oldGroupName, result));
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
                        (dialog, whichButton) -> {
                        });
            }
        };
        d.setTitle(String.format(getString(R.string.groups_containing), song.getName()));
        d.show();
    }

    private void editSongName(final int position, final boolean itemIsNew) {
        final SpotifySpinner nameEditor = new SpotifySpinner(this);
        nameEditor.setMaxLines(1);
        nameEditor.setText(mProject.getSong(position).getName());
        nameEditor.selectAll();

        // show keyboard
        nameEditor.requestFocus();
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(nameEditor, InputMethodManager.SHOW_IMPLICIT);
        }, 100);

        Dialog songNameDialog = new AlertDialog(this) {
            {
                nameEditor.setDropDownVerticalOffset(120);
                setView(nameEditor);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final String newName = nameEditor.getText().toString();
                                Log.i(TAG, "Create new song with name <" + newName + ">");
                                if (newName.isEmpty() && itemIsNew) {
                                    Log.i(TAG, "Remove new song since name was empty");
                                    mProject.removeSong(position);
                                } else {
                                    mProject.renameSong(position, newName);
                                    if (itemIsNew) {
                                        openEditMode(position);
                                    }
                                    expandNoGroup();
                                }
                            }
                        });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.d(TAG, "Item is new (cancel): " + itemIsNew);
                                if (itemIsNew) {
                                    mProject.removeSong(position);
                                }
                            }
                        });
                final AlertDialog d = this;
                nameEditor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int itemPosition, long id) {
                        final TrackAdapter.Track track = nameEditor.getTrack(itemPosition);
                        mProject.renameSong(position, track.label);
                        mProject.getSong(position).setTrack(track);
                        expandNoGroup();
                        if (itemIsNew) {
                            openEditMode(position);
                        }
                        d.cancel();
                    }
                });
                nameEditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            mProject.renameSong(position, v.getText().toString());
                            expandNoGroup();
                            d.cancel();
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
            }

            void expandNoGroup() {
                final ExpandableListView listView = MainActivity.this.findViewById(R.id.listView);
                final int lastGoup = listView.getExpandableListAdapter().getGroupCount() - 1;
                listView.expandGroup(lastGoup);
            }
        };

        Window window = songNameDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.TOP;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        songNameDialog.show();
    }

    void setProject(final Project project) {
        mProject = project;
        mSongListAdapter = new ExpandableSongListAdapter(this, project);
        final ExpandableListView listView = findViewById(R.id.listView);
        listView.setAdapter(mSongListAdapter);
        project.setOnSongListChangedListener(() -> {
            mSongListAdapter.notifyDataSetChanged();
            save();
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            final int position = project.addSong(getString(R.string.default_song_name));
            editSongName(position, true);
        });
    }

    private void openEditMode(int position) {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("song", mProject.getSong(position));
        if (currentEditPosition >= 0) throw new AssertionFailedError();
        currentEditPosition = position;
        MainActivity.this.startActivityForResult(intent, PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case PATTERN_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (currentEditPosition < 0) throw new AssertionFailedError();

                    if (intent.getExtras() != null && intent.getExtras().containsKey("song")) {
                        // if EditActivity was not read-only.
                        Song song = intent.getExtras().getParcelable("song");
                        mProject.setSong(currentEditPosition, song);
                    }
                }
                currentEditPosition = -1;
                break;
            case RETURN_IMPORT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Project project = intent.getParcelableExtra("MergedProject");
                    setProject(project);
                }
                break;
            case NEW_GROUP_REQUEST:
                if (resultCode == RESULT_OK) {
                    final String groupName = intent.getStringExtra("groupName");
                    for (int p : intent.getIntArrayExtra("songs")) {
                        final Song s = mProject.getSong(p);
                        Set<String> groups = s.getGroups();
                        groups.add(groupName);
                        s.setGroups(groups);
                    }
                    mSongListAdapter.notifyDataSetChanged();
                    save();
                }
                break;
            case MainActivity.LOGIN_SPOTIFY_REQUEST:
                MySpotify.onLoginResponse(this, resultCode, intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.menu_settings).setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivityForResult(intent, PATTERN_REQUEST);
            return true;
        });
        menu.findItem(R.id.menu_export_project).setOnMenuItemClickListener(item -> {
            exportProject();
            return true;
        });
        menu.findItem(R.id.menu_import_project).setOnMenuItemClickListener(item -> {
            importProject();
            return true;
        });
        menu.findItem(R.id.menu_new_group).setOnMenuItemClickListener(item -> {
            newGroup();
            return true;
        });
        return true;
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
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        SearchView sv = findViewById(R.id.searchView);
        if (!sv.isIconified()) {
            sv.setQuery("", true);
            sv.setIconified(true);
        } else {
            ExpandableListView songsView = findViewById(R.id.listView);
            boolean allGroupsAreCollapsed = true;
            for (int i = 0; i < songsView.getExpandableListAdapter().getGroupCount(); ++i) {
                if (songsView.isGroupExpanded(i)) {
                    allGroupsAreCollapsed = false;
                    songsView.collapseGroup(i);
                }
            }
            if (allGroupsAreCollapsed) {
                super.onBackPressed();
            }
        }
    }
}