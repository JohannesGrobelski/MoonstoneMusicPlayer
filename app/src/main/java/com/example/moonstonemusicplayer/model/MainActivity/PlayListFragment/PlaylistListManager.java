/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore;

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

/** saves and loads playlists and contains the current (displayed) playlist in playlistfragment*/
public class PlaylistListManager {

  private static final String TAG = PlaylistListManager.class.getSimpleName();
  private final Context context;
  //private DataSource dataSource;

  private Playlist currentPlaylist;
  private List<Playlist> playlists = new ArrayList<>();
  private final List<Playlist> playlists_backup = new ArrayList<>();

  public PlaylistListManager(Context baseContext) {
    this.context = baseContext;
    loadPlaylistsFromDB(baseContext);
  }

  /** Update the playlists.
   *
   * @param context
   */
  public void updateData(Context context){
    playlists_backup.clear();
    playlists.clear();
    loadPlaylistsFromDB(context);
    playlists.addAll(playlists_backup);
  }

  /** loads local music and adds it to dataSource*/
  public void loadPlaylistsFromDB(Context context){
    playlists_backup.clear();
    playlists.clear();
    List<Playlist> allPlayLists = PlaylistUtil.getAllPlaylists(context);
    int indexRecentlyPlayed = -1;
    for(int i=0; i<allPlayLists.size(); i++){
      if(allPlayLists.get(i).name.equals(RECENTLY_PLAYED_PLAYLIST_NAME)){
        indexRecentlyPlayed = i;
        //Collections.reverse(playlist.getPlaylist());
      }
    }
    if(indexRecentlyPlayed != -1){
        //move RECENTLY_PLAYED
        Playlist recentlyPlayed = allPlayLists.remove(indexRecentlyPlayed);
        allPlayLists.add(0, recentlyPlayed); 
    } else {
      createRecentlyPlayedPlaylist(context);
    }
    createRecentlyAddedPlaylist(context);
    playlists_backup.add(PlaylistUtil.getPlaylistMostlyPlayed(context));

    this.playlists_backup.addAll(allPlayLists);
    playlists.addAll(playlists_backup);
  }

  public Playlist setOnRecentlyAddedPlaylist(){
    for(Playlist playlist : this.playlists){
      if(playlist.getName().equals(RECENTLY_ADDED_PLAYLIST_NAME)){
        this.currentPlaylist = playlist;
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
      if(playlist.getName().toLowerCase().contains(query.toLowerCase())){
        result.add(playlist);
      }
    }
    return result.toArray(new Playlist[result.size()]);
  }

  public Song[] getAllSongsMatchingQuery(String query) {
    List<Song> result = new ArrayList<>();
    for(Playlist playlist: getAllPlaylists()){
      for(Song song: playlist.getPlaylist()) {
        if(song.getName().toLowerCase().contains(query.toLowerCase())){
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

  private void createRecentlyAddedPlaylist(Context context){
    Playlist recentlyAddedPlaylist = new Playlist(RECENTLY_ADDED_PLAYLIST_NAME, new ArrayList<>());

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

        File songFile = new File(filePath);
        Song song = new Song(filePath,name,artist,album,genre,duration_ms,"");
        songList.add(song);
        --limit;
      } while (cursor.moveToNext() && limit > 0);

      cursor.close();
    }
    recentlyAddedPlaylist.playlist.addAll(songList);
    this.playlists_backup.add(recentlyAddedPlaylist);
  }

  private void createRecentlyPlayedPlaylist(Context context){
    Playlist recentlyPlayedPlaylist = new Playlist(RECENTLY_PLAYED_PLAYLIST_NAME, new ArrayList<>());
    List<Song> songListRecentlyPlayed = DBPlaylists.getInstance(context).getAllRecentlyPlayed(context);
    for(int i=songListRecentlyPlayed.size()-1; i>=0; i--){
      recentlyPlayedPlaylist.playlist.add(songListRecentlyPlayed.get(i));
    }
    this.playlists_backup.add(0, recentlyPlayedPlaylist);
  }
}
