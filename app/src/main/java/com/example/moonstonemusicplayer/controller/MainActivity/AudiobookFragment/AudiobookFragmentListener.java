/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.MediaPlayerService;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AudiobookFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudiobookFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
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
          int audiobookPosition = position - BrowserManager.getDirectories(this.selectedAudiobook, BrowserManager.Filter.AUDIOBOOKS).length;
          startAudiobookAudiobooklist(playlist, audiobookPosition, audiobookFragment);
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
    audiobookFragment.lv_folderList.setAdapter(folderListAdapter);
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


  public void startAudiobookAudiobooklist(File[] playlist, int audiobook_index, AudiobookFragment audiobookFragment){
    AudiobookAudiobooklist = playlist.clone();
    Intent intent = new Intent(audiobookFragment.getActivity(), PlayListActivity.class);

    intent.putExtra(AudiobookFragment.FOLDERAUDIOBOOKINDEXEXTRA,audiobook_index);
    audiobookFragment.startActivity(intent);
  }


  public static File[] getAudiobookAudiobooklist(){
    try {
      File[] playlistCopy = AudiobookFragmentListener.AudiobookAudiobooklist.clone();
      AudiobookFragmentListener.AudiobookAudiobooklist = null;
      return playlistCopy;
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

  public void refreshFolderList(){
    // Simulate refreshing (e.g., fetch new data)
    new Thread(() -> {
      //implement refresh
      BrowserManager.reloadFilesInstance(audiobookFragment.getContext());

      if (audiobookFragment.getActivity() != null) {
        audiobookFragment.getActivity().runOnUiThread(() -> {
          // Stop the refreshing animation
          audiobookFragment.srl_folder.setRefreshing(false);
        });
      }
    }).start();
  }
}
