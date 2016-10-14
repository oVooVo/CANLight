package com.example.pascal.canlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
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
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import junit.framework.AssertionFailedError;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int PATTERN_REQUEST = 0;
    public static final int IMPORT_PATTERN_REQUEST = 2;
    public static final int IMPORT_PATTERN_PREVIEW_REQUEST = 3;
    public static final int LOGIN_SPOTIFY_REQUEST = 4;
    public static final int LOGIN_GOOGLE_DRIVE_REQUEST = 5;
    private static final String TAG = "GDRIVE";
    private GoogleApiClient mClient;

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
        final EditText editName = new SpotifySpinner(this);
        editName.setMaxLines(1);
        editName.setText(project.getSong(position).getName());
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
        final int fPosition = position;
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
                        project.renameItem(fPosition, newName);
                    }
                });
                setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.rename_dialog_cancel),
                        new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (fItemIsNew) {
                            project.remove(fPosition);
                        }
                    }
                });
            }
        }.show();
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
        ImportPatternCache.load();

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
        ImportPatternCache.save();
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
        switch (requestCode) {
            case PATTERN_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (currentEditPosition < 0) throw new AssertionFailedError();

                    if (data.getExtras() != null && data.getExtras().containsKey("song")) {
                        // if EditActivity was not read-only.
                        Song song = data.getExtras().getParcelable("song");
                        project.setSong(currentEditPosition, song);
                    }
                }
                currentEditPosition = -1;
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
                shareAll();
                return true;
            }
        });
        return true;
    }

    private void shareAll() {
        String content = project.toJson().toString();
        Intent intent = new Intent(MainActivity.this, GoogleDriveCreateFileActivity.class);
        intent.putExtra("content", content);
        MainActivity.this.startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mClient.connect();
    }

    @Override
    protected void onPause() {
        if (mClient != null) {
            mClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, MainActivity.LOGIN_GOOGLE_DRIVE_REQUEST);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");

        /*
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                    REQUEST_CODE_CAPTURE_IMAGE);
            return;
        }
        saveFileToDrive();
        */
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Log.i(TAG, "GoogleApiClient connection suspended");
    }

}
