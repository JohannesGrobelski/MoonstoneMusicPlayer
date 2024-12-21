/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.Database;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Playcountlist.DBPlaycountList;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;

import java.util.LinkedList;
import java.util.List;

public class PlaylistUtil {
    private static final String TAG = DBPlaylists.class.getSimpleName();
    private static final boolean DEBUG = true;

    public static List<Playlist> getAllPlaylists(Context context) {
        try {
            List<Playlist> playlists = new LinkedList<>(DBPlaylists.getInstance(context).getAllPlaylists(context));
            return playlists;
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return List.of();
        }
    }

    public static Playlist getPlaylistMostlyPlayed(Context context){
        return new Playlist(DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME, DBPlaycountList.getInstance(context).getMostlyPlayed());
    }
}
