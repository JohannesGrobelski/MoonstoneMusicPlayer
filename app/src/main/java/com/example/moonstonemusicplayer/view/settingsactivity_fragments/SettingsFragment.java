package com.example.moonstonemusicplayer.view.settingsactivity_fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.moonstonemusicplayer.controller.SettingsActivity.SettingsFragmentListener;
import com.github.koston.preference.ColorPreferenceFragmentCompat;
import com.github.koston.preference.ColorPreference;

import com.example.moonstonemusicplayer.R;

public class SettingsFragment extends ColorPreferenceFragmentCompat {

    public static final String COLOR_PRIMARY = "colorPrimary";
    public static final String COLOR_PRIMARY_DARK = "colorPrimaryDark";
    public static final String COLOR_ACCENT = "colorAccent";
    public static final String EXPORT_PLAYLISTS_DEVICE = "ExportPlaylistsDevice";
    public static final String IMPORT_PLAYLISTS_DEVICE = "ImportPlaylistsDevice";
    public static final String SIGN_INTO_GDRIVE = "SignIntoGDrive";
    public static final String EXPORT_PLAYLISTS_GDRIVE = "ExportPlaylistsGDrive";
    public static final String IMPORT_PLAYLISTS_GDRIVE = "ImportPlaylistsGDrive";

    public ColorPreference primaryColorPreference;
    public ColorPreference primaryDarkColorPreference;
    public ColorPreference accentColorPreference;

    public Preference importPlaylistsDevicePreference;
    public Preference exportPlaylistsDevicePreference;
    public Preference signIntoGdrivePreference;
    public Preference exportPlaylistsGdrivePreference;
    public Preference importPlaylistsGdrivePreference;

    private SettingsFragmentListener settingsFragmentListener;

    private ActivityResultLauncher<Intent> importPlaylistLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register activity result launcher
        importPlaylistLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        settingsFragmentListener.handleImportPlaylistResult(result.getData());
                    }
                }
        );


    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        //init color preferences
        primaryColorPreference = findPreference(COLOR_PRIMARY);
        primaryDarkColorPreference = findPreference(COLOR_PRIMARY_DARK);
        accentColorPreference = findPreference(COLOR_ACCENT);
        importPlaylistsDevicePreference = findPreference(IMPORT_PLAYLISTS_DEVICE);
        exportPlaylistsDevicePreference = findPreference(EXPORT_PLAYLISTS_DEVICE);
        signIntoGdrivePreference = findPreference(SIGN_INTO_GDRIVE);
        exportPlaylistsGdrivePreference = findPreference(IMPORT_PLAYLISTS_GDRIVE);
        importPlaylistsGdrivePreference = findPreference(EXPORT_PLAYLISTS_GDRIVE);

        settingsFragmentListener = new SettingsFragmentListener(this);
    }


    /**
     * Returns the primary color.
     * @return The value of COLOR_PRIMARY.
     */
    public static int getPrimaryColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(COLOR_PRIMARY, R.color.colorPrimary);
    }

    /**
     * Returns the darkened primary color.
     * @return The value of COLOR_PRIMARY_DARK.
     */
    public static int getPrimaryDarkColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(COLOR_PRIMARY_DARK, R.color.colorPrimaryDark);
    }

    /**
     * Returns the accent color.
     * @return The value of COLOR_ACCENT.
     */
    public static int getAccentColor(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(COLOR_ACCENT, R.color.colorAccent);
    }

    public ActivityResultLauncher<Intent> getImportPlaylistLauncher() {
        return importPlaylistLauncher;
    }

    public void handleSignInResult(Intent data) {
        settingsFragmentListener.handleSignInResult(data);
    }
}
