/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PlaylistJsonHandler {
    private static final String TAG = PlaylistJsonHandler.class.getSimpleName();
    private static final String EXPORT_DIRECTORY = "MoonstoneMusic/Playlists";

    // In PlaylistJsonHandler.java
    public static void exportPlaylists(Context context, List<Playlist> playlists) {
        try {
            JSONArray playlistsArray = new JSONArray();

            for (Playlist playlist : playlists) {

                // make sure not to export special playlists like recently added
                if(playlist.getName().equals(RECENTLY_ADDED_PLAYLIST_NAME)
                || playlist.getName().equals(RECENTLY_PLAYED_PLAYLIST_NAME)
                || playlist.getName().equals(MOSTLY_PLAYED_PLAYLIST_NAME)){
                    continue;
                }
                JSONObject playlistObj = new JSONObject();
                playlistObj.put("name", playlist.getName());

                JSONArray songsArray = new JSONArray();
                for (Song song : playlist.getPlaylist()) {
                    JSONObject songObj = new JSONObject();
                    songObj.put("path", song.getPath() != null ? song.getPath() : "");
                    songObj.put("name", song.getName() != null ? song.getName() : "");
                    songObj.put("artist", song.getArtist() != null ? song.getArtist() : "");
                    songObj.put("album", song.getAlbum() != null ? song.getAlbum() : "");
                    songObj.put("genre", song.getGenre() != null ? song.getGenre() : "");
                    songObj.put("duration_ms", song.getDuration_ms());
                    songObj.put("lyrics", song.getLyrics() != null ? song.getLyrics() : "");
                    songsArray.put(songObj);
                }

                playlistObj.put("songs", songsArray);
                playlistsArray.put(playlistObj);
            }

            String jsonContent = playlistsArray.toString(4).replace("\\/", "/"); // Pretty print with 4-space indentation
            String filename = "moonstone_playlists_backup.json";


            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/json");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/MoonstoneMusic/Playlists");

            ContentResolver resolver = context.getContentResolver();
            Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);

            if (uri != null) {
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    if (os != null) {
                        os.write(jsonContent.getBytes());
                        os.flush();
                        Toast.makeText(context, "Playlists exported to Documents/MoonstoneMusic/Playlists/" + filename,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            Log.d(TAG, "All playlists exported successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error exporting playlists: " + e.getMessage());
            throw new RuntimeException("Failed to export playlists", e);
        }
    }
    public static void importPlaylists(Context context, File jsonFile) {
        try {
            // Read JSON file
            StringBuilder content = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();

            JSONArray playlistsArray = new JSONArray(content.toString());
            DBPlaylists dbPlaylists = DBPlaylists.getInstance(context);

            // Get existing playlist names
            Set<String> existingNames = new HashSet<>();
            for (String name : dbPlaylists.getAllPlaylistNames()) {
                existingNames.add(name.toLowerCase());
            }

            // Process each playlist
            for (int i = 0; i < playlistsArray.length(); i++) {
                JSONObject playlistObj = playlistsArray.getJSONObject(i);
                String originalName = playlistObj.getString("name");
                String playlistName = getUniquePlaylistName(originalName, existingNames);

                // make sure not to import special playlists like recently added
                if(playlistName.equals(RECENTLY_ADDED_PLAYLIST_NAME)
                        || playlistName.equals(RECENTLY_PLAYED_PLAYLIST_NAME)
                        || playlistName.equals(MOSTLY_PLAYED_PLAYLIST_NAME)){
                    continue;
                }

                // Add the new name to our set of existing names
                existingNames.add(playlistName.toLowerCase());

                JSONArray songsArray = playlistObj.getJSONArray("songs");

                // Add each song to the playlist
                for (int j = 0; j < songsArray.length(); j++) {
                    JSONObject songObj = songsArray.getJSONObject(j);

                    Song song = new Song(
                            songObj.getString("path"),
                            songObj.getString("name"),
                            songObj.getString("artist"),
                            songObj.getString("album"),
                            songObj.getString("genre"),
                            songObj.getInt("duration_ms"),
                            songObj.getString("lyrics")
                    );

                    dbPlaylists.addToPlaylist(context, song, playlistName);
                }
            }

            Log.d(TAG, "All playlists imported successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error importing playlists: " + e.getMessage());
            throw new RuntimeException("Failed to import playlists", e);
        }
    }

    private static String getUniquePlaylistName(String baseName, Set<String> existingNames) {
        if (!existingNames.contains(baseName.toLowerCase())) {
            return baseName;
        }

        int counter = 1;
        String newName;
        do {
            newName = baseName + " (" + counter + ")";
            counter++;
        } while (existingNames.contains(newName.toLowerCase()));

        return newName;
    }
}
