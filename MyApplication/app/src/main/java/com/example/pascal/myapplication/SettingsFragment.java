package com.example.pascal.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    //@see http://stackoverflow.com/a/18807490/4248972
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference("clear_cache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ImportCache.clear();
                updatePreference(preference);
                return true;
            }
        });
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
        if (preference.getKey().equals("clear_cache")) {
            Toast.makeText(getActivity().getApplicationContext(), "Hello World", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof ListPreference) {
            ListPreference lp = (ListPreference) preference;
            lp.setSummary(lp.getEntry());
        } else if (preference.getKey().equals("clear_cache")) {
            final int size = ImportCache.computeSizeInKB();
            final int count = ImportCache.numberOfItems();
            String text = getResources().getQuantityString(R.plurals.importCacheCount, count, count);
            text += getResources().getQuantityString(R.plurals.importCacheSize, size, size);
            preference.setSummary(text);
        }
    }
}