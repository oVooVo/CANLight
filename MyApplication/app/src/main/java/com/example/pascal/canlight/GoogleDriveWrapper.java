package com.example.pascal.canlight;
public class GoogleDriveWrapper {}

/*
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.services.drive.Drive;

public class GoogleDriveWrapper extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final GoogleApiClient mClient;
    private final Activity mActivity;
    public GoogleDriveWrapper(Activity activity) {
        this.mActivity = activity;
        mClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivity, MainActivity.LOGIN_GOOGLE_DRIVE_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    private static GoogleDriveWrapper instance;
    public static void initialize(Context context) {
        instance = new GoogleDriveWrapper(context);
    }

    public static GoogleDriveWrapper getInstance() {
        return instance;
    }

    public static GoogleApiClient getClient() {
        return getInstance().mClient;
    }
}*/