package com.example.moonstonemusicplayer.view.settingsactivity_fragments;

import android.os.Bundle;
import android.graphics.Color;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.github.koston.preference.ColorPreferenceFragmentCompat;
import com.github.koston.preference.ColorPreference;
import com.example.moonstonemusicplayer.R;

public class ColorSettingsFragment extends ColorPreferenceFragmentCompat {

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

        // Add listener to COLOR_PRIMARY
        ColorPreference primaryColorPreference = findPreference(COLOR_PRIMARY);
        ColorPreference primaryDarkColorPreference = findPreference(COLOR_PRIMARY_DARK);
        ColorPreference accentColorPreference = findPreference(COLOR_ACCENT);
        if (primaryColorPreference != null) {
            primaryColorPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (newValue instanceof Integer) {
                    int primaryColor = (Integer) newValue;

                    // Update COLOR_PRIMARY_DARK
                    int darkerColor = darkenColor(primaryColor);
                    primaryDarkColorPreference.setColor(darkerColor);

                    // Update COLOR_ACCENT
                    int accentColor = calculateAccentColor(primaryColor);
                    accentColorPreference.setColor(accentColor);

                    return true; // Change accepted
                }
                return false; // Invalid value
            });
        }
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
}
