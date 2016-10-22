package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pascal.canlight.IconArrayAdapter;
import com.example.pascal.canlight.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pascal on 20.10.16.
 */
public class GetTrackActivity extends Activity {
    private static final String TAG = "GetTrackActivity";
    private int mCurrentService;
    private SpotifyTrackAdapter mSpotifyAdapter;
    private YouTubeTrackAdapter mYoutubeAdapter;
    private TrackAdapter mCurrentAdapter;

    public final static List<String> SERVICES = Arrays.asList("Spotify", "YouTube");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create content
        setContentView(R.layout.activity_get_track);

        // service spinner
        Spinner spinner = (Spinner) findViewById(R.id.switchServiceSpinner);
        spinner.setAdapter(new IconArrayAdapter(this, SERVICES) {
            @Override
            protected void setIcon(ImageView view, int position) {
                if (position == 0) {
                    view.setImageResource(R.drawable.ic_spotify);
                } else {
                    view.setImageResource(R.drawable.ic_youtube);
                }
            }
        });
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
                search();
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        mSpotifyAdapter = new SpotifyTrackAdapter(this);
        mYoutubeAdapter = new YouTubeTrackAdapter(this);
        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        TrackAdapter.OnResultsArrivedListener onResultsArrivedListener = new TrackAdapter.OnResultsArrivedListener() {
            @Override
            public void onResultsArrived(List<?> results) {
                findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                if (results.isEmpty()) {
                    Toast.makeText(GetTrackActivity.this,
                            "No results", Toast.LENGTH_LONG).show();
                }
            }
        };
        mYoutubeAdapter.setOnResultsArrivedListener(onResultsArrivedListener);
        mSpotifyAdapter.setOnResultsArrivedListener(onResultsArrivedListener);

        final String initialLabel = getIntent().getStringExtra("label");
        EditText editTextQuery = (EditText) findViewById(R.id.textViewQuery);
        editTextQuery.setText(initialLabel);
        editTextQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                } else {
                    return false;
                }
            }
        });
        editTextQuery.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((EditText) v).setText(getIntent().getStringExtra("songName"));
                return false;
            }
        });

        final String initialService = getIntent().getStringExtra("service");
        setService(SERVICES.indexOf(initialService));

        search();
    }

    protected void onDestroy() {
        super.onDestroy();
        mYoutubeAdapter.deinit();
    }

    private void search() {
        EditText editText = (EditText) findViewById(R.id.textViewQuery);
        mCurrentAdapter.search(editText.getText().toString());
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
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
