package com.example.pascal.canlight.googleDrive;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.pascal.canlight.ImportExportActivity;
import com.example.pascal.canlight.Project;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.example.pascal.canlight.R;

public class ReceiverActivity extends GoogleDriveActivity {

    private static final int IMPORT_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
    }

    final private ResultCallback<DriveApi.DriveIdResult> idCallback = new ResultCallback<DriveApi.DriveIdResult>() {
        @Override
        public void onResult(DriveApi.DriveIdResult result) {
            final DriveId id = result.getDriveId();
            if (id != null) {
                new RetrieveDriveFileContentsAsyncTask(ReceiverActivity.this)
                        .execute(result.getDriveId());
            } else {
                Toast.makeText(ReceiverActivity.this,
                        R.string.google_drive_cannot_get_drive_id,
                        Toast.LENGTH_LONG)
                        .show();
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    };

    final private class RetrieveDriveFileContentsAsyncTask
            extends GoogleDriveApiClientAsyncTask<DriveId, Boolean, String> {

        public RetrieveDriveFileContentsAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {
            String contents = null;
            DriveFile file = params[0].asDriveFile();
            DriveApi.DriveContentsResult driveContentsResult =
                    file.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.w(LOG_TAG, "drive contents result no success");
                return null;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(driveContents.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                contents = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException while reading from the stream", e);
            }

            driveContents.discard(getGoogleApiClient());
            return contents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                showMessage(getString(R.string.error_read_file));
                setResult(RESULT_CANCELED);
                finish();
            } else {
                final Project project = new Project();
                try {
                    project.fromJson(new JSONObject(result));
                } catch (JSONException e) {
                    throw new AssertionFailedError();
                }

                Intent intent = new Intent(ReceiverActivity.this, ImportExportActivity.class);
                intent.putExtra("ImportedProject", project);
                intent.putExtra("project", getIntent().getParcelableExtra("project"));
                intent.putExtra("export", false);
                ReceiverActivity.this.startActivityForResult(intent, IMPORT_REQUEST);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMPORT_REQUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent();
                    intent.putExtra("MergedProject", data.getParcelableExtra("MergedProject"));
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        final String id = getIntent().getStringExtra("id");
        Drive.DriveApi.fetchDriveId(getGoogleApiClient(), id)
                    .setResultCallback(idCallback);
    }
}
