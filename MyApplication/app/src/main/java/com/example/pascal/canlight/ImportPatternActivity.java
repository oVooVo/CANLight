package com.example.pascal.canlight;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ImportPatternActivity extends AppCompatActivity {

    ArrayList<String> items;
    ArrayList<String> urls;
    ArrayAdapter<String> adapter;

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
            getSupportActionBar().setTitle("Import Pattern");
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
        adapter = new ArrayAdapter<String>(this, R.layout.search_song_list_item, R.id.search_song_item_name, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                ImageView imageView = (ImageView) view.findViewById(R.id.search_song_item_icon);
                if (ImportPatternCache.isPatternCached(urls.get(position))) {
                    imageView.setImageResource(android.R.drawable.btn_star_big_on);
                } else {
                    imageView.setImageResource(android.R.drawable.btn_star_big_off);
                }
                return view;
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
        new PatternImporter.Pattern(url, getApplicationContext()) {
            @Override
            void onPatternArrived(String pattern) {
                hideProgressBar();
                if (pattern != null) {
                    Intent intent = new Intent(ImportPatternActivity.this, PatternPreviewActivity.class);
                    intent.putExtra("pattern", pattern);
                    ImportPatternActivity.this.startActivityForResult(intent, MainActivity.IMPORT_PATTERN_PREVIEW_REQUEST);
                }
            }
        };
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
        new PatternImporter.SearchResults(key, getApplicationContext()) {
            @Override
            void onSearchResultsArrived(PatternImporter.SearchResult[] results) {
                items.clear();
                urls.clear();
                for (PatternImporter.SearchResult e : results) {
                    String label = e.type + ": " + e.name + " - " + e.artist;
                    items.add(label);
                    urls.add(e.url);
                }
                adapter.notifyDataSetChanged();

                if (items.isEmpty()) {
                    Toast.makeText(ImportPatternActivity.this, R.string.nothing_found_search_songs, Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        };

    }


}

