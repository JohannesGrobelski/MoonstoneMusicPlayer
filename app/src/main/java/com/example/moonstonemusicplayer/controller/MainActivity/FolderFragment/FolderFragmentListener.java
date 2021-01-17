package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import android.app.AlertDialog;
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

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = FolderFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  private static Song[] FolderSonglist;
  private final FolderFragment folderFragment;
  private FolderListAdapter folderListAdapter;

  public FolderFragmentListener(FolderFragment folderFragment) {
    this.folderFragment = folderFragment;
    if(folderFragment.selectedFolder!=null){
      setAdapter(folderFragment.selectedFolder);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(DEBUG)Log.d(TAG,"\n\n\n\n\n");

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
      startFolderSonglist(playlist,song_index, folderFragment);
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

        //setAdapter
        setAdapter(folderFragment.selectedFolder);
      }
    }
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    int clickedPosition = info.position;
    //only show context menu if clicked on song
    if(folderFragment.selectedFolder.getChildren_folders() == null
    ||(folderFragment.selectedFolder.getChildren_folders().length <= clickedPosition)){
        menu.add(0, 1, 0, "zu Favoriten hinzufügen");
        menu.add(0, 2, 0, "zu Playlists hinzufügen");
    }
  }

  public boolean onContextItemSelected(MenuItem item) {
    //only react to context menu in this fragment
    if(item.getGroupId() == 0){
      //calculate the index of the song clicked
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      int index = info.position;
      if(folderFragment.selectedFolder.getChildren_folders() != null)index -= folderFragment.selectedFolder.getChildren_folders().length;
      Song selectedSong = folderFragment.selectedFolder.getChildren_songs()[index];

      switch (item.getItemId()){
        case 1: {
          DBPlaylists.getInstance(folderFragment.getActivity()).addToFavorites(folderFragment.getContext(),selectedSong);
          break;
        }
        case 2:  {
          showAlertDialogAddToPlaylists(selectedSong);
          break;
        }
      }
    }
    ((PlayListFragment) ((MainActivity) folderFragment.getActivity())
        .sectionsPagerAdapter.getFragments()[1])
        .playlistListManager.loadPlaylistsFromDB(folderFragment.getActivity());
    ((PlayListFragment) ((MainActivity) folderFragment.getActivity())
        .sectionsPagerAdapter.getFragments()[1])
        .playlistFragmentListener.playlistListAdapter.notifyDataSetChanged();
    return true;
  }

  private void showAlertDialogAddToPlaylists(final Song song){
    final String[] allPlaylistNames = DBPlaylists.getInstance(folderFragment.getActivity()).getAllPlaylistNames();

    LayoutInflater inflater = folderFragment.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.add_to_playlist_layout, null);
    ListView lv_playlist_alert = dialogView.findViewById(R.id.lv_playlists_alert);
    final EditText et_addNewPlaylist = dialogView.findViewById(R.id.et_addNewPlaylist);

    lv_playlist_alert.setAdapter(new ArrayAdapter<String>(folderFragment.getActivity(),android.R.layout.simple_list_item_1,allPlaylistNames));

    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(folderFragment.getActivity());
    dialogBuilder.setView(dialogView);
    dialogBuilder.setNegativeButton(android.R.string.no,null);
    dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String text = et_addNewPlaylist.getText().toString();
        if(!text.isEmpty()){
          DBPlaylists.getInstance(folderFragment.getActivity()).addToPlaylist(folderFragment.getContext(),song,text);
        }
      }
    });
    dialogBuilder.setTitle("Füge den Song einer Playlist hinzu \noder erstelle eine neue.");

    final AlertDialog alertDialog  = dialogBuilder.show();

    lv_playlist_alert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBPlaylists.getInstance(folderFragment.getActivity()).addToPlaylist(folderFragment.getContext(),song,allPlaylistNames[position]);
        alertDialog.dismiss();
      }
    });
  }


  public boolean onBackPressed() {
    if(folderFragment.selectedFolder != null && folderFragment.selectedFolder.getParent() != null){
      folderFragment.selectedFolder = folderFragment.selectedFolder.getParent();

      //setAdapter
      setAdapter(folderFragment.selectedFolder);
      return true;
    }
    return false;
  }

  public void startFolderSonglist(Song[] playlist, int song_index, FolderFragment folderFragment){
    FolderSonglist = playlist.clone();
    Intent intent = new Intent(folderFragment.getActivity(), PlayListActivity.class);

    intent.putExtra(FolderFragment.FOLDERSONGINDEXEXTRA,song_index);
    folderFragment.startActivity(intent);
  }


  public static Song[] getFolderSonglist(){
    Song[] playlistCopy = FolderFragmentListener.FolderSonglist.clone();
    FolderFragmentListener.FolderSonglist = null;
    return playlistCopy;
  }
}
