package com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
//import com.example.moonstonemusicplayer.model.Database.DBSonglists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnCreateContextMenuListener {
  private static final String TAG = PlaylistFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = true;
  public static final String PLAYLISTINDEXEXTRA = "playlistextra";

  private final PlayListFragment playListFragment;
  public PlaylistListAdapter playlistListAdapter;

  private static Playlist Playlist;

  public PlaylistFragmentListener(PlayListFragment playListFragment) {
    this.playListFragment = playListFragment;
    List<Object> playlists = new ArrayList<>();
    playlists.addAll(playListFragment.getPlaylistManager().getAllPlaylists());
    setAdapter(playlists);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Object clickItem = playlistListAdapter.getItem(position);
    playListFragment.srl_playlist.setEnabled(false);
    if(clickItem != null) {
      if(clickItem instanceof Playlist) {
        List<Object> itemList = new ArrayList<>();
        itemList.addAll(((Playlist) clickItem).getPlaylist());
        setAdapter(itemList);
        playListFragment.getPlaylistManager().setCurrentPlaylist((Playlist) clickItem);
        playListFragment.srl_playlist.setEnabled(playListFragment.getPlaylistManager().getCurrentPlaylist().getName().equals(RECENTLY_ADDED_PLAYLIST_NAME));
      } else if(clickItem instanceof Song) {
        startPlaylist(playListFragment.getPlaylistManager().getCurrentPlaylist(),position);
      } else { }
    }
  }

  public void updateAdapter(Playlist playlist){
    List<Object> playlists = new ArrayList<>();
    playlists.addAll(playlist.getPlaylist());
    setAdapter(playlists);
  }

  public void updateAdapter(){
    List<Object> playlists = new ArrayList<>();
    playlists.addAll(playListFragment.getPlaylistManager().getAllPlaylists());
    setAdapter(playlists);
  }

  public void setAdapter(List<Object> itemList) {
    // Ensure this runs on the main thread
    playListFragment.getActivity().runOnUiThread(() -> {
      playlistListAdapter = new PlaylistListAdapter(playListFragment.getContext(), itemList);
      playListFragment.lv_playlist.setAdapter(playlistListAdapter);
    });
  }


  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_playlist) {
      List<Object> itemList = new ArrayList<>();
      itemList.addAll(playListFragment.getPlaylistManager().getPlaylists());
      setAdapter(itemList);
      playListFragment.getPlaylistManager().setCurrentPlaylist(null);
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
  public static File[] getPlaylistSonglist(){
    List<File> fileList = new ArrayList<>();
    for(Song song : Playlist.getPlaylist()){
      fileList.add(new File(song.getPath()));
    }
    return fileList.toArray(new File[0]);
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    //the menu created if a song is clicked on
    if(playListFragment.getPlaylistManager().getCurrentPlaylist() != null){
      //create menu item with groupid to distinguish between fragments
      //präsentation
      menu.add(1, 11, 0, "aus Playlist löschen");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Song song = playListFragment.getPlaylistManager().getCurrentPlaylist().getPlaylist().get(index);
          Playlist currentPlaylist = playListFragment.getPlaylistManager().getCurrentPlaylist();
          currentPlaylist.getPlaylist().remove(song);

          DBPlaylists.getInstance(playListFragment.getContext()).deleteFromPlaylist(song,
              playListFragment.getPlaylistManager().getCurrentPlaylist().getName());

          playListFragment.reloadPlaylistManager(playListFragment.getContext()); 
          playListFragment.getPlaylistManager().setCurrentPlaylist(currentPlaylist);

          List<Object> songs = new ArrayList<>();
          songs.addAll(currentPlaylist.getPlaylist());
          setAdapter(songs);

          return false;
        }
      });
    } else {//the menu created if a playlist is clicked on
      menu.add(1, 12, 0, "playlist löschen");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Playlist playlist = playListFragment.getPlaylistManager().getPlaylists().get(index);

          DBPlaylists.getInstance(playListFragment.getContext()).deletePlaylist(playlist);


          playListFragment.reloadPlaylistManager(playListFragment.getContext()); 
          playListFragment.getPlaylistManager().setCurrentPlaylist(null);

          List<Object> songs = new ArrayList<>();
          songs.addAll(playListFragment.getPlaylistManager().getAllPlaylists());
          setAdapter(songs);

          return false;
        }
      });

    }
  }


  public void reloadPlaylistManager() {
    Log.d("PLaylistsfragment","onResume");
    if(playListFragment.getPlaylistManager() != null){
      Playlist currentPlaylist = playListFragment.getPlaylistManager().getCurrentPlaylist();
      playListFragment.getPlaylistManager().loadPlaylistsFromDB(playListFragment.getContext());

      List<Object> currentItems = new ArrayList<>();

      if(currentPlaylist != null){
        currentPlaylist = playListFragment.getPlaylistManager().getPlaylist(currentPlaylist.getName());
        if(currentPlaylist != null){
          playListFragment.getPlaylistManager().setCurrentPlaylist(currentPlaylist);

          currentItems.addAll(currentPlaylist.getPlaylist());
          setAdapter(currentItems);
          playListFragment.getPlaylistManager().setCurrentPlaylist(currentPlaylist);
        }
      } else {
        currentItems.addAll(playListFragment.getPlaylistManager().getPlaylists());
        setAdapter(currentItems);
      }
    }
  }

  public boolean onBackpressed() {
    playListFragment.srl_playlist.setEnabled(false);
    if(playListFragment.getPlaylistManager().getCurrentPlaylist() != null){
      playListFragment.srl_playlist.setEnabled(playListFragment.getPlaylistManager().getCurrentPlaylist().getName().equals(RECENTLY_ADDED_PLAYLIST_NAME));
      List<Object> itemList = new ArrayList<>();
        itemList.addAll(playListFragment.getPlaylistManager().getPlaylists());
      setAdapter(itemList);
      playListFragment.getPlaylistManager().setCurrentPlaylist(null);
      return true;
    }
    return false;
  }

  public void refreshRecentAddedPlaylist(){
    // Simulate refreshing (e.g., fetch new data)
    new Thread(() -> {
      //implement refresh
      playListFragment.update(true);

      if (playListFragment.getActivity() != null) {
        playListFragment.getActivity().runOnUiThread(() -> {
          // Stop the refreshing animation
          playListFragment.srl_playlist.setRefreshing(false);
        });
      }
    }).start();
  }
}
