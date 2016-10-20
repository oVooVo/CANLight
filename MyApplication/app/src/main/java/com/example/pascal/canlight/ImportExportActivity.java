package com.example.pascal.canlight;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pascal.canlight.googleDrive.GoogleDriveCreateFileActivity;

import java.util.List;

public class ImportExportActivity extends AppCompatActivity {

    private Project masterProject;
    private Project projectOfInterest;
    private boolean isImport;
    private static final int EXPORT_REQUEST_CODE = 0;

    private class SongListArrayAdapter extends ArrayAdapter<Song> {
        private List<Song> mSongs;

        public SongListArrayAdapter(List<Song> songs) {
            super(ImportExportActivity.this, android.R.layout.simple_list_item_checked, songs);
            mSongs = songs;
            setNotifyOnChange(true);
            //mCheckBoxes = new CheckBox[songs.size()];
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            CheckedTextView textView = (CheckedTextView) view.findViewById(android.R.id.text1);
            textView.setText(mSongs.get(position).getName());
            return view;
        }
    }
    private SongListArrayAdapter songListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        masterProject = getIntent().getParcelableExtra("project");

        if (getIntent().getBooleanExtra("export", false)) {
            isImport = false;
            songListAdapter = new SongListArrayAdapter(masterProject.getSongs());
        } else {
            isImport = true;
            projectOfInterest = getIntent().getParcelableExtra("ImportedProject");
            songListAdapter = new SongListArrayAdapter(projectOfInterest.getSongs());
        }
        ListView view = (ListView) findViewById(R.id.listView);
        view.setAdapter(songListAdapter);
        for (int i = 0; i < view.getCount(); ++i) {
            view.setItemChecked(i, true);
        }
    }

    private void createProjectOfInterest() {
        projectOfInterest = new Project();
        ListView listView = (ListView) findViewById(R.id.listView);
        for (int i = 0; i < masterProject.getSongs().size(); ++i) {
            if (listView.isItemChecked(i)) {
                projectOfInterest.addSong("");
                projectOfInterest.setSong(i, masterProject.getSong(i));
            }
        }
    }

    private Project mergeProjects(Project master, Project slave) {
        Project project = new Project();
        for (Song song : master.getSongs()) {
            project.addSong(song);
        }

        ListView view = (ListView) findViewById(R.id.listView);
        for (int i = 0; i < slave.getSongs().size(); ++i) {
            if (view.isItemChecked(i)) {
                final Song song = slave.getSong(i);
                int position = master.findSong(song.getName());
                if (position < 0) {
                    project.addSong(song);
                } else {
                    project.setSong(position, song);
                }
            }
        }

        return project;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.import_export_menu, menu);

        // Edit Stuff
        MenuItem importExportItem = menu.findItem(R.id.menu_import_export);
        importExportItem.setTitle( isImport ? R.string.menu_import_title : R.string.menu_export_title );
        importExportItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (isImport) {
                    //IMPORT
                    Intent intent = new Intent();
                    projectOfInterest = mergeProjects(masterProject, projectOfInterest);
                    intent.putExtra("MergedProject", projectOfInterest);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    //EXPORT
                    createProjectOfInterest();
                    final String content = projectOfInterest.toJson().toString();
                    Intent intent = new Intent(ImportExportActivity.this, GoogleDriveCreateFileActivity.class);
                    intent.putExtra("content", content);
                    startActivityForResult(intent, EXPORT_REQUEST_CODE);
                }
                return true;
            }
        });
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EXPORT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    // export is ok, close this activity.
                    finish();
                } else {
                    // something went wrong.
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
