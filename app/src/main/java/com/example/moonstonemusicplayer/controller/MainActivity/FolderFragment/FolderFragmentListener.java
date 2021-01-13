package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import android.app.AlertDialog;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.DBPlaylists;
import com.example.moonstonemusicplayer.model.Database.DBSonglists;
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
  public FolderListAdapter folderListAdapter;

  public FolderFragmentListener(FolderFragment folderFragment) {
    this.folderFragment = folderFragment;
    if(folderFragment.selectedFolder!=null){
      setAdapter(folderFragment.selectedFolder);
    }
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
        song_index -= folderFragment.selectedFolder.getChildren_folders().length; //folders are always written before songs
      }
      folderFragment.startFolderSonglist(playlist,song_index);
    }
  }

  private void setAdapter(Folder folder){
    List<Object> children = new ArrayList<>();
    if(folder.getChildren_folders()!=null)children.addAll(Arrays.asList(folder.getChildren_folders()));
    if(folder.getChildren_songs()!=null)children.addAll(Arrays.asList(folder.getChildren_songs()));
    this.folderListAdapter = new FolderListAdapter(folderFragment.getContext(),children);
    folderFragment.lv_folderList.setAdapter(folderListAdapter);
    Log.d(TAG,"created adapter");
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

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    folderFragment.getActivity().getMenuInflater().inflate(R.menu.song_context_menu_folderfrag_playlistact,menu);
  }

  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    int index = info.position;
    Song selectedSong = folderFragment.selectedFolder.getChildren_songs()[index];

    switch (item.getItemId()){
      case R.id.mi_addToFavorites: {
        DBPlaylists.getInstance(folderFragment.getActivity()).addToFavorites(selectedSong);
        break;
      }
      case R.id.mi_addToPlaylist:  {
        showAlertDialogAddToPlaylists(selectedSong);
        break;
      }
    }
    return true;
  }

  private void showAlertDialogAddToPlaylists(final Song song){
      final String[] allPlaylistNames = DBPlaylists.getInstance(folderFragment.getActivity()).getAllPlaylistNames();

      LayoutInflater inflater = folderFragment.getLayoutInflater();
      View dialogView = inflater.inflate(R.layout.add_to_playlist_layout, null);
      ListView lv_playlist_alert = dialogView.findViewById(R.id.lv_playlists_alert);
      lv_playlist_alert.setAdapter(new ArrayAdapter<String>(folderFragment.getActivity(),android.R.layout.simple_list_item_1,allPlaylistNames));

      final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(folderFragment.getActivity());
      dialogBuilder.setView(dialogView);
      dialogBuilder.setNegativeButton(android.R.string.no,null);
      dialogBuilder.setTitle("Add Song to a playlist:");
      dialogBuilder.show();

    lv_playlist_alert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBPlaylists.getInstance(folderFragment.getActivity()).addToPlaylist(song,allPlaylistNames[position]);
      }
    });
  }

}
