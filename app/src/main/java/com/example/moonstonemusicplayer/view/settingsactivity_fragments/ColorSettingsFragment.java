package com.example.moonstonemusicplayer.view.settingsactivity_fragments;

import android.os.Bundle;


import com.github.koston.preference.ColorPreferenceFragmentCompat;
import com.example.moonstonemusicplayer.R;

public class ColorSettingsFragment extends ColorPreferenceFragmentCompat{

    public static final String COLOR_PRIMARY = "colorPrimary";
    public static final String COLOR_PRIMARY_DARK = "colorPrimaryDark";
    public static final String COLOR_ACCENT = "colorAccent";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.color_preferences);
    }
}