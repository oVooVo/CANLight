package com.example.pascal.canlight;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.example.pascal.canlight.chordPattern.ImportPatternCache;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    //@see http://stackoverflow.com/a/18807490/4248972
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_clear_cache_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ImportPatternCache.clear();
                updatePreference(preference);
                return true;
            }
        });
        findPreference(getString(R.string.pref_loggin_spotify_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MySpotify.loginRequest(getActivity());
                return true;
            }
        });
        /*
        findPreference(getString(R.string.pref_loggin_google_drive_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                GoogleDriveWrapper.loginRequest(getActivity());
                return true;
            }
        });
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                    Preference singlePref = preferenceGroup.getPreference(j);
                    updatePreference(singlePref);
                }
            } else {
                updatePreference(preference);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        final Preference preference = findPreference(key);
        updatePreference(preference);
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference lp = (ListPreference) preference;
            lp.setSummary(lp.getEntry());
        } else if (preference != null) {
            if (preference.getKey().equals(getActivity().getString(R.string.pref_clear_cache_key))) {
                final int size = ImportPatternCache.computeSizeInKB();
                final int count = ImportPatternCache.numberOfItems();
                String text = getResources().getQuantityString(R.plurals.importCacheCount, count, count);
                text += getResources().getQuantityString(R.plurals.importCacheSize, size, size);
                preference.setSummary(text);
            }
        }
    }
}