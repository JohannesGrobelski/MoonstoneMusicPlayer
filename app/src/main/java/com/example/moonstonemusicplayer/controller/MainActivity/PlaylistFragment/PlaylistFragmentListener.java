/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;
import com.woxthebox.draglistview.DragListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistFragmentListener implements View.OnClickListener, View.OnCreateContextMenuListener, BrowserManager.AfterFileDeletion {
  private static final String TAG = PlaylistFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = true;
  public static final String PLAYLISTINDEXEXTRA = "playlistIndexExtra";
  public static final String PLAYLISTNAMEEXTRA = "playlistNameExtra";

  private final PlayListFragment playListFragment;
  public PlaylistListAdapter playlistListAdapter;

  private String currentPlaylist = "";

  private static Playlist Playlist;

  public PlaylistFragmentListener(PlayListFragment playListFragment) {
    this.playListFragment = playListFragment;
    List<Object> playlists = new ArrayList<>();
    playlists.addAll(playListFragment.getPlaylistManager().getAllPlaylists());
    setAdapter(playlists);
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
    playlistListAdapter = new PlaylistListAdapter(playListFragment, itemList);

      // Set up click listener
      playlistListAdapter.setItemClickListener((clickItem, position) -> {
        playListFragment.srl_playlist.setEnabled(false);
        if (clickItem != null) {
          if (clickItem instanceof Playlist) {
            List<Object> newItemList = new ArrayList<>();
            newItemList.addAll(((Playlist) clickItem).getPlaylist());
            BrowserManager.grabThumbnails(((Playlist) clickItem).getPlaylist().stream().map(s -> new File(s.getPath())).collect(Collectors.toList()));
            setAdapter(newItemList);
            playListFragment.getPlaylistManager().setCurrentPlaylist((Playlist) clickItem);
            playListFragment.srl_playlist.setEnabled(
                    playListFragment.getPlaylistManager().getCurrentPlaylist().getName().equals(RECENTLY_ADDED_PLAYLIST_NAME)
            );
          } else if (clickItem instanceof Song) {
            startPlaylist(playListFragment.getPlaylistManager().getCurrentPlaylist(), position);
          }
        }
      });

      // Set up drag listener if needed
      playListFragment.dlv_playlistSongList.setDragListListener(new DragListView.DragListListener() {
        @Override
        public void onItemDragStarted(int position) {
          // Handle drag start#
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {
          // Handle item dragging
        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition) {
          // Handle drag end - update your data model here if needed
          if (!currentPlaylist.isEmpty() && fromPosition != toPosition) {
            Playlist playlist = new Playlist(currentPlaylist, playlistListAdapter.getItemList().stream().map(o -> (Song) o).collect(Collectors.toList()));

            //NOTE: drag list view does already manipulate the data list (do not change playlistListAdapter.getItemList())!!!
            DBPlaylists dbPlaylists = DBPlaylists.getInstance(playListFragment.getContext());
            dbPlaylists.changePlaylistOrder(currentPlaylist, playlistListAdapter.getItemList().stream().map(o -> (Song) o).collect(Collectors.toList()));
            playListFragment.reloadPlaylistManager(playListFragment.getContext());
            playListFragment.getPlaylistManager().setCurrentPlaylist(playlist);
          }
        }
      });
      playListFragment.dlv_playlistSongList.setLayoutManager(new LinearLayoutManager(playListFragment.getActivity()));

      playListFragment.dlv_playlistSongList.setAdapter(playlistListAdapter, true);
      playListFragment.dlv_playlistSongList.setCanDragHorizontally(false);
    });
  }


  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_playlist) {
      List<Object> itemList = new ArrayList<>();
      itemList.addAll(playListFragment.getPlaylistManager().getPlaylists());
      setCurrentPlaylist("");
      setAdapter(itemList);
      playListFragment.getPlaylistManager().setCurrentPlaylist(null);
      playListFragment.dlv_playlistSongList.setDragEnabled(false);
    }
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startPlaylist(Playlist playlist, int song_index){
    Playlist = new Playlist(playlist.getName(),playlist.getPlaylist());
    Intent intent = new Intent(playListFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(PLAYLISTINDEXEXTRA,song_index);
    intent.putExtra(PLAYLISTNAMEEXTRA,playlist.getName());
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
    int position = playlistListAdapter.getLastLongClickedPosition();
    //the menu created if a song is clicked on
    if(playlistListAdapter.getItemList().get(position) instanceof Song){
      //create menu item with groupid to distinguish between fragments
      boolean isCalculatedPlaylist = currentPlaylist.equals(RECENTLY_ADDED_PLAYLIST_NAME) 
          || currentPlaylist.equals(RECENTLY_PLAYED_PLAYLIST_NAME)
          || currentPlaylist.equals(MOSTLY_PLAYED_PLAYLIST_NAME);
      if(!isCalculatedPlaylist){
        menu.add(0, 0, 0, "aus Playlist löschen");
      }
      menu.add(0, 1, 0, "zu Playlist hinzufügen");
      menu.add(0, 2, 0, "Song löschen");
      if(!currentPlaylist.equals(RECENTLY_ADDED_PLAYLIST_NAME)){
          menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            /** onContextItemSelected(MenuItem item) doesnt work*/
            public boolean onMenuItemClick(MenuItem item) {
              Song song = playListFragment.getPlaylistManager().getCurrentPlaylist().getPlaylist().get(position);
              deleteSongFromPlaylist(song);
              return false;
            }
          });
      }
      menu.getItem(!isCalculatedPlaylist ? 1 : 0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          Song song = playListFragment.getPlaylistManager().getCurrentPlaylist().getPlaylist().get(position);
          showAlertDialogAddToPlaylists(playListFragment.getLayoutInflater(), playListFragment.getContext(), song);
          return false;
        }
      });
      menu.getItem(!isCalculatedPlaylist ? 2 : 1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        /** onContextItemSelected(MenuItem item) doesnt work*/
        public boolean onMenuItemClick(MenuItem item) {
          Song song = playListFragment.getPlaylistManager().getCurrentPlaylist().getPlaylist().get(position);
          deleteSongFromPlaylist(song);
          BrowserManager.deleteFile(playListFragment.playlistFragmentListener, playListFragment.getContext(), ((MainActivity) playListFragment.getActivity()).getDeletetionIntentSenderLauncher(), song.getPath());
          return false;
        }
      });

    } else {//the menu created if a playlist is clicked on
      if(!((Playlist) playlistListAdapter.getItemList().get(position)).getName().equals(RECENTLY_ADDED_PLAYLIST_NAME)
      && !((Playlist) playlistListAdapter.getItemList().get(position)).getName().equals(RECENTLY_PLAYED_PLAYLIST_NAME)
      && !((Playlist) playlistListAdapter.getItemList().get(position)).getName().equals(MOSTLY_PLAYED_PLAYLIST_NAME)){
        menu.add(0, 0, 0, "playlist löschen");
        menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
          @Override
          /** onContextItemSelected(MenuItem item) doesnt work*/
          public boolean onMenuItemClick(MenuItem item) {
            try {
                 AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                // Check if menu info is null
                if (info == null) {
                    // Handle the case where info is null
                    Toast.makeText(playListFragment.getActivity(),"Could not delete playlist.", Toast.LENGTH_LONG).show();
                    Log.e("onContextItemSelected", "MenuInfo is null for item: " + item.getTitle());
                    return false; 
                }        

                int index = info.position;
                Playlist playlist = playListFragment.getPlaylistManager().getPlaylists().get(index);

                DBPlaylists.getInstance(playListFragment.getContext()).deletePlaylist(playlist);


                playListFragment.reloadPlaylistManager(playListFragment.getContext());
                playListFragment.getPlaylistManager().setCurrentPlaylist(null);

                List<Object> songs = new ArrayList<>();
                songs.addAll(playListFragment.getPlaylistManager().getAllPlaylists());
                setAdapter(songs);

                return false;
            } catch (Exception e) {
                Toast.makeText(playListFragment.getActivity(),"Could not delete playlist.", Toast.LENGTH_LONG).show();
                Log.e("onContextItemSelected", "MenuInfo is null for item: " + item.getTitle());
                return false;
            }
          }
        });
      }
      }

  }

  private void deleteSongFromPlaylist(Song song) {
    Playlist currentPlaylist = playListFragment.getPlaylistManager().getCurrentPlaylist();
    currentPlaylist.getPlaylist().remove(song);

    DBPlaylists.getInstance(playListFragment.getContext()).deleteFromPlaylist(song,
            playListFragment.getPlaylistManager().getCurrentPlaylist().getName());

    playListFragment.reloadPlaylistManager(playListFragment.getContext());
    playListFragment.getPlaylistManager().setCurrentPlaylist(currentPlaylist);

    List<Object> songs = new ArrayList<>();
    songs.addAll(currentPlaylist.getPlaylist());
    setAdapter(songs);
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

  public String getCurrentPlaylist() {
    return currentPlaylist;
  }

  public void setCurrentPlaylist(String currentPlaylist) {
    this.currentPlaylist = currentPlaylist;
  }

  public boolean onBackpressed() {
    playListFragment.srl_playlist.setEnabled(false);
    if(playListFragment.getPlaylistManager().getCurrentPlaylist() != null){
      playListFragment.srl_playlist.setEnabled(playListFragment.getPlaylistManager().getCurrentPlaylist().getName().equals(RECENTLY_ADDED_PLAYLIST_NAME));
      List<Object> itemList = new ArrayList<>();
      itemList.addAll(playListFragment.getPlaylistManager().getPlaylists());
      setAdapter(itemList);
      playListFragment.getPlaylistManager().setCurrentPlaylist(null);
      playListFragment.dlv_playlistSongList.setDragEnabled(false);
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


  private void showAlertDialogAddToPlaylists(LayoutInflater inflater, Context context, final Song song){
    final String[] allPlaylistNames = DBPlaylists.getInstance(context).getAllPlaylistNames();

    View dialogView = inflater.inflate(R.layout.add_to_playlist_layout, null);
    ListView lv_playlist_alert = dialogView.findViewById(R.id.lv_playlists_alert);
    final EditText et_addNewPlaylist = dialogView.findViewById(R.id.et_addNewPlaylist);

    lv_playlist_alert.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,allPlaylistNames));

    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
    dialogBuilder.setView(dialogView);
    dialogBuilder.setNegativeButton(android.R.string.no,null);
    dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String text = et_addNewPlaylist.getText().toString();
        if(!text.isEmpty()){
          DBPlaylists.getInstance(context).addToPlaylist(context,song,text);
        }
      }
    });
    dialogBuilder.setTitle("Füge den Song einer Playlist hinzu \noder erstelle eine neue.");

    final AlertDialog alertDialog  = dialogBuilder.show();

    lv_playlist_alert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBPlaylists.getInstance(context).addToPlaylist(context,song,allPlaylistNames[position]);
        alertDialog.dismiss();
      }
    });
  }

  @Override
  public void refreshAfterSongDeletion() {
    // Simulate refreshing (e.g., fetch new data)
    new Thread(() -> {
      //implement refresh
      if(currentPlaylist != null) {

      }

      if (playListFragment.getActivity() != null) {
        playListFragment.getActivity().runOnUiThread(() -> {
          // Stop the refreshing animation
          playListFragment.srl_playlist.setRefreshing(false);
        });
      }
    }).start();
  }
}
