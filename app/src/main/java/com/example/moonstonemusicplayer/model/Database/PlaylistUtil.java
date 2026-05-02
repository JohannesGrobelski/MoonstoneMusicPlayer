/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.Database;

import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.FAVORITES;
import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.MOSTLY_PLAYED;
import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.RECENTLY_PLAYED;

import android.content.Context;
import android.view.animation.Transformation;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import androidx.lifecycle.Transformations;

import timber.log.Timber;

public class PlaylistUtil {
    
    public static LiveData<List<Playlist>> getAllPlaylists(Context context) {
        try {
            return Transformations.map(
                DBPlaylists.getInstance(context).getAllPlaylists(context),
                songList -> {
                    return sortPlaylists(songList);   
                }
            );
        } catch (Exception e){
            Timber.e( e.toString());
            return null;
        }
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
                .filter(p -> p.getName().equals(FAVORITES) ||
                        p.getName().equals(RECENTLY_PLAYED) ||
                        p.getName().equals(MOSTLY_PLAYED))
                .collect(Collectors.toMap(Playlist::getName, p -> p));

        List<Playlist> result = playlistList.stream()
                .filter(p -> !specialPlaylists.containsKey(p.getName()))
                .collect(Collectors.toList());

        if(specialPlaylists.containsKey(MOSTLY_PLAYED)){
            result.add(0, specialPlaylists.get(MOSTLY_PLAYED));
        }
        if(specialPlaylists.containsKey(RECENTLY_PLAYED)) {
            result.add(0, specialPlaylists.get(RECENTLY_PLAYED));
        }
        if(specialPlaylists.containsKey(FAVORITES)) {
            result.add(0, specialPlaylists.get(FAVORITES));
        }

        return result;
    }
}
