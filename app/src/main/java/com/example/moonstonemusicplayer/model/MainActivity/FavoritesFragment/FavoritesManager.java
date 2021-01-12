package com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment;

import android.content.Context;

import com.example.moonstonemusicplayer.model.Database.DataSourceSingleton;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

  private static final String TAG = FavoritesManager.class.getSimpleName();
  private Context context;

  private List<Song> favorites;

  public FavoritesManager(Context baseContext) {
    this.context = baseContext;
    loadFavoritesFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadFavoritesFromDB(Context context){
    if(context != null){
      this.favorites.addAll(DataSourceSingleton.getInstance(context).getAllFavorites());
    }

  }

  public List<Song> getFavorites(){
   return this.favorites;
  }
}
