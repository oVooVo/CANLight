package com.example.pascal.canlight;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;

import junit.framework.AssertionFailedError;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;

public class GoogleDriveCreateFileActivity extends GoogleDriveActivity {
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_create_file);
        content = getIntent().getStringExtra("content");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);

        final MetadataChangeSet meta = new MetadataChangeSet.Builder()
                .setTitle("New file")
                .setMimeType("application/json")
                .setStarred(true).build();

        Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                DriveContents contents = driveContentsResult.getDriveContents();
                OutputStream s = contents.getOutputStream();
                Writer writer = new OutputStreamWriter(s);
                try {
                    writer.write(content);
                    writer.close();
                } catch (IOException e) {
                    throw new AssertionFailedError();
                }

                // Create an empty file on root folder.
                Drive.DriveApi.getAppFolder(getGoogleApiClient())
                        .createFile(getGoogleApiClient(), meta, contents)
                        .setResultCallback(fileCallback);
            }
        });


    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    final DriveId id = result.getDriveFile().getDriveId();
                    showMessage("Created an empty file: " + id);
                    id.asDriveFile().addChangeListener(getGoogleApiClient(), new ChangeListener() {

                        @Override
                        public void onChange(ChangeEvent changeEvent) {
                            String id = changeEvent.getDriveId().getResourceId();
                            if (id != null) {
                                Intent shareIdIntent = new Intent(Intent.ACTION_SEND);
                                shareIdIntent.setType("text/plain");
                                //i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");

                                try {
                                    id = URLEncoder.encode(id, "utf-8");
                                } catch (UnsupportedEncodingException e) {
                                    throw new AssertionFailedError();
                                }
                                shareIdIntent.putExtra(Intent.EXTRA_TEXT, "http://canlight.com/rcv_shr/" + id);
                                startActivity(Intent.createChooser(shareIdIntent, "Share CANLight collection"));
                            }
                            finish();
                        }
                    });
                }
            };
}