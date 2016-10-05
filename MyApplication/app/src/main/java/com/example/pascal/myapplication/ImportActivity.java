package com.example.pascal.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ImportActivity extends AppCompatActivity {

    ArrayList<String> items;
    ArrayList<URL> urls;
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
        getSupportActionBar().setTitle("Import Pattern");

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

        search();
    }

    private static final String URL_PATTERN = "https://www.ultimate-guitar.com/search.php?search_type=title&order=&value=";

    private void search() {
        final String key = ((EditText) findViewById(R.id.importSearchKeywordEdit)).getText().toString();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            final String url;
            try {
                url = URL_PATTERN + URLEncoder.encode(key, "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionFailedError();
            }
            new DownloadWebpageTask().execute(url);

            showProgressBar();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            ParseSearchResults parser = new ParseSearchResults(result);
            items.clear();
            items.addAll(parser.items());
            urls = parser.urls();
            adapter.notifyDataSetChanged();

            if (items.isEmpty()) {
                Toast.makeText(ImportActivity.this, "Nothing found", Toast.LENGTH_SHORT).show();
            }

            hideProgressBar();
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = IOUtils.toString(is);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

}

