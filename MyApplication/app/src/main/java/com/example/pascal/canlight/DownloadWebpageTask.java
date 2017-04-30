package com.example.pascal.canlight;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by pascal on 05.10.16.
 */
public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

    public String userAgent;
    public int readTimeout;
    public int connectTimeout;

    @Override
    protected String doInBackground(String... urls) {
        try {
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            return "";
        }
    }

    private String downloadUrl(String url)
            throws IOException {
        InputStream is = null;
        try {
            System.setProperty("http.agent", userAgent);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setReadTimeout(readTimeout);
            conn.setConnectTimeout(connectTimeout);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return IOUtils.toString(is);

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}

