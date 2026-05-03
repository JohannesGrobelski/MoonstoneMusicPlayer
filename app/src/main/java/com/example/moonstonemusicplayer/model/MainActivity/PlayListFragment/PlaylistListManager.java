/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.FAVORITES;
import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.MOSTLY_PLAYED;
import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.RECENTLY_ADDED;
import static com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistDao.RECENTLY_PLAYED;
import com.example.moonstonemusicplayer.utils.LocaleUtil;
import android.content.Context;
import android.database.Cursor;
import android.graphics.text.LineBreaker.Result;
import android.os.Build;
import android.provider.MediaStore;
import androidx.lifecycle.LifecycleOwner;
import android.os.Handler;
import android.os.Looper;

//import com.example.moonstonemusicplayer.model.Database.DBSonglists;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.Database.PlaylistUtil;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import androidx.lifecycle.MediatorLiveData;

/** saves and loads playlists and contains the current (displayed) playlist in playlistfragment*/
public class PlaylistListManager {

  
  private final Context context;
  private final LifecycleOwner lifecycleOwner;
  //private DataSource dataSource;

  private Playlist currentPlaylist;
  private List<Playlist> playlists = new ArrayList<>();
  private final List<Playlist> playlists_backup = new ArrayList<>();

  private MediatorLiveData<List<Playlist>> mergedPlaylists = null;
  private Playlist recentlyPlayedPlaylist = null;
  private Playlist mostlyPlayedPlaylist = null;
  private Playlist favoritesPlaylist = null;
  private List<Playlist> userPlaylists = null;

  public PlaylistListManager(Context baseContext, LifecycleOwner lifecycleOwner, Runnable onPlaylistsLoaded) {
    this.context = baseContext;
    this.lifecycleOwner = lifecycleOwner;
    loadPlaylistsFromDB(baseContext, onPlaylistsLoaded);
  }

  /** loads local music and adds it to dataSource*/
  private void loadPlaylistsFromDB(Context context, Runnable onPlaylistsLoaded){
    playlists_backup.clear();
    playlists.clear();

    recentlyPlayedPlaylist = null;
    mostlyPlayedPlaylist = null;
    favoritesPlaylist = null;
    userPlaylists = null;
    mergedPlaylists = new MediatorLiveData<>();
   
    //NOTE: RECENTLY_PLAYED aufbauen
    mergedPlaylists.addSource(DBPlaylists.getInstance(context).getAllRecentlyPlayed(context), songListRecentlyPlayed -> {
        List<Song> temp = new LinkedList<>();
        for(int i=songListRecentlyPlayed.size()-1; i>=0; i--){
          Song song = songListRecentlyPlayed.get(i);
          if(song != null 
            && song.getName() != null && !song.getName().isEmpty()
            && song.getPath() != null && new File(song.getPath()).exists()){
            temp.add(songListRecentlyPlayed.get(i));
          }
        }
        recentlyPlayedPlaylist = new Playlist(RECENTLY_PLAYED, temp);
        mergePlaylists(mergedPlaylists, recentlyPlayedPlaylist, mostlyPlayedPlaylist, favoritesPlaylist, userPlaylists);
    });

    //NOTE: MOSTLY_PLAYED aufbauen
    mergedPlaylists.addSource(DBPlaylists.getInstance(context).getMostlyPlayed(), songListMostlyPlayed -> {
        mostlyPlayedPlaylist = new Playlist(MOSTLY_PLAYED, songListMostlyPlayed);
        mergePlaylists(mergedPlaylists, recentlyPlayedPlaylist, mostlyPlayedPlaylist, favoritesPlaylist, userPlaylists);
    });

    //NOTE: FAVORITES aufbauen
    mergedPlaylists.addSource(DBPlaylists.getInstance(context).getAllFavorites(context), songListFavorites -> {
        favoritesPlaylist = new Playlist(FAVORITES, songListFavorites);
        mergePlaylists(mergedPlaylists, recentlyPlayedPlaylist, mostlyPlayedPlaylist, favoritesPlaylist, userPlaylists);
    });

    //NOTE: Alle anderen Playlists aufbauen
    mergedPlaylists.addSource(DBPlaylists.getInstance(context).getAllPlaylists(context), allUserPlaylists -> {
        userPlaylists = allUserPlaylists;
        mergePlaylists(mergedPlaylists, recentlyPlayedPlaylist, mostlyPlayedPlaylist, favoritesPlaylist, userPlaylists);
    });

    //NOTE: Wenn wir alle Playlists gemerget haben -> in playlists_backup und playlists eintragen!
    mergedPlaylists.observe(lifecycleOwner, result -> {
        playlists_backup.clear();
        playlists.clear();
        playlists_backup.addAll(result);
        playlists.addAll(result);

        //NOTE: update Adapter etc.
        if(onPlaylistsLoaded != null){
            onPlaylistsLoaded.run();
        }
    });
  }

  private void mergePlaylists(MediatorLiveData<List<Playlist>> merged, Playlist recentlyPlayed, Playlist mostlyPlayed, Playlist favorites, List<Playlist> userPlaylists){
      if(recentlyPlayed == null || mostlyPlayed == null || userPlaylists == null || favorites == null){
        return;
      }
        
      List<Playlist> allPlaylists = new LinkedList<>();

      allPlaylists.add(recentlyPlayed);
      allPlaylists.add(mostlyPlayed);
      allPlaylists.add(createRecentlyAddedPlaylist(context));
      allPlaylists.add(favorites);
      allPlaylists.addAll(userPlaylists);
      
      merged.postValue(allPlaylists);
  }

  public Playlist setOnRecentlyAddedPlaylist(){
    for(Playlist playlist : this.playlists){
      if(playlist.getName().equals(RECENTLY_ADDED)){
        //this.currentPlaylist = playlist;
        return playlist;
      }
    }
    return null;
  }

  public Playlist getPlaylist(String name){
    for(Playlist playList: this.playlists){
      if(playList.name.equals(name))return playList;
    }
    return null;
  }

  public List<Playlist> getAllPlaylists(){
    playlists.clear();
    playlists.addAll(playlists_backup);
    return this.playlists;
  }

  public List<String> getPlaylistNames(){
    List<String> playlistNames = new ArrayList<>();
    for(Playlist playList: this.playlists){
      playlistNames.add(playList.name);
    }
    return playlistNames;
  }

  public Playlist getCurrentPlaylist() {
    return currentPlaylist;
  }

  public void setCurrentPlaylist(Playlist currentPlaylist) {
    this.currentPlaylist = currentPlaylist;
  }

  public List<Playlist> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(List<Playlist> playlists) {
    this.playlists = playlists;
  }

  public Playlist[] getAllPlaylistsMatchingQuery(String query) {
    List<Playlist> result = new ArrayList<>();
    for(Playlist playlist: getAllPlaylists()){
      if(playlist.getName().toLowerCase(LocaleUtil.getCurrentLocale(context)).contains(query.toLowerCase(LocaleUtil.getCurrentLocale(context)))){
        result.add(playlist);
      }
    }
    return result.toArray(new Playlist[result.size()]);
  }

  public Song[] getAllSongsMatchingQuery(String query) {
    List<Song> result = new ArrayList<>();
    for(Playlist playlist: getAllPlaylists()){
      for(Song song: playlist.getPlaylist()) {
        if(song.getName().toLowerCase(LocaleUtil.getCurrentLocale(context)).contains(query.toLowerCase(LocaleUtil.getCurrentLocale(context)))){
          result.add(song);
        }
      }
    }
    return result.toArray(new Song[result.size()]);
  }



  public void deletePlaylist(Playlist playlist) {
    playlists_backup.remove(playlist);
    playlists.remove(playlist);
  }

  public void deleteFromPlaylist(Song song, String name) {
    for(Playlist playlist: playlists_backup){
      if(playlist.name.equals(name)){
        playlist.getPlaylist().remove(song);
      }
    }
    for(Playlist playlist: playlists){
      if(playlist.name.equals(name)){
        playlist.getPlaylist().remove(song);
      }
    }
  }

  public void sortSongsByGenre() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getGenre().compareTo(o2.getGenre());
        }
      });
    }
  }

  public void sortSongsByDuration() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return (int)(o1.getDuration_ms() - o2.getDuration_ms());
        }
      });
    }
  }

  public void sortSongsByArtist() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getArtist().compareTo(o2.getArtist());
        }
      });
    }
  }

  public void sortSongsByName() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    Collections.sort(playlists, new Comparator<Playlist>() {
      @Override
      public int compare(Playlist o1, Playlist o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
  }

  public void reverse(){
    if(currentPlaylist != null){
      Collections.reverse(currentPlaylist.getPlaylist());
    }
  }

  public void updateRecentlyAddedPlaylist(Context context){
    createRecentlyAddedPlaylist(context);
  }

  private Playlist createRecentlyAddedPlaylist(Context context){
    List<Song> songList = new ArrayList<>();

    // Define the columns to retrieve from the MediaStore
    String[] projection = {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
    };

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // SDK 30 or above
      projection = new String[]{
              MediaStore.Audio.Media.DATA,
              MediaStore.Audio.Media.TITLE,
              MediaStore.Audio.Media.ARTIST,
              MediaStore.Audio.Media.ALBUM,
              MediaStore.Audio.Media.GENRE,
              MediaStore.Audio.Media.DURATION,
      };
    }


    // Set the sort order to get the most recently added songs first
    String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

    // Limit the query to retrieve only the last 30 songs
    int limit = 100;

    Cursor cursor = context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
    );

    // Process the cursor to get the song information
    if (cursor != null && cursor.moveToFirst()) {
      do {
        String genre = "";
        // Retrieve the column index for the data column
        int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // SDK 30 or above
          int genreIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE);
          genre = cursor.getString(genreIndex);
        }

        int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        String filePath = cursor.getString(dataIndex);
        String name = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);
        String album = cursor.getString(albumIndex);
        String durationString = cursor.getString(durationIndex);

        if(filePath == null || filePath.isEmpty()
                || name == null || name.isEmpty()
                || durationString == null || durationString.isEmpty())continue;

        int duration_ms = 0;
        duration_ms = Integer.parseInt(durationString);

        Song song = new Song(filePath,name,artist,album,genre,duration_ms,"");
        songList.add(song);
        --limit;
      } while (cursor.moveToNext() && limit > 0);

      cursor.close();
    }
    return new Playlist(RECENTLY_ADDED, songList);
  }
}
