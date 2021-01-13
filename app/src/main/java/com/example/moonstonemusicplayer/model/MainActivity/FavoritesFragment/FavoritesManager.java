package com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment;

import android.content.Context;

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

  public FavoritesManager(Context baseContext) {
    this.context = baseContext;
    loadFavoritesFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadFavoritesFromDB(Context context){
    if(context != null){
      this.favorites.clear();
      this.favorites.addAll(DBPlaylists.getInstance(context).getAllFavorites());
    }
  }

  public List<Song> getFavorites(){
   return this.favorites;
  }


}
