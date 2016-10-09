package com.example.pascal.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import junit.framework.AssertionFailedError;


public class MainActivity extends AppCompatActivity {

    public static final int PATTERN_REQUEST = 0;
    public static final int IMPORT_PATTERN_REQUEST = 2;
    public static final int IMPORT_PATTERN_PREVIEW_REQUEST = 2;
    int currentEditPosition = -1;
    private Project project;

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listview_context_menu, menu);

        setTitle(R.string.app_name);
        final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) info;

        menu.findItem(R.id.menu_delete_song).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                project.remove(acmi.position);
                return true;
            }
        });
        menu.findItem(R.id.menu_rename_song).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                editSongName(acmi.position, false);
                return true;
            }
        });
    }

    private void editSongName(int position, boolean itemIsNew) {
        final EditText editName = new EditText(this);
        editName.setMaxLines(1);
        editName.setText(project.getSong(position).getName());
        editName.selectAll();

        // show keyboard
        editName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);

        final boolean fItemIsNew = itemIsNew;
        final int fPosition = position;
        new AlertDialog.Builder(this)
            .setView(editName)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final String newName = editName.getText().toString();
                    project.renameItem(fPosition, newName);
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (fItemIsNew) {
                        project.remove(fPosition);
                    }
                }
            })
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = project.addItem();
                editSongName(position, true);
            }
        });

        project = new Project(getApplicationContext());
        project.load();
        ImportCache.load();

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(project.itemAdapter());

        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                openEditMode(position);
            }
        });
    }

    @Override
    protected void onStop() {
        project.save();
        ImportCache.save();
        super.onStop();
    }

    private void openEditMode(int position) {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("song", project.getSong(position));
        if (currentEditPosition >= 0) throw new AssertionFailedError();
        currentEditPosition = position;
        MainActivity.this.startActivityForResult(intent, PATTERN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PATTERN_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (currentEditPosition < 0) throw new AssertionFailedError();

                if (data.getExtras() != null && data.getExtras().containsKey("song")) {
                    // if EditActivity was not read-only.
                    Song song = data.getExtras().getParcelable("song");
                    project.setSong(currentEditPosition, song);
                }
            }
            currentEditPosition = -1;
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
        return true;
    }
}
