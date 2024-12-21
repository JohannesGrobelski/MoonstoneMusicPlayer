/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.Artist;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ArtistManager {

  private static final String TAG = ArtistManager.class.getSimpleName();

  private Context context;
  private Artist currentArtist = null;
  private Album currentAlbum = null;
  private List<Album> albumList = new ArrayList<>();
  private List<Artist> artistList = new ArrayList<>();
  private final List<Artist> artistList_backup = new ArrayList<>();

  public ArtistManager(Context baseContext) {
    loadArtistsFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadArtistsFromDB(Context context){
    Map<String, List<Song>> artistMap = BrowserManager.getArtistListMap();
    for(String artistName : artistMap.keySet()){
      Artist artist = new Artist(artistName, artistMap.get(artistName));
      artistList.add(artist);
    }
  }

  public List<Artist> getArtistList() {
    return artistList;
  }

  public void setArtistList(List<Artist> artistList) {
    this.artistList = artistList;
  }

  /** search for artists with name matching the query*/
  public Artist[] getAllArtistsMatchingQuery(String query) {
    List<Artist> results = new ArrayList<>();
    for(Artist Artist: getArtistList()){
      if(Artist.getName().toLowerCase().contains(query.toLowerCase())){
        results.add(Artist);
      }
    }
    return results.toArray(new Artist[results.size()]);
  }

  public List<Artist> getAllArtists() {
    Log.d("artistList","backup: "+artistList_backup.size());
    artistList.clear();
    artistList.addAll(artistList_backup);
    return artistList;
  }

  public Artist getCurrentArtist() {
    return currentArtist;
  }

  public void setCurrentArtist(Artist currentArtist) {
    this.currentArtist = currentArtist;
  }

  public Album getCurrentAlbum() {
    return currentAlbum;
  }

  public void setCurrentAlbum(Album currentAlbum) {
    this.currentAlbum = currentAlbum;
  }

  public void sortSongsByDuration() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (int) (o1.getDuration_ms() - o2.getDuration_ms());
        }
      });
    }

  }

  public void sortSongsByArtist() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getArtist().compareTo(o2.getArtist()));
        }
      });
    }

  }

  public void sortSongsByName() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getName().compareTo(o2.getName()));
        }
      });
    }

  }

  public void sortSongsByGenre() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getGenre().compareTo(o2.getGenre()));
        }
      });
    }

  }

  public void reverse(){
    if(currentAlbum != null){
      Collections.reverse(currentAlbum.getSongList());
    }
  }


  public List<Album> getAlbumList() {
    return albumList;
  }

}
