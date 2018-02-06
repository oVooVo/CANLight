package com.example.pascal.canlight;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.example.pascal.canlight.chordPattern.ImportPatternCache;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    //@see http://stackoverflow.com/a/18807490/4248972

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_clear_cache_key)).setOnPreferenceClickListener(preference -> {
            ImportPatternCache.clear();
            updatePreference(preference);
            return true;
        });


        findPreference(getString(R.string.pref_spotify_login_key)).setOnPreferenceClickListener(preference -> {
            MySpotify.spotifyConnectRequest(getActivity());
            return true;
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        assert  getView() != null;
        updateAllPreferences();
    }

    private void updateAllPreferences() {
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
        if (preference != null) {
            if (getActivity().getString(R.string.pref_clear_cache_key).equals(preference.getKey())) {
                final int size = ImportPatternCache.computeSizeInKB();
                final int count = ImportPatternCache.numberOfItems();
                String text = getResources().getQuantityString(R.plurals.importCacheCount, count, count);
                text += getResources().getQuantityString(R.plurals.importCacheSize, size, size);
                preference.setSummary(text);
            }
        }
    }
}