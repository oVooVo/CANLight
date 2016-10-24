package com.example.pascal.canlight.midi;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.kshoji.driver.midi.device.MidiDeviceConnectionWatcher;
import jp.kshoji.driver.midi.device.MidiInputDevice;
import jp.kshoji.driver.midi.device.MidiOutputDevice;
import jp.kshoji.driver.midi.listener.OnMidiDeviceAttachedListener;
import jp.kshoji.driver.midi.listener.OnMidiDeviceDetachedListener;

/**
 * Created by pascal on 23.10.16.
 */
public class Midi {
    private Context mContext;
    private UsbManager mUsbManager;
    private static Midi mInstance;

    public static Midi getInstance() {
        return mInstance;
    }

    public static void init(Activity activity) {
        mInstance = new Midi(activity);
    }

    protected Midi(Activity activity) {
        mContext = activity;
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        List<String> ids = new ArrayList<>();
        for (String id : mUsbManager.getDeviceList().keySet()) {
            ids.add(id);
        }
        if (!ids.isEmpty()) {
            final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
            UsbDevice device = (UsbDevice) mUsbManager.getDeviceList().values().toArray()[0];
            PendingIntent pi = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(device, pi);
        }
    }

    public void sendMidiProgram(final MidiProgram midiProgram) {
        if (!midiProgram.isValid()) {
            return;
        }
        new MidiDeviceConnectionWatcher(mContext, mUsbManager, new OnMidiDeviceAttachedListener() {
            @Override
            public void onDeviceAttached(@NonNull UsbDevice usbDevice) {

            }

            @Override
            public void onMidiInputDeviceAttached(@NonNull MidiInputDevice midiInputDevice) {

            }

            @Override
            public void onMidiOutputDeviceAttached(@NonNull MidiOutputDevice midiOutputDevice) {
                final int program = midiProgram.getProgram() + 5 * midiProgram.getPage();
                midiOutputDevice.sendMidiControlChange(0, 1, 32, midiProgram.getBank());
                midiOutputDevice.sendMidiProgramChange(0, 1, program);
            }
        },
        new OnMidiDeviceDetachedListener() {
            @Override
            public void onDeviceDetached(@NonNull UsbDevice usbDevice) {

            }

            @Override
            public void onMidiInputDeviceDetached(@NonNull MidiInputDevice midiInputDevice) {

            }

            @Override
            public void onMidiOutputDeviceDetached(@NonNull MidiOutputDevice midiOutputDevice) {

            }
        });
    }
}
