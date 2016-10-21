package com.example.pascal.canlight.audioPlayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.pascal.canlight.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pascal on 20.10.16.
 */
public class GetTrackActivity extends AppCompatActivity {
    private static final String TAG = "GetTrackActivity";
    private int mCurrentService;
    private SpotifyTrackAdapter mSpotifyAdapter;
    private YouTubeTrackAdapter mYoutubeAdapter;
    private TrackAdapter mCurrentAdapter;

    public final static List<String> SERVICES = Arrays.asList("Spotify", "YouTube");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // create content
        setContentView(R.layout.get_song_dialog_layout);

        // service spinner
        Spinner spinner = (Spinner) findViewById(R.id.switchServiceSpinner);
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, SERVICES));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setService(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ListView resultListView = (ListView) findViewById(R.id.listViewResults);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finish( SERVICES.get(mCurrentService),
                        mCurrentAdapter.getId(position),
                        mCurrentAdapter.getLabel(position) );
            }
        });

        Button searchButton = (Button) findViewById(R.id.buttonSearch);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.textViewQuery);
                mCurrentAdapter.search(editText.getText().toString());
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        mSpotifyAdapter = new SpotifyTrackAdapter(this);
        mYoutubeAdapter = new YouTubeTrackAdapter(this);

        final String initialLabel = getIntent().getStringExtra("label");
        ((EditText) findViewById(R.id.textViewQuery)).setText(initialLabel);


        final String initialService = getIntent().getStringExtra("service");
        setService(SERVICES.indexOf(initialService));
    }

    protected void onDestroy() {
        super.onDestroy();
        mYoutubeAdapter.deinit();
    }

    private void setService(int service) {
        Spinner serviceSpinner = (Spinner) findViewById(R.id.switchServiceSpinner);
        ListView resultListView = (ListView) findViewById(R.id.listViewResults);
        mCurrentService = service;
        if (service == 0) {
            mCurrentAdapter = mSpotifyAdapter;
        } else {
            mCurrentAdapter = mYoutubeAdapter;
        }
        serviceSpinner.setSelection(service);
        resultListView.setAdapter(mCurrentAdapter);
    }

    private void finish(String service, String id, String label) {
        Intent data = new Intent();
        data.putExtra("service", service);
        data.putExtra("id", id);
        data.putExtra("label", label);
        setResult(RESULT_OK, data);
        finish();
    }
}
