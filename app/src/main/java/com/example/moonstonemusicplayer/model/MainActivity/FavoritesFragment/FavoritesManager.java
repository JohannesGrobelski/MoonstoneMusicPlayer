package com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavoritesManager {

  private static final String TAG = FavoritesManager.class.getSimpleName();

  private List<Song> favorites = new ArrayList<>();
  private List<Song> favorites_backup = new ArrayList<>();

  public FavoritesManager(Context baseContext) {
    loadFavoritesFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadFavoritesFromDB(Context context){
    if(context != null){
      this.favorites.clear();
      this.favorites_backup.clear();
      this.favorites.addAll(DBPlaylists.getInstance(context).getAllFavorites());
      this.favorites_backup.addAll(favorites);
    }
  }

  public List<Song> getFavorites(){
   return this.favorites;
  }

  public void setFavorites(List<Song> favorites) {
    this.favorites = favorites;
  }

  public Song[] getAllSongsMatchingQuery(String query) {
    List<Song> results = new ArrayList<>();
    for(Song song: getFavorites()){
      if(song.getName().toLowerCase().contains(query.toLowerCase())
      || song.getArtist().toLowerCase().contains(query.toLowerCase())
      || song.getGenre().toLowerCase().contains(query.toLowerCase())){
            results.add(song);
      }
    }
    return results.toArray(new Song[results.size()]);
  }

  public List<Song> getAllFavorites() {
    Log.d("favorites","backup: "+favorites_backup.size());
    favorites.clear();
    favorites.addAll(favorites_backup);
    return favorites;
  }

  public void sortSongsByDuration() {
    Collections.sort(favorites, new Comparator<Song>() {
      @Override
      public int compare(Song o1, Song o2) {
        return (int) (o1.getDuration_ms() - o2.getDuration_ms());
      }
    });
  }

  public void sortSongsByArtist() {
    Collections.sort(favorites, new Comparator<Song>() {
      @Override
      public int compare(Song o1, Song o2) {
        return (o1.getArtist().compareTo(o2.getArtist()));
      }
    });
  }

  public void sortFavoritesByName() {
    Collections.sort(favorites, new Comparator<Song>() {
      @Override
      public int compare(Song o1, Song o2) {
        return (o1.getName().compareTo(o2.getName()));
      }
    });
  }

  public void sortSongsByGenre() {
    Collections.sort(favorites, new Comparator<Song>() {
      @Override
      public int compare(Song o1, Song o2) {
        return (o1.getGenre().compareTo(o2.getGenre()));
      }
    });
  }

  public void reverse(){
    Collections.reverse(favorites);
  }
}
