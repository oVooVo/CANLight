package com.example.pascal.canlight.chordPattern;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pascal.canlight.MainActivity;
import com.example.pascal.canlight.R;
import com.example.pascal.canlight.chordPattern.UltimateGuitarChordPatternImporter.UltimateGuitarChordPatternImporter;

import java.util.ArrayList;
import java.util.List;

public class ImportPatternActivity extends AppCompatActivity {

    ArrayList<String> items;
    ArrayList<String> urls;
    ArrayAdapter<String> adapter;
    private ChordPatternImporter m_chordPatternImporter = UltimateGuitarChordPatternImporter.getMostCurrentInstance();

    private void showProgressBar() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarImportTOC);
        bar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.progressBarImportTOC);
        bar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_import);
        final String name = getIntent().getStringExtra("name");

        final EditText editText = (EditText) findViewById(R.id.importSearchKeywordEdit);
        editText.setText(name);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.import_pattern_activity_title);
        }

        Button searchButton = (Button) findViewById(R.id.buttonRefreshImportList);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        hideProgressBar();

        items = new ArrayList<>();
        urls = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                Spannable content = new SpannableString(items.get(position));
                if (ImportPatternCache.isPatternCached(urls.get(position))) {
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                }
                tv.setText(content);
                return tv;
            }
        };
        ListView listView = (ListView) findViewById(R.id.importVersionsListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImportPatternActivity.this.onItemClick(position);
            }
        });

        search();
    }


    private void onItemClick(int position) {
        final String url = urls.get(position);
        showProgressBar();
        m_chordPatternImporter.getChordPattern(getApplicationContext(), url, new ChordPatternImporter.OnResult<String>() {
            @Override
            public void onSuccess(String pattern) {
                if (pattern != null) {
                    Intent intent = new Intent(ImportPatternActivity.this, PatternPreviewActivity.class);
                    intent.putExtra("pattern", pattern);
                    ImportPatternActivity.this.startActivityForResult(intent, MainActivity.IMPORT_PATTERN_PREVIEW_REQUEST);
                }
                hideProgressBar();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(ImportPatternActivity.this, "Failed to download pattern.", Toast.LENGTH_SHORT).show();
                hideProgressBar();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.IMPORT_PATTERN_PREVIEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("pattern", data.getStringExtra("pattern"));
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // user does not like the pattern. Do nothing.
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void search() {
        final String key = ((EditText) findViewById(R.id.importSearchKeywordEdit)).getText().toString();
        showProgressBar();
        m_chordPatternImporter.getSearchResults(getApplicationContext(), key, 5,
                new ChordPatternImporter.OnResult<List<ChordPatternImporter.SearchResult>>() {
                    @Override
                    public void onSuccess(List<ChordPatternImporter.SearchResult> results) {
                        items.clear();
                        urls.clear();
                        for (ChordPatternImporter.SearchResult e : results) {
                            final String label = e.type + ": " + e.name + " - " + e.artist;
                            items.add(label);
                            urls.add(e.url);
                        }
                        adapter.notifyDataSetChanged();
                        hideProgressBar();
                    }

                    @Override
                    public void onFail(String error) {
                        items.clear();
                        urls.clear();
                        Toast.makeText(ImportPatternActivity.this, R.string.nothing_found_search_songs, Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        hideProgressBar();
                    }
                });
    }
}

