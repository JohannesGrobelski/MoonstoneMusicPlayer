package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = FolderFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  private final FolderFragment folderFragment;
  private FolderListAdapter folderListAdapter;

  public FolderFragmentListener(FolderFragment folderFragment) {
    this.folderFragment = folderFragment;
    if(folderFragment.selectedFolder!=null)setAdapter(folderFragment.selectedFolder);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(DEBUG)Log.d(TAG,"\n\n\n\n\n");

    //set back text
    folderFragment.tv_folder_back.setText(folderFragment.selectedFolder.getName());

    if( folderFragment.selectedFolder.getChildren_folders() != null &&
        position < folderFragment.selectedFolder.getChildren_folders().length){ //selected Folder
      Folder selectedFolder = folderFragment.selectedFolder.getChildren_folders()[position];
      if(DEBUG)Log.d(TAG,"onItemClick: selected_folder "+selectedFolder.getName());
      if(DEBUG)Log.d(TAG,"onItemClick: selected_folder "+selectedFolder.toString());
      if(selectedFolder.getChildren_folders()!=null){
        if(DEBUG)if(DEBUG)Log.d(TAG,"onItemClick: selected_folder children:"+selectedFolder.getChildren_folders().length);
        for(Folder subfolder: selectedFolder.getChildren_folders()){
          if(subfolder == null)if(DEBUG)Log.d(TAG,"onItemClick: selected_folder has null child");
          else Log.d(TAG,"onItemClick: selected_folder child: "+subfolder.getName());
        }
      }
      if(selectedFolder != null){
        //setAdapter
        folderFragment.selectedFolder = selectedFolder;
        if(DEBUG)Log.d(TAG,"onItemClick: new selected_folder "+selectedFolder.toString());
        setAdapter(folderFragment.selectedFolder);
      } else {
        if(DEBUG)Log.d(TAG,"itemClick FEHLER");
      }
    } else { //selected Song
      Song[] playlist = folderFragment.selectedFolder.getChildren_songs();
      int song_index = position;
      if(folderFragment.selectedFolder.getChildren_folders() != null){
        song_index = position - folderFragment.selectedFolder.getChildren_folders().length; //folders are always written before songs
      }
      folderFragment.startFolderSonglist(playlist,song_index);
    }
  }

  private void setAdapter(Folder folder){
    List<Object> children = new ArrayList<>();
    if(folder.getChildren_folders()!=null)children.addAll(Arrays.asList(folder.getChildren_folders()));
    if(folder.getChildren_songs()!=null)children.addAll(Arrays.asList(folder.getChildren_songs()));
    folderListAdapter = new FolderListAdapter(folderFragment.getContext(),children);
    folderFragment.lv_folderList.setAdapter(folderListAdapter);
  }

  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_folder){
      if(folderFragment.selectedFolder != null && folderFragment.selectedFolder.getParent() != null){

        folderFragment.selectedFolder = folderFragment.selectedFolder.getParent();
        if(folderFragment.selectedFolder.getParent() != null){
          folderFragment.tv_folder_back.setText(folderFragment.selectedFolder.getName());
        } else {
          folderFragment.tv_folder_back.setText("...");
        }
        //setAdapter
        setAdapter(folderFragment.selectedFolder);
      }
    }
  }
}
