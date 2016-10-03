package com.example.pascal.myapplication;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import junit.framework.AssertionFailedError;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    static final int PATTERN_REQUEST = 0;
    int currentEditPosition = -1;
    private Project project;

    private void showMsg(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.addItem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final int position = project.addItem();
                editItemName(position);
                return true;
            }
        });

        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.listview_context_menu, menu);

        final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) info;

        menu.findItem(R.id.deleteItem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                project.remove(acmi.position);
                return true;
            }
        });
        menu.findItem(R.id.renameItem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                editItemName(acmi.position);
                return true;
            }
        });
    }

    private void editItemName(int position) {
        final EditText editName = new EditText(this);
        editName.setMaxLines(1);
        editName.setText(project.name(position));
        editName.selectAll();

        // show keyboard
//        editName.requestFocus();
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

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
                    // do nothing.
                }
            })
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        project = new Project(getApplicationContext());
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

    private void openEditMode(int position) {
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra("pattern", project.pattern(position));
        if (currentEditPosition >= 0) throw new AssertionFailedError();
        currentEditPosition = position;
        MainActivity.this.startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PATTERN_REQUEST) {
            if (resultCode == RESULT_OK || true) {
                if (currentEditPosition < 0) throw new AssertionFailedError();

                final String pattern = data.getExtras().getString("pattern");
                project.setPattern(currentEditPosition, pattern);
            }
            currentEditPosition = -1;
        }
    }
}
