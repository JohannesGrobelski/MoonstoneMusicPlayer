/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment;

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
import com.example.moonstonemusicplayer.model.NextSongToPlayUtility;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AudiobookFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudiobookFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener, BrowserManager.AfterFileDeletion {
  private static final String TAG = AudiobookFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  private static File[] AudiobookAudiobooklist;
  private final AudiobookFragment audiobookFragment;
  private AudiobookListAdapter folderListAdapter;

  private File selectedAudiobook;

  private List<File> displayedItems = new ArrayList<>();

  String searchQuery = "";

  /** Sets reference to folder fragment and init selectedAudiobook to rootAudiobook
   *  i.e. the root folder will be displayed with the adapter
   *
   * @param audiobookFragment
   */
  public AudiobookFragmentListener(AudiobookFragment audiobookFragment) {
    this.audiobookFragment = audiobookFragment;
    selectedAudiobook = BrowserManager.getInstance(audiobookFragment.getContext()).getRootFolder();
    if(selectedAudiobook!=null){
      setAdapter(selectedAudiobook);
    }
  }

  /** If folder is selected go into the folder, if audiobook is selected start PlaylistActivity.
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
        if(itemClicked.isDirectory()){ //selected Audiobook
          //setAdapter
          this.selectedAudiobook = itemClicked;
          setAdapter(this.selectedAudiobook);
        } else { //selected Audiobook
          File[] playlist = BrowserManager.getChildFiles(this.selectedAudiobook, BrowserManager.Filter.AUDIOBOOKS);
          startAudiobookAudiobooklist(playlist, position, audiobookFragment);
        }
      } else {
        startAudiobookAudiobooklist(displayedItems.toArray(new File[0]), position, audiobookFragment);
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(audiobookFragment.getContext(), "ERROR: Could not click on item.", Toast.LENGTH_LONG).show();
    }
  }

  /** Display the folder with the adapter.
   *
   * @param folder to be displayed
   */
  private void setAdapter(File folder){
    if(this.searchQuery.isEmpty()){
      this.displayedItems = BrowserManager.getChildren(folder, BrowserManager.Filter.AUDIOBOOKS);
    } else {
      this.displayedItems = BrowserManager.getChildrenMatchingQuery(folder, this.searchQuery, BrowserManager.Filter.AUDIOBOOKS);
    }
    this.folderListAdapter = new AudiobookListAdapter(audiobookFragment.getContext(),this.displayedItems);
    audiobookFragment.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        audiobookFragment.lv_folderList.setAdapter(folderListAdapter);
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
        if(this.selectedAudiobook != null
        && !this.selectedAudiobook.equals(BrowserManager.getInstance(audiobookFragment.getContext()).getRootFolder())
        && this.selectedAudiobook.getParent() != null){
          this.selectedAudiobook = this.selectedAudiobook.getParentFile();
          setAdapter(this.selectedAudiobook);
        }
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(audiobookFragment.getContext(), "ERROR: Could not go back!", Toast.LENGTH_LONG).show();
    }
  }

  /** If clicked on "ll_back_folder" go to parent folder
   *  (if (1) it has parent (2) the folder is not the root).
   *
   */
  public boolean onBackPressed() {
    try {
      if(this.selectedAudiobook != null
      && !this.selectedAudiobook.equals(BrowserManager.getInstance(audiobookFragment.getContext()).getRootFolder())
      && this.selectedAudiobook.getParent() != null){
        this.selectedAudiobook = this.selectedAudiobook.getParentFile();
        //setAdapter
        setAdapter(this.selectedAudiobook);
        return true;
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(audiobookFragment.getContext(), "ERROR: Could not go back!", Toast.LENGTH_LONG).show();
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
                
      // Check if menu info is null
      if (info == null) {
        // Handle the case where info is null
        Toast.makeText(audiobookFragment.getActivity(),"Could not open context menu.", Toast.LENGTH_LONG).show();
        Log.e("onContextItemSelected", "MenuInfo is null for item"); 
      }        

      int clickedPosition = info.position;
      //only show context menu if clicked on song
      if(!this.selectedAudiobook.listFiles()[clickedPosition].isDirectory()){
        menu.add(0, 1, 0, "zu Favoriten hinzufügen");
        menu.add(0, 2, 0, "zu Playlists hinzufügen");
        menu.add(0, 3, 0, "als nächstes abspielen");
        menu.add(0, 4, 0, "Hörbuch löschen");
      }
    } catch (Exception e){
      Log.e(TAG,e.toString());
      Toast.makeText(folderListAdapter.getContext(), "ERROR: Could not open context menu", Toast.LENGTH_LONG).show();
    }
  }

  /** Implements the options of the context menu (defined above, in onCreateContextMenu(...))
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
        // Check if menu info is null
        if (info == null) {
            // Handle the case where info is null
            Toast.makeText(audiobookFragment.getActivity(),"Could not open context menu.", Toast.LENGTH_LONG).show();
            Log.e("onContextItemSelected", "MenuInfo is null for item: " + item.getTitle());
            return false; 
        }        

        int index = info.position;
        Song selectedSong = BrowserManager.getChildAudiobooks(this.selectedAudiobook)[index];

        switch (item.getItemId()){
          case 1: {
            DBPlaylists.getInstance(audiobookFragment.getActivity()).addToFavorites(audiobookFragment.getContext(),selectedSong);
            refreshAfterSongDeletion();
            break;
          }
          case 2:  {
            showAlertDialogAddToPlaylists(audiobookFragment.getLayoutInflater(), folderListAdapter.getContext(), selectedSong);
            break;
          }
          case 3: {
            NextSongToPlayUtility.setSongToPlayNext(selectedSong);
            return true;
          }
          case 4: {
            BrowserManager.deleteFile(this, audiobookFragment.getContext(), ((MainActivity) audiobookFragment.getActivity()).getDeletetionIntentSenderLauncher(),selectedSong.getPath());
            return true;
          }
        }

        ((PlayListFragment) ((MainActivity) audiobookFragment.getActivity())
                .sectionsPagerAdapter.getFragments()[2])
                .getPlaylistManager().loadPlaylistsFromDB(audiobookFragment.getActivity());
        ((PlayListFragment) ((MainActivity) audiobookFragment.getActivity())
                .sectionsPagerAdapter.getFragments()[2])
                .playlistFragmentListener.playlistListAdapter.notifyDataSetChanged();
      }
      return true;
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(audiobookFragment.getContext(),item.getItemId()==1 ? "ERROR: Could not add song to favorites." : "ERROR: Could not song add to playlist.", Toast.LENGTH_LONG).show();
      return false;
    }
  }


  public void startAudiobookAudiobooklist(File[] playlist, int audiobook_index, AudiobookFragment audiobookFragment){
    AudiobookAudiobooklist = playlist.clone();
    Intent intent = new Intent(audiobookFragment.getActivity(), PlayListActivity.class);

    intent.putExtra(AudiobookFragment.FOLDERAUDIOBOOKINDEXEXTRA,audiobook_index);
    audiobookFragment.startActivity(intent);
  }


  public static File[] getAudiobookAudiobooklist(){
    try {
      return AudiobookFragmentListener.AudiobookAudiobooklist.clone();
    } catch (Exception e){
      Log.e(TAG, e.toString());
      return new File[0];
    }
  }

  public void searchMusic(String query) {
    this.searchQuery = query;
    try {
      //setAdapter
      setAdapter(this.selectedAudiobook);
    } catch (Exception e){
      Log.e(TAG, e.toString());
      Toast.makeText(audiobookFragment.getContext(), "ERROR: Could not click on item.", Toast.LENGTH_LONG).show();
    }
  }

  public void reverse() {
    Toast.makeText(audiobookFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }


  public void sortSongsByName() {
    Toast.makeText(audiobookFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByArtist() {
    Toast.makeText(audiobookFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByDuration() {
    Toast.makeText(audiobookFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  public void sortSongsByGenre() {
    Toast.makeText(audiobookFragment.getContext(), "TO BE IMPLEMENTED", Toast.LENGTH_LONG).show();
  }

  @Override
  public void refreshAfterSongDeletion() {
    // Simulate refreshing (e.g., fetch new data)
    new Thread(() -> {
      //implement refresh
      String selectedFolderPath = this.selectedAudiobook.getPath();
      BrowserManager.reloadFilesInstance(audiobookFragment.getContext());
      this.selectedAudiobook = new File(selectedFolderPath);
      setAdapter(this.selectedAudiobook);

      if (audiobookFragment.getActivity() != null) {
        audiobookFragment.getActivity().runOnUiThread(() -> {
          // Stop the refreshing animation
          audiobookFragment.srl_folder.setRefreshing(false);
        });
      }
    }).start();
  }
}
