package com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.DBHelperPlaylists;
import com.example.moonstonemusicplayer.model.Database.DBPlaylists;
import com.example.moonstonemusicplayer.model.Database.DBSonglists;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

  private static final String TAG = FavoritesManager.class.getSimpleName();
  private Context context;

  private List<Song> favorites = new ArrayList<>();
  private List<Song> favorites_backup = new ArrayList<>();

  public FavoritesManager(Context baseContext) {
    this.context = baseContext;
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
      if(song.getName().toLowerCase().contains(query.toLowerCase())){
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
}
