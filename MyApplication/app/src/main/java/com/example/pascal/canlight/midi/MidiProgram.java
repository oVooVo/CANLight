package com.example.pascal.canlight.midi;

import android.os.Parcel;
import android.os.Parcelable;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pascal on 23.10.16.
 */
public class MidiProgram implements Parcelable {
    private boolean mIsValid;
    private int mBank;
    private int mPage;
    private int mProgram;

    public MidiProgram() {
        mIsValid = false;
    }

    public MidiProgram(boolean isEnabled, int bank, int page, int program) {
        mIsValid = isEnabled;
        mBank = bank;
        mPage = page;
        mProgram = program;
    }

    public MidiProgram(Parcel in) {
        mIsValid = in.readInt() == 0;
        mBank = in.readInt();
        mPage = in.readInt();
        mProgram = in.readInt();
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
        dest.writeInt(mBank);
        dest.writeInt(mPage);
        dest.writeInt(mProgram);
    }

    public static final Parcelable.Creator<MidiProgram> CREATOR
            = new Parcelable.Creator<MidiProgram>() {
        public MidiProgram createFromParcel(Parcel in) {
            return new MidiProgram(in);
        }

        @Override
        public MidiProgram[] newArray(int size) {
            return new MidiProgram[0];
        }
    };

    public void fromJson(JSONObject o) {
        try {
            mIsValid = o.getBoolean("isValid");
            mBank = o.getInt("bank");
            mPage = o.getInt("page");
            mProgram = o.getInt("program");
        } catch (JSONException e) {
            // ignore
        }
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("isValid", mIsValid);
            o.put("bank", mBank);
            o.put("page", mPage);
            o.put("program", mProgram);
        } catch (JSONException e) {
            throw new AssertionFailedError();
        }
        return o;
    }

    public int getBank() {
        return mBank;
    }

    public int getPage() {
        return mPage;
    }

    public int getProgram() {
        return mProgram;
    }
}
