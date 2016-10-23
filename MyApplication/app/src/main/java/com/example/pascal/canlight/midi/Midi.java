package com.example.pascal.canlight.midi;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.example.pascal.canlight.MidiCommand;

/**
 * Created by pascal on 23.10.16.
 */
public abstract class Midi {
    private static final String TAG = "Midi";

    private int mChannel;
    private static Midi mSingleton;
    private static Context mContext;

    protected static void log(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void init(Context context) {
        mContext = context;
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            // do MIDI stuff
            //log("Init native midi");
            mSingleton = new NativeMidi(context);
        } else {
            //log("Init midi stub");
            mSingleton = new Midi() {
                @Override
                public void send(byte statusByte, byte commandByte1, byte commandByte2) {
                    log("Your android version does not support MIDI.");
                }
            };
        }
    }

    public static Midi getInstance() {
        return mSingleton;
    }

    protected Midi() {

    }

    public int getChannel() {
        return mChannel;
    }

    public void setChannel(int channel) {
        mChannel = channel;
    }

    public void send(MidiCommand cmd) {
        if (cmd.isValid()) {
            //send(cmd.getStatusByte(), cmd.getDataByte1(), cmd.getDataByte2());
        }
    }

    public abstract void send(byte statusByte, byte commandByte1, byte commandByte2);

}
