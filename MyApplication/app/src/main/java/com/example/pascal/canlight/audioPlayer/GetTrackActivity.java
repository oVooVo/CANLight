package com.example.pascal.canlight.audioPlayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import com.example.pascal.canlight.IconArrayAdapter;
import com.example.pascal.canlight.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by pascal on 20.10.16.
 */
public class GetTrackActivity extends Activity {
    private static final String TAG = "GetTrackActivity";
    private TrackAdapter mCurrentAdapter;
    private List<TrackAdapter> mTrackAdapters = new ArrayList<>();

    private void maybeAddTrackAdapter(TrackAdapter newTrackAdapter) {
        if (newTrackAdapter.readyToUse()) {
            Log.i(TAG, "Init " + newTrackAdapter.getName() + " Track Adapter");
            mTrackAdapters.add(newTrackAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // create content
        setContentView(R.layout.activity_get_track);

        maybeAddTrackAdapter(new SpotifyTrackAdapter(this));
        maybeAddTrackAdapter(new YouTubeTrackAdapter(this));

        // service spinner
        Spinner spinner = findViewById(R.id.switchServiceSpinner);
        spinner.setAdapter(new IconArrayAdapter(this, mTrackAdapters.stream().map(TrackAdapter::getName).collect(Collectors.toList())) {
            @Override
            protected void setIcon(ImageView view, int position) {
                view.setImageResource(mTrackAdapters.get(position).getIcon());
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setTrackAdapter(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ListView resultListView = findViewById(R.id.listViewResults);
        resultListView.setOnItemClickListener((parent, view, position, id) -> finish( mCurrentAdapter.getName(),
                mCurrentAdapter.getId(position),
                mCurrentAdapter.getLabel(position) ));

        Button searchButton = findViewById(R.id.buttonSearch);
        searchButton.setOnClickListener(v -> search());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);


        findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
        findViewById(R.id.no_results_hint).setVisibility(View.INVISIBLE);
        TrackAdapter.OnResultsArrivedListener onResultsArrivedListener = results -> {
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            if (results.isEmpty()) {
                findViewById(R.id.no_results_hint).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.no_results_hint).setVisibility(View.INVISIBLE);
            }
        };

        for (TrackAdapter ta: mTrackAdapters) {
            ta.setOnResultsArrivedListener(onResultsArrivedListener);
        }

        EditText editTextQuery = findViewById(R.id.textViewQuery);
        final String initialLabel = getIntent().getStringExtra("label");
        if (initialLabel == null || initialLabel.isEmpty()) {
            editTextQuery.setText(getIntent().getStringExtra("songName"));
        } else {
            editTextQuery.setText(initialLabel);
        }
        editTextQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
                return true;
            } else {
                return false;
            }
        });
        editTextQuery.setOnLongClickListener(v -> {
            ((EditText) v).setText(getIntent().getStringExtra("songName"));
            return false;
        });

        int initialTrackAdapterId = 0;
        final String initialServiceName = getIntent().getStringExtra("service");
        if (initialServiceName != null) {
            OptionalInt initialTrackAdapterIdOption = IntStream.range(0, mTrackAdapters.size())
                    .filter(i -> mTrackAdapters.get(i).getName().equals(initialServiceName)).findAny();
            if (!initialTrackAdapterIdOption.isPresent()) {
                throw new IllegalArgumentException();
            }
            initialTrackAdapterId = initialTrackAdapterIdOption.getAsInt();
        }

        setTrackAdapter(initialTrackAdapterId);

        search();
    }

    protected void onDestroy() {
        super.onDestroy();

        for (TrackAdapter ta: mTrackAdapters) {
            ta.deinit();
        }
    }

    private void search() {
        EditText editText = findViewById(R.id.textViewQuery);
        mCurrentAdapter.search(editText.getText().toString());
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.no_results_hint).setVisibility(View.INVISIBLE);
    }

    /**
     * activates the ith track adapter in mTrackAdapters
     * @param i is an index in mTrackAdapters
     */
    private void setTrackAdapter(int i) {
        Spinner serviceSpinner = findViewById(R.id.switchServiceSpinner);
        ListView resultListView = findViewById(R.id.listViewResults);
        mCurrentAdapter = mTrackAdapters.get(i);
        serviceSpinner.setSelection(i);
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
