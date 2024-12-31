/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.Database;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.FAVORITES_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playcountlist.DBPlaycountList;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaylistUtil {
    private static final String TAG = DBPlaylists.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static List<Playlist> getAllPlaylists(Context context) {
        try {
            List<Playlist> playlists = new LinkedList<>(DBPlaylists.getInstance(context).getAllPlaylists(context));
            return sortPlaylists(playlists);
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return List.of();
        }
    }

    public static Playlist getPlaylistMostlyPlayed(Context context){
        return new Playlist(DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME, DBPlaycountList.getInstance(context).getMostlyPlayed());
    }

    /**
     * Sorts playlists to ensure Favorites, Recently Played, and Mostly Played appear first in that order.
     *
     * @param playlistList List of playlists to sort
     * @return Sorted list with special playlists at the beginning
     * @throws IllegalArgumentException if any of the required playlists are not found
     */
    public static List<Playlist> sortPlaylists(List<Playlist> playlistList) {
        if (playlistList == null || playlistList.isEmpty()) {
            return playlistList;
        }

        Map<String, Playlist> specialPlaylists = playlistList.stream()
                .filter(p -> p.getName().equals(FAVORITES_PLAYLIST_NAME) ||
                        p.getName().equals(RECENTLY_PLAYED_PLAYLIST_NAME) ||
                        p.getName().equals(MOSTLY_PLAYED_PLAYLIST_NAME))
                .collect(Collectors.toMap(Playlist::getName, p -> p));

        if (specialPlaylists.size() != 3) {
            throw new IllegalArgumentException("One or more required playlists not found");
        }

        List<Playlist> result = playlistList.stream()
                .filter(p -> !specialPlaylists.containsKey(p.getName()))
                .collect(Collectors.toList());

        result.add(0, specialPlaylists.get(MOSTLY_PLAYED_PLAYLIST_NAME));
        result.add(0, specialPlaylists.get(RECENTLY_PLAYED_PLAYLIST_NAME));
        result.add(0, specialPlaylists.get(FAVORITES_PLAYLIST_NAME));

        return result;
    }
}
