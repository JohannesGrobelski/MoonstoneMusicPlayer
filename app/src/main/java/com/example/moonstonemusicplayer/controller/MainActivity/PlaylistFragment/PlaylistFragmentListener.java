package com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.DBPlaylists;
import com.example.moonstonemusicplayer.model.Database.DBSonglists;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.PlaylistListManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.ui.main.PlayListFragment;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnCreateContextMenuListener {
  private static final String TAG = PlaylistFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = true;
  public static final String PLAYLISTINDEXEXTRA = "playlistextra";

  private PlayListFragment playListFragment;
  public PlaylistListAdapter playlistListAdapter;

  private static Playlist Playlist;

  public PlaylistFragmentListener(PlayListFragment playListFragment) {
    this.playListFragment = playListFragment;
    List<Object> playlists = new ArrayList<>();
    playlists.addAll(playListFragment.playlistListManager.getAllPlaylists());
    setAdapter(playlists);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Object clickItem = playlistListAdapter.getItem(position);
    if(clickItem != null) {
      if(clickItem instanceof Playlist) {
        List<Object> itemList = new ArrayList<>();;
        itemList.addAll(((Playlist) clickItem).getPlaylist());
        setAdapter(itemList);
        playListFragment.playlistListManager.setCurrentPlaylist((Playlist) clickItem);
      } else if(clickItem instanceof Song) {
        startPlaylist(playListFragment.playlistListManager.getCurrentPlaylist(),position);
      } else { }
    }
  }

  public void setAdapter(List<Object> itemList){
    playlistListAdapter = new PlaylistListAdapter(playListFragment.getContext(),itemList);
    playListFragment.lv_playlist.setAdapter(playlistListAdapter);
  }

  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_playlist){
      List<Object> itemList = new ArrayList<>();;
      itemList.addAll(playListFragment.playlistListManager.getPlaylists());
      setAdapter(itemList);
      playListFragment.playlistListManager.setCurrentPlaylist(null);
    }
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startPlaylist(Playlist playlist, int song_index){
    Playlist = new Playlist(playlist.getName(),playlist.getPlaylist());
    Intent intent = new Intent(playListFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(PLAYLISTINDEXEXTRA,song_index);
    playListFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static Song[] getPlaylistSonglist(){
    Song[] playlistCopy = Playlist.getPlaylist().toArray(new Song[Playlist.getPlaylist().size()]);
    Playlist = null;
    return playlistCopy;
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    //the menu created if a song is clicked on
    if(playListFragment.playlistListManager.getCurrentPlaylist() != null){
      //create menu item with groupid to distinguish between fragments
      menu.add(1, 11, 0, "delete from playlist");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Song song = playListFragment.playlistListManager.getCurrentPlaylist().getPlaylist().get(index);
          Playlist currentPlaylist = playListFragment.playlistListManager.getCurrentPlaylist();
          currentPlaylist.getPlaylist().remove(song);

          DBPlaylists.getInstance(playListFragment.getContext()).deleteFromPlaylist(song,
              playListFragment.playlistListManager.getCurrentPlaylist().getName());

          playListFragment.playlistListManager = new PlaylistListManager(playListFragment.getContext());
          playListFragment.playlistListManager.setCurrentPlaylist(currentPlaylist);

          List<Object> songs = new ArrayList<>();
          songs.addAll(currentPlaylist.getPlaylist());
          setAdapter(songs);

          return false;
        }
      });
    } else {//the menu created if a playlist is clicked on
      menu.add(1, 12, 0, "delete playlist");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Playlist playlist = playListFragment.playlistListManager.getPlaylists().get(index);

          DBPlaylists.getInstance(playListFragment.getContext()).deletePlaylist(playlist);

          playListFragment.playlistListManager = new PlaylistListManager(playListFragment.getContext());
          playListFragment.playlistListManager.setCurrentPlaylist(null);

          List<Object> songs = new ArrayList<>();
          songs.addAll(playListFragment.playlistListManager.getAllPlaylists());
          setAdapter(songs);

          return false;
        }
      });

    }
  }


  public void reloadPlaylistManager() {
    Log.d("PLaylistsfragment","onResume");
    if(playListFragment.playlistListManager != null){
      Playlist currentPlaylist = playListFragment.playlistListManager.getCurrentPlaylist();
      playListFragment.playlistListManager.loadPlaylistsFromDB(playListFragment.getContext());

      List<Object> currentItems = new ArrayList<>();

      if(currentPlaylist != null){
        currentPlaylist = playListFragment.playlistListManager.getPlaylist(currentPlaylist.getName());
        if(currentPlaylist != null){
          playListFragment.playlistListManager.setCurrentPlaylist(currentPlaylist);

          currentItems.addAll(currentPlaylist.getPlaylist());
          setAdapter(currentItems);
          playListFragment.playlistListManager.setCurrentPlaylist(currentPlaylist);
        }
      } else {
        currentItems.addAll(playListFragment.playlistListManager.getPlaylists());
        setAdapter(currentItems);
      }
    }
  }

  public boolean onBackpressed() {
    if(playListFragment.playlistListManager.getCurrentPlaylist() != null){
      List<Object> itemList = new ArrayList<>();;
      itemList.addAll(playListFragment.playlistListManager.getPlaylists());
      setAdapter(itemList);
      playListFragment.playlistListManager.setCurrentPlaylist(null);
      return true;
    }
    return false;
  }
}
