package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.PlaylistListManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A fragment to view and select the saved playlists.
 */
public class PlayListFragment extends Fragment {

  private static final String TAG = PlayListFragment.class.getSimpleName();
  private static PlaylistListManager playlistListManager;
  public PlaylistFragmentListener playlistFragmentListener;

  public SwipeRefreshLayout srl_playlist;
  private LinearLayout ll_playlistBack;
  public ListView lv_playlist;

  public static void preloadPlaylistManager(Context context){
    new Thread(() -> {
        playlistListManager = new PlaylistListManager(context);
    }).start();
  }

  public static PlayListFragment newInstance(int index) {
    PlayListFragment fragment = new PlayListFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_playlist, container, false);

    srl_playlist = root.findViewById(R.id.srl_playlist);

    lv_playlist = root.findViewById(R.id.lv_playlistSongList);
    ll_playlistBack = root.findViewById(R.id.ll_back_playlist);

    playlistFragmentListener = new PlaylistFragmentListener(this);

    lv_playlist.setOnItemClickListener(playlistFragmentListener);
    ll_playlistBack.setOnClickListener(playlistFragmentListener);

    registerForContextMenu(lv_playlist);
    lv_playlist.setOnCreateContextMenuListener(this);

    srl_playlist.setEnabled(false);
    srl_playlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        playlistFragmentListener.refreshRecentAddedPlaylist();
      }
    });

    return root;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if(playlistListManager == null){
        playlistListManager = new PlaylistListManager(this.getContext());
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    update(false);
  }

  public void update(boolean onSRLRefresh){
    playlistListManager.updateData(this.getContext());
    playlistFragmentListener.updateAdapter();
    Playlist recentlyAddedPlaylist = playlistListManager.setOnRecentlyAddedPlaylist();
    if(onSRLRefresh){
      playlistFragmentListener.updateAdapter(recentlyAddedPlaylist);
    }
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    playlistFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }

  public PlaylistListManager getPlaylistManager(){
    return playlistListManager;
  }

  public void reloadPlaylistManager(Context context){
      playlistListManager = new PlaylistListManager(context); 
  }

  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Playlist[] matchingPlaylists = playlistListManager.getAllPlaylistsMatchingQuery(query);

      playlistListManager.setPlaylists(new ArrayList<Playlist>(Arrays.asList(matchingPlaylists)));

      List<Object> searchResults = new ArrayList<>();
      searchResults.addAll(playlistListManager.getPlaylists());

      playlistFragmentListener.setAdapter(searchResults);
    } else {
      List<Object> allPlaylists = new ArrayList<>();
      allPlaylists.addAll(playlistListManager.getAllPlaylists());
      playlistFragmentListener.setAdapter(allPlaylists);
    }
  }

  public void sortSongsByName() {
    playlistListManager.sortSongsByName();
    if(playlistListManager.getCurrentPlaylist() != null){
      List<Object> songs = new ArrayList<>();
      songs.addAll(playlistListManager.getCurrentPlaylist().getPlaylist());
      playlistFragmentListener.setAdapter(songs);
    }
  }

  public void sortSongsByArtist() {
    playlistListManager.sortSongsByArtist();
    if(playlistListManager.getCurrentPlaylist() != null){
      List<Object> songs = new ArrayList<>();
      songs.addAll(playlistListManager.getCurrentPlaylist().getPlaylist());
      playlistFragmentListener.setAdapter(songs);
    }
  }

  public void sortSongsByDuration() {
    playlistListManager.sortSongsByDuration();
    if(playlistListManager.getCurrentPlaylist() != null){
      List<Object> songs = new ArrayList<>();
      songs.addAll(playlistListManager.getCurrentPlaylist().getPlaylist());
      playlistFragmentListener.setAdapter(songs);
    }
  }

  public void sortSongsByGenre() {
    playlistListManager.sortSongsByGenre();
    if(playlistListManager.getCurrentPlaylist() != null){
      List<Object> songs = new ArrayList<>();
      songs.addAll(playlistListManager.getCurrentPlaylist().getPlaylist());
      playlistFragmentListener.setAdapter(songs);
    }
  }

  public boolean onBackpressed() {
    return playlistFragmentListener.onBackpressed();
  }

  public void reverse() {
    playlistListManager.reverse();
    if(playlistListManager.getCurrentPlaylist() != null){
      List<Object> songs = new ArrayList<>();
      songs.addAll(playlistListManager.getCurrentPlaylist().getPlaylist());
      playlistFragmentListener.setAdapter(songs);
    }
  }
}
