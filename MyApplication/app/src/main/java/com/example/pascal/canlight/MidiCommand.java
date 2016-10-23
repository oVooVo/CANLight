package com.example.pascal.canlight;

import android.os.Parcel;
import android.os.Parcelable;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pascal on 23.10.16.
 */
public class MidiCommand implements Parcelable {
    private boolean mIsValid;
    private byte mStatus;
    private byte mData1;
    private byte mData2;

    public MidiCommand() {
        mIsValid = false;
    }

    public MidiCommand(boolean isEnabled, byte status, byte data1, byte data2) {
        mIsValid = isEnabled;
        mStatus = status;
        mData1 = data1;
        mData2 = data2;
    }

    public MidiCommand(Parcel in) {
        mIsValid = in.readInt() == 0;
        mStatus = in.readByte();
        mData1 = in.readByte();
        mData2 = in.readByte();
    }

    public boolean isValid() {
        return mIsValid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIsValid ? 0 : 1);
        dest.writeByte(mStatus);
        dest.writeByte(mData1);
        dest.writeByte(mData2);
    }

    public static final Parcelable.Creator<MidiCommand> CREATOR
            = new Parcelable.Creator<MidiCommand>() {
        public MidiCommand createFromParcel(Parcel in) {
            return new MidiCommand(in);
        }

        @Override
        public MidiCommand[] newArray(int size) {
            return new MidiCommand[0];
        }
    };

    void fromJson(JSONObject o) {
        try {
            mIsValid = o.getBoolean("isValid");
            mStatus = (byte) o.getInt("status");
            mData1 = (byte) o.getInt("data1");
            mData2 = (byte) o.getInt("data2");
        } catch (JSONException e) {
            // ignore
        }
    }

    JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("isValid", mIsValid);
            o.put("status", mStatus);
            o.put("data1", mData1);
            o.put("data2", mData2);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return o;
    }

    public byte getStatusByte() {
        return mStatus;
    }

    public byte getDataByte1() {
        return mData1;
    }

    public byte getDataByte2() {
        return mData2;
    }
}
