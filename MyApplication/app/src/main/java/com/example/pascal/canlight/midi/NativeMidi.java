package com.example.pascal.canlight.midi;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

/**
 * Created by pascal on 23.10.16.
 */
public class NativeMidi extends Midi {
    private static final String TAG = "NativeMidi";
    //private final MidiManager mMidiManager;
    private MidiInputPort mInputPort;

    @TargetApi(Build.VERSION_CODES.M)
    protected NativeMidi(Context context) {
        /*
        mMidiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        final Handler h = new Handler();
        new Runnable() {
            @Override
            public void run() {
                lookForDevices();
                h.postDelayed(this, 1000);
            }
        }.run();
        */
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void send(byte statusByte, byte commandByte1, byte commandByte2) {
        /*
        if (mInputPort != null) {
            try {
                log("sent midi command");
                mInputPort.send(new byte[]{statusByte, commandByte1, commandByte2}, 0, 3);
            } catch (IOException e) {
                log("IOException during send.");
                e.printStackTrace();
            }
        } else {
            log("No active input port, cannot send.");
        }
        */
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void lookForDevices() {
        /*
        MidiDeviceInfo[] devices = mMidiManager.getDevices();
        if (devices.length == 0) {
            log("No device found.");
            mInputPort = null;
        } else {
            MidiDeviceInfo mdi = devices[0];
            log("Found " + devices.length + " devices. Pick the first one.");
            if (devices[0].getInputPortCount() == 0) {
                log("Device has no ports.");
                mInputPort = null;
            } else {
                log("Found " + devices.length + " devices. Pick the first one.");
                mMidiManager.openDevice(mdi, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice device) {
                        Log.i(TAG, "Opened input port");
                        mInputPort = device.openInputPort(0);
                    }
                }, new Handler(Looper.getMainLooper()));
            }
        }
        */
    }
}
