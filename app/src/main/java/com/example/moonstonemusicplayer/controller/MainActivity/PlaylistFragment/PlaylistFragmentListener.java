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
    if(DEBUG)Log.d(TAG,"\n\n\n\n\n");

    //set back text
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
      itemList.addAll(playListFragment.playlistListManager.getAllPlaylists());
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
    //only show context menu if clicked on song
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
          String playlistName = playListFragment.playlistListManager.getCurrentPlaylist().getName();
          DBPlaylists.getInstance(playListFragment.getContext()).deleteFromPlaylist(song,playlistName);
          return false;
        }
      });
    }
  }


}
