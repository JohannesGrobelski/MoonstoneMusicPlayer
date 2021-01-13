package com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistListAdapter;
import com.example.moonstonemusicplayer.model.Database.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.ui.main.FavoritesFragment;
import com.example.moonstonemusicplayer.view.ui.main.PlayListFragment;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragmentListener implements AdapterView.OnItemClickListener {
  private static final String TAG = FavoriteFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  public static final String FAVORITELISTEXTRA = "favoritelistextra";

  private FavoritesFragment favoritesFragment;
  private FavoriteListAdapter favoriteListAdapter;

  private static List<Song> FavoriteList;

  public FavoriteFragmentListener(FavoritesFragment favoritesFragment) {
    this.favoritesFragment = favoritesFragment;
    List<Song> favoriteList = favoritesFragment.favoritesManager.getFavorites();
    setAdapter(favoriteList);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    //set back text
    Object clickItem = favoriteListAdapter.getItem(position);
    if(clickItem != null) {
      if(clickItem instanceof Song) {
        startPlaylist(favoritesFragment.favoritesManager.getFavorites(),position);
      } else { Log.e(TAG,"favorite list contains something different than songs");}
    }
  }

  private void setAdapter(List<Song> itemList){
    favoriteListAdapter = new FavoriteListAdapter(favoritesFragment.getContext(),itemList);
    favoritesFragment.lv_favorites.setAdapter(favoriteListAdapter);
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startPlaylist(List<Song> favoriteList, int song_index){
    FavoriteList = new ArrayList<>(favoriteList);
    Intent intent = new Intent(favoritesFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(FAVORITELISTEXTRA,song_index);
    favoritesFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static Song[] getFavoriteSonglist(){
    Song[] songlistCopy = FavoriteList.toArray(new Song[FavoriteList.size()]);
    FavoriteList = null;
    return songlistCopy;
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    //only show context menu if clicked on song
    if(favoritesFragment.favoritesManager.getFavorites() != null){
      //create menu item with groupid to distinguish between fragments
      menu.add(2, 21, 0, "delete from playlist");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Song song = favoritesFragment.favoritesManager.getFavorites().get(index);
          DBPlaylists.getInstance(favoritesFragment.getContext()).deleteFromFavorites(song);
          return false;
        }
      });
    }
  }

  public void onResume() {
    favoritesFragment.favoritesManager.loadFavoritesFromDB(favoritesFragment.getContext());
    List<Song> favoriteList = favoritesFragment.favoritesManager.getFavorites();
    setAdapter(favoriteList);
  }
}
