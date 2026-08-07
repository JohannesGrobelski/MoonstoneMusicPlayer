package com.example.moonstonemusicplayer.controller.SettingsActivity;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;

import androidx.preference.Preference;

import com.example.moonstonemusicplayer.controller.PlayListActivity.PlaylistJsonHandler;
import com.example.moonstonemusicplayer.model.Database.PlaylistUtil;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class SettingsFragmentListener {


    
    private final SettingsFragment settingsFragment;

    public SettingsFragmentListener(SettingsFragment settingsFragment) {
        this.settingsFragment = settingsFragment;
        initPreferences();
    }

    private void initPreferences() {
        if (settingsFragment.primaryColorPreference != null) {
            //update primary_dark and accent color based on value of primary color to create a fitting theme based on primary color
            settingsFragment.primaryColorPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue instanceof Integer) {
                    int primaryColor = (Integer) newValue;

                    // Update COLOR_PRIMARY_DARK
                    int darkerColor = darkenColor(primaryColor);
                    settingsFragment.primaryDarkColorPreference.setColor(darkerColor);

                    // Update COLOR_ACCENT
                    int accentColor = calculateAccentColor(primaryColor);
                    settingsFragment.accentColorPreference.setColor(accentColor);

                    return true; // Change accepted
                }
                return false; // Invalid value
            });
        }

        //init playlist actions
        settingsFragment.importPlaylistsDevicePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                handleImportPlaylists();
                return true;
            }
        });
        settingsFragment.exportPlaylistsDevicePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference){
                handleExportPlaylists();
                return true;
            }
        });
    }


    /**
     * Darkens a given color by reducing its brightness.
     *
     * @param color The original color.
     * @return The darkened color.
     */
    private int darkenColor(int color) {
        float factor = 0.7f; // Adjust this factor to control the darkening level
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.rgb(r, g, b);
    }

    /**
     * Calculates an accent color based on the primary color.
     *
     * @param color The primary color.
     * @return The accent color.
     */
    private int calculateAccentColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Example: Calculate a complementary hue
        float newHue = (hsv[0] + 10) % 360;

        // Adjust saturation and value as needed
        hsv[1] = Math.max(0.2f, Math.min(0.8f, hsv[1] * 0.8f)); // Reduce saturation slightly
        hsv[2] = Math.max(0.2f, Math.min(0.8f, hsv[2] * 1.4f)); // Slightly increase brightness

        hsv[0] = newHue;
        return Color.HSVToColor(hsv);
    }

    private void handleImportPlaylists() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            settingsFragment.getImportPlaylistLauncher().launch(
                    Intent.createChooser(intent, "Select playlist file")
            );
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(settingsFragment.getContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleImportPlaylistResult(Intent data) {
        try {
            Uri uri = data.getData();
            InputStream inputStream = settingsFragment.getActivity().getContentResolver().openInputStream(uri);
            File tempFile = createTempFileFromInputStream(inputStream);

            PlaylistJsonHandler.importPlaylists(settingsFragment.getContext(), tempFile);
            Toast.makeText(settingsFragment.getContext(), "Playlists imported successfully", Toast.LENGTH_SHORT).show();

            // Cleanup temp file
            tempFile.delete();
        } catch (Exception e) {
            Toast.makeText(settingsFragment.getContext(), "Failed to import playlists", Toast.LENGTH_SHORT).show();
            Timber.e( "Import failed: " + e.getMessage());
        }
    }


    private File createTempFileFromInputStream(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("playlist_import", ".json", settingsFragment.getContext().getCacheDir());

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        FileOutputStream fos = new FileOutputStream(tempFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] buffer = new byte[4096];
        int count;
        while ((count = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, count);
        }

        bos.flush();
        bos.close();
        fos.close();
        bis.close();
        inputStream.close();

        return tempFile;
    }

    private void handleExportPlaylists() {
        try {
            List<Playlist> playlists = PlaylistUtil.getAllPlaylists(settingsFragment.getActivity());
            playlists = playlists.stream().filter(playlist -> !playlist.getName().equals(RECENTLY_ADDED_PLAYLIST_NAME) && !playlist.getName().equals(RECENTLY_PLAYED_PLAYLIST_NAME)).collect(Collectors.toList());
            PlaylistJsonHandler.exportPlaylists(settingsFragment.getActivity(), playlists);
            Toast.makeText(settingsFragment.getActivity(), "Playlists exported successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(settingsFragment.getActivity(), "Failed to export playlists", Toast.LENGTH_SHORT).show();
            Timber.e( "Export failed: " + e.getMessage());
        }
    }
}
