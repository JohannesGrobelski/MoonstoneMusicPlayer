package com.example.moonstonemusicplayer.controller.MainActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

import java.util.List;

public class FolderFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = FolderFragmentListener.class.getSimpleName();
  private final FolderFragment folderFragment;

  public FolderFragmentListener(FolderFragment folderFragment) {
    this.folderFragment = folderFragment;
  }


  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Log.d(TAG,"\n\n\n\n\n");

    if( folderFragment.selectedFolder.getChildren_folders() != null){ //selected Folder
      Folder selectedFolder = folderFragment.selectedFolder.getChildren_folders()[position];
      Log.d(TAG,"onItemClick: selected_folder "+selectedFolder.getName());
      Log.d(TAG,"onItemClick: selected_folder "+selectedFolder.toString());
      if(selectedFolder.getChildren_folders()!=null){
        Log.d(TAG,"onItemClick: selected_folder children:"+selectedFolder.getChildren_folders().length);
        for(Folder subfolder: selectedFolder.getChildren_folders()){
          if(subfolder == null)Log.d(TAG,"onItemClick: selected_folder has null child");
          else Log.d(TAG,"onItemClick: selected_folder child: "+subfolder.getName());
        }
      }
      if(selectedFolder != null){
        //setAdapter
        String[] children = selectedFolder.getAllChildrenAsStrings();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
            folderFragment.getContext(),android.R.layout.simple_list_item_1,children);
        folderFragment.lv_folderList.setAdapter(arrayAdapter);
        folderFragment.selectedFolder = selectedFolder;
        Log.d(TAG,"onItemClick: new selected_folder "+selectedFolder.toString());

      } else {
        Log.d(TAG,"itemClick FEHLER");
      }
    } else { //selected Song
      Song[] playlist = folderFragment.selectedFolder.getChildren_songs();
      folderFragment.startPlaylist(playlist);
    }




  }

  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.btn_folder_back){
      if(folderFragment.selectedFolder != null && folderFragment.selectedFolder.getParent() != null){
        folderFragment.selectedFolder = folderFragment.selectedFolder.getParent();
        //setAdapter
        String[] children = folderFragment.selectedFolder.getAllChildrenAsStrings();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(folderFragment.getContext(),android.R.layout.simple_list_item_1,children);
        folderFragment.lv_folderList.setAdapter(arrayAdapter);
      }
    }
  }
}
