package com.example.pascal.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.AssertionFailedError;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ImportActivity extends AppCompatActivity {

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
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.BLUE);
                return view;
            }
        };
        ListView listView = (ListView) findViewById(R.id.importVersionsListView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImportActivity.this.onItemClick(position);
            }
        });

        search();
    }


    private void onItemClick(int position) {
        final String url = urls.get(position);
        showProgressBar();
        new Importer.Pattern(url, getApplicationContext()) {
            @Override
            void onPatternArrived(String pattern) {
                hideProgressBar();
                if (pattern != null) {
                    Intent intent = new Intent(ImportActivity.this, PatternPreviewActivity.class);
                    intent.putExtra("pattern", pattern);
                    ImportActivity.this.startActivityForResult(intent, MainActivity.IMPORT_PATTERN_PREVIEW_REQUEST);
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
    }


    private void search() {
        final String key = ((EditText) findViewById(R.id.importSearchKeywordEdit)).getText().toString();
        showProgressBar();
        new Importer.SearchResults(key, getApplicationContext()) {
            @Override
            void onSearchResultsArrived(Importer.SearchResult[] results) {
                items.clear();
                urls.clear();
                for (Importer.SearchResult e : results) {
                    String label = e.type + ": " + e.name + " - " + e.artist;
                    items.add(label);
                    urls.add(e.url);
                }
                adapter.notifyDataSetChanged();

                if (items.isEmpty()) {
                    Toast.makeText(ImportActivity.this, "Nothing found", Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        };

    }


}

