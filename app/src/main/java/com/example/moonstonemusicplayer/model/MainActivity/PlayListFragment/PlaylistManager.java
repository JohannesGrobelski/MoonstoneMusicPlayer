package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import android.content.Context;

import com.example.moonstonemusicplayer.model.Database.DataSourceSingleton;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;

import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

  private static final String TAG = PlaylistManager.class.getSimpleName();
  private Context context;
  //private DataSource dataSource;

  private Playlist currentPlaylist;
  private List<Playlist> playlists;

  public PlaylistManager(Context baseContext) {
    this.context = baseContext;
    loadPlaylistsFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadPlaylistsFromDB(Context context){
    if(context != null){
      this.playlists.addAll(DataSourceSingleton.getInstance(context).getAllPlaylists());
    }

  }

  public Playlist getPlaylists(String name){
    for(Playlist playList: this.playlists){
      if(playList.name.equals(name))return playList;
    }
    return null;
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
}
