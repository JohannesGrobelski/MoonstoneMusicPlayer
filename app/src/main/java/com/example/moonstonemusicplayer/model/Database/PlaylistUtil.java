package com.example.moonstonemusicplayer.model.Database;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Playcountlist.DBPlaycountList;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PlaylistUtil {
    private static final String TAG = DBPlaylists.class.getSimpleName();
    private static final boolean DEBUG = true;


    public static List<Playlist> getAllPlaylists(Context context) {
        try {
            List<Playlist> playlists = new LinkedList<>(DBPlaylists.getInstance(context).getAllPlaylists(context));
            playlists.add(getPlaylistMostlyPlayed(context));
            return playlists;
        } catch (Exception e){
            Log.e(TAG, e.toString());
            return List.of();
        }
    }

    private static Playlist getPlaylistMostlyPlayed(Context context){
        return new Playlist("MOSTLY PLAYED", DBPlaycountList.getInstance(context).getMostlyPlayed());
    }
}
