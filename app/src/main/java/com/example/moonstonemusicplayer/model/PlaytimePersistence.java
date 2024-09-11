package com.example.moonstonemusicplayer.model;

import android.content.Context;
import android.content.SharedPreferences;

public class PlaytimePersistence {

    /** Save playtime to SharedPreferences
     *
     * @param context
     * @param audioFileName
     * @param playtimeInSeconds
     */
    public static void savePlaytime(Context context, String audioFileName, int playtimeInSeconds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PlaytimePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Save the playtime with the audio file name as the key
        editor.putInt(audioFileName, playtimeInSeconds);
        editor.apply(); // Apply changes
    }

    /** Retrieve playtime from SharedPreferences
     *
     * @param context
     * @param audioFileName
     * @return
     */
    public static int getPlaytime(Context context, String audioFileName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("PlaytimePrefs", Context.MODE_PRIVATE);

        // Get the saved playtime, default to 0 if no value is found
        return sharedPreferences.getInt(audioFileName, 0);
    }

}
