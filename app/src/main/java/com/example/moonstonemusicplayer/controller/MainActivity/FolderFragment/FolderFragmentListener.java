/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import static com.example.moonstonemusicplayer.controller.MainActivity.SharedUtility.showAlertDialogAddToPlaylists;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

import com.example.moonstonemusicplayer.model.NextSongToPlayUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener, BrowserManager.AfterFileDeletion {
  private static final String TAG = FolderFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  private static File[] FolderSonglist;
  private final FolderFragment folderFragment;
  private FolderListAdapter folderListAdapter;

  private File selectedFolder;

  private List<File> displayedItems = new ArrayList<>();

  String searchQuery = "";


  /** Sets reference to folder fragment and init selectedFolder to rootFolder
   *  i.e. the root folder will be displayed with the adapter
   *
   * @param folderFragment
   */
  public FolderFragmentListener(FolderFragment folderFragment) {
    this.folderFragment = folderFragment;
    selectedFolder = BrowserManager.getInstance(folderFragment.getContext()).getRootFolder();
    if(selectedFolder!=null){
      setAdapter(selectedFolder);
    }
  }

  /** If folder is selected go into the folder, if song is selected start PlaylistActivity.
   *
   * @param parent AdapterView that triggered the item click (i.e. the listView)
   * @param view The view (item) in the list that was clicked.
   * @param position The position (int) of the view in the ListView.
   * @param id the rowId of the item (View) in the list.
   */
  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    try {
      //get get item clicked
      File itemClicked = displayedItems.get(position);

      if(this.searchQuery.isEmpty()){
        if(itemClicked.isDirectory()){ //selected Folder
          //setAdapter
          this.selectedFolder = itemClicked;
          setAdapter(this.selectedFolder);
        } else { //selected Song

          File[] playlist = BrowserManager.getChildFiles(this.selectedFolder, BrowserManager.Filter.SONGS);
          int songPosition = position - BrowserManager.getDirectories(this.selectedFolder, BrowserManager.Filter.SONGS).length;
          startFolderSonglist(playlist, songPosition, folderFragment);
        }
      } else {
        startFolderSonglist(displayedItems.toArray(new File[0]), position, folderFragment);
      }

    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(folderFragment.getContext(), "ERROR: Could not click on item.", Toast.LENGTH_LONG).show();
    }
  }

  /** Display the folder with the adapter.
   *
   * @param folder to be displayed
   */
  private void setAdapter(File folder){
    if(this.searchQuery.isEmpty()){
      this.displayedItems = BrowserManager.getChildren(folder, BrowserManager.Filter.SONGS);
    } else {
      this.displayedItems = BrowserManager.getChildrenMatchingQuery(folder, this.searchQuery, BrowserManager.Filter.SONGS);
    }
    this.folderListAdapter = new FolderListAdapter(folderFragment.getContext(),this.displayedItems);
    folderFragment.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        folderFragment.lv_folderList.setAdapter(folderListAdapter);
      }
    });
  }

  /** If clicked on "ll_back_folder" go to parent folder
   *  (if (1) it has parent (2) the folder is not the root).
   *
   * @param v View that is clicked
   */
  @Override
  public void onClick(View v) {
    try {
      if(v.getId() == R.id.ll_back_folder){
        if(this.selectedFolder != null
        && !this.selectedFolder.equals(BrowserManager.getInstance(folderFragment.getContext()).getRootFolder())
        && this.selectedFolder.getParent() != null){
          this.selectedFolder = this.selectedFolder.getParentFile();
          setAdapter(this.selectedFolder);
        }
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(folderFragment.getContext(), "ERROR: Could not go back!", Toast.LENGTH_LONG).show();
    }
  }

  /** If clicked on "ll_back_folder" go to parent folder
   *  (if (1) it has parent (2) the folder is not the root).
   *
   */
  public boolean onBackPressed() {
    try {
      if(this.selectedFolder != null
      && !this.selectedFolder.equals(BrowserManager.getInstance(folderFragment.getContext()).getRootFolder())
      && this.selectedFolder.getParent() != null){
        this.selectedFolder = this.selectedFolder.getParentFile();
        //setAdapter
        setAdapter(this.selectedFolder);
        return true;
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(folderFragment.getContext(), "ERROR: Could not go back!", Toast.LENGTH_LONG).show();
    }
    return false;
  }

  /** Defines the options of the context menu in the folder fragment:
   *  1) add to favorites
   *  2) add to playlist
   *
   * @param menu
   * @param v
   * @param menuInfo
   */
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    try {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
      int clickedPosition = info.position;
      //only show context menu if clicked on song
      if((BrowserManager.getDirectories(this.selectedFolder, BrowserManager.Filter.SONGS).length <= clickedPosition)){
        menu.add(0, 1, 0, "zu Favoriten hinzufügen");
        menu.add(0, 2, 0, "zu Playlists hinzufügen");
        menu.add(0, 3, 0, "als nächstes abspielen");
        menu.add(0, 4, 0, "Song löschen");
      }
    } catch (Exception e){
      Log.e(TAG,e.toString());
      Toast.makeText(folderListAdapter.getContext(), "ERROR: Could not open context menu", Toast.LENGTH_LONG).show();
    }
  }

  /** Implements the options of the context menu (defined above, in onCreateContextMenu(...))
   *
   *
   * @param item
   * @return
   */
  public boolean onContextItemSelected(MenuItem item) {
    try {
      //only react to context menu in this fragment (with id 0)
      if(item.getGroupId() == 0){
        //calculate th
        // e index of the song clicked
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        index -= BrowserManager.getDirectories(this.selectedFolder, BrowserManager.Filter.SONGS).length;
        Song selectedSong = BrowserManager.getChildSongs(this.selectedFolder)[index];

        switch (item.getItemId()){
          case 1: {
            DBPlaylists.getInstance(folderFragment.getActivity()).addToFavorites(folderFragment.getContext(),selectedSong);
            refreshAfterSongDeletion();
            break;
          }
          case 2:  {
            showAlertDialogAddToPlaylists(folderFragment.getLayoutInflater(), folderListAdapter.getContext(), selectedSong);
            break;
          }
          case 3: {
            NextSongToPlayUtility.setSongToPlayNext(selectedSong);
            return true;
          }
          case 4: {
            BrowserManager.deleteFile(this, folderFragment.getContext(), ((MainActivity) folderFragment.getActivity()).getDeletetionIntentSenderLauncher(), selectedSong.getPath());
            return true;
          }
        }

        ((PlayListFragment) ((MainActivity) folderFragment.getActivity())
                .sectionsPagerAdapter.getFragments()[2])
                .getPlaylistManager().loadPlaylistsFromDB(folderFragment.getActivity());
        ((PlayListFragment) ((MainActivity) folderFragment.getActivity())
                .sectionsPagerAdapter.getFragments()[2])
                .playlistFragmentListener.playlistListAdapter.notifyDataSetChanged();
      }
      return true;
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(folderFragment.getContext(),item.getItemId()==1 ? "ERROR: Could not add song to favorites." : "ERROR: Could not song add to playlist.", Toast.LENGTH_LONG).show();
      return false;
    }
  }



  public void startFolderSonglist(File[] playlist, int song_index, FolderFragment folderFragment){
    FolderSonglist = playlist.clone();
    Intent intent = new Intent(folderFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(FolderFragment.FOLDERSONGINDEXEXTRA,song_index);
    folderFragment.startActivity(intent);
  }


  public static File[] getFolderSonglist(){
    try {
      return FolderFragmentListener.FolderSonglist.clone();
    } catch (Exception e){
      Log.e(TAG, e.toString());
      return new File[0];
    }
  }

  public void searchMusic(String query) {
    this.searchQuery = query;
    try {
      //setAdapter
      setAdapter(this.selectedFolder);
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(folderFragment.getContext(), "ERROR: Could not click on item.", Toast.LENGTH_LONG).show();
    }
  }

  public void sortSongsByName() {
    Toast.makeText(folderFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByArtist() {
    Toast.makeText(folderFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByDuration() {
    Toast.makeText(folderFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByGenre() {
    Toast.makeText(folderFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void reverse() {
    Toast.makeText(folderFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }


  @Override
  public void refreshAfterSongDeletion() {
    // Simulate refreshing (e.g., fetch new data)
    new Thread(() -> {
      //implement refresh
      String selectedFolderPath = this.selectedFolder.getPath();
      BrowserManager.reloadFilesInstance(folderFragment.getContext());
      this.selectedFolder = new File(selectedFolderPath);
      setAdapter(this.selectedFolder);

      if (folderFragment.getActivity() != null) {
        folderFragment.getActivity().runOnUiThread(() -> {
          // Stop the refreshing animation
          folderFragment.srl_folder.setRefreshing(false);
        });
      }
    }).start();
  }
}
