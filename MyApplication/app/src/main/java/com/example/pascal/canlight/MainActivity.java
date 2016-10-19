package com.example.pascal.canlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import junit.framework.AssertionFailedError;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO remove implements..
public class MainActivity extends AppCompatActivity {

    public static final int PATTERN_REQUEST = 0;
    public static final int IMPORT_PATTERN_REQUEST = 2;
    public static final int IMPORT_PATTERN_PREVIEW_REQUEST = 3;
    public static final int LOGIN_SPOTIFY_REQUEST = 4;
    public static final int LOGIN_GOOGLE_DRIVE_REQUEST = 5;
    public static final int RETURN_IMPORT_REQUEST = 6;
    private static final String TAG = "GDRIVE";
    private GoogleApiClient mClient;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://canlight.com/rcv_shr/"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.pascal.canlight/http/canlight.com/rcv_shr/")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    private ExpandableSongListAdapter songListAdapter;

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

        final MenuItem deleteSongItem = menu.findItem(R.id.menu_delete_song);
        final MenuItem renameSongItem = menu.findItem(R.id.menu_rename_song);
        final MenuItem editGroupItem = menu.findItem(R.id.menu_edit_song_group);
        final MenuItem renameGroupItem = menu.findItem(R.id.menu_rename_group);
        deleteSongItem.setVisible(!isGroupContextMenu);
        deleteSongItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you really want to delete \""
                                + mProject.getSong(projectIndex).getName()
                                + "\" from all groups?")
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
        if (gpos == songListAdapter.getGroupCount() - 1) {
            renameGroupItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    getString("No Group", new OnStringDialogOk() {
                        @Override
                        public void onStringDialogOk(String result) {
                            Set<String> groups = new HashSet<>();
                            groups.add(result);
                            for (Song song : mProject.getSongs()) {
                                song.setGroups(groups);
                            }
                            songListAdapter.notifyDataSetChanged();
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

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOk.onStringDialogOk(editName.getText().toString());
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
        final String oldGroupName = (String) songListAdapter.getGroup(position);
        getString(oldGroupName, new OnStringDialogOk() {
            @Override
            public void onStringDialogOk(String result) {
                mProject.renameGroup(oldGroupName, result);
            }
        });
    }

    private Song getSong(int groupPosition, int childPosition) {
        final boolean isGroupContextMenu = childPosition < 0;
        return isGroupContextMenu ? null : (Song) songListAdapter.getChild(groupPosition, childPosition);
    }

    private void editSongGroup(final int position) {
        final ListView listView = new ListView(this);
        List<String> groups = songListAdapter.getGroupNames();
        groups.add("New Group");
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_checked,
                groups);
        listView.setAdapter(adapter);
        adapter.setNotifyOnChange(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final Song song = mProject.getSong(position);
        for (int i = 0; i < songListAdapter.getGroupCount(); ++i) {
            final String groupName = (String) songListAdapter.getGroup(i);
            listView.setItemChecked(i, song.getGroups().contains(groupName));
        }
        new AlertDialog(this) {
            {
                setView(listView);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.rename_dialog_ok),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final Set<String> groups = new HashSet<>();
                                final int n = songListAdapter.getGroupCount();
                                for (int i = 0; i < n - 1; ++i) {
                                    if (listView.isItemChecked(i)) {
                                        groups.add((String) songListAdapter.getGroup(i));
                                    }
                                }
                                if (listView.isItemChecked(n - 1)) {
                                    getString("Group name", new OnStringDialogOk() {
                                        @Override
                                        public void onStringDialogOk(String result) {
                                            groups.add(result);
                                            song.setGroups(groups);
                                            songListAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                                song.setGroups(groups);
                                songListAdapter.notifyDataSetChanged();
                            }
                        });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) { }
                        });
            }
        }.show();
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
                        final String displayName = editName.getDisplayName(itemPosition);
                        mProject.renameSong(position, displayName);
                        mProject.getSong(position).setSpotifyTrack(editName.getId(itemPosition), displayName);
                        d.cancel();
                    }
                });
            }
        }.show();
    }

    void setProject(final Project project) {
        mProject = project;
        songListAdapter = new ExpandableSongListAdapter(this, project);
        project.setOnSongListChangedListener(new Project.OnSongListChangedListener() {
            @Override
            public void onSongListChanged() {
                songListAdapter.notifyDataSetChanged();
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
        listView.setAdapter(songListAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProject = new Project();
        mProject.load(getApplicationContext());
        ImportPatternCache.load();
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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStop() {
        mProject.save(getApplicationContext());
        ImportPatternCache.save();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://canlight.com/rcv_shr/"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.pascal.canlight/http/canlight.com/rcv_shr/")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
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
}
