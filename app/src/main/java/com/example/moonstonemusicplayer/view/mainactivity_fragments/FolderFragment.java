package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment.FolderFragmentListener;
import com.example.moonstonemusicplayer.controller.PlayListActivity.RefreshTask;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FolderFragment extends Fragment {
  public static final String FOLDERSONGINDEXEXTRA = "SONG_INDEX";
  private static final String TAG = FolderFragment.class.getSimpleName();
  private static final boolean DEBUG = false;
  private FolderFragmentListener folderFragmentListener;

  private FolderManager folderManager;

  public Folder selectedFolder;

  public ListView lv_folderList;
  private LinearLayout ll_folder_back;
  private TextView tv_folder_back;

  public FolderFragment() {/*empty constructor, no context*/}

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    folderManager = new FolderManager(this.getContext());
    loadMusicAsFolders();
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param index Parameter 1.
   * @return A new instance of fragment FolderFragment.
   */
  public static FolderFragment newInstance(int index) {
    FolderFragment fragment = new FolderFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }




  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, //zum Layout verbinden
                           @Nullable ViewGroup container,    //wo das Layout angezeigt wird
                           @Nullable Bundle savedInstanceState) { //falls es gespeicherte Werte gibt
    //Das Layout verbinden für das Fragment
    View view = inflater.inflate(R.layout.fragment_folder, container, false);
    //Referenz des listviews
    lv_folderList = view.findViewById(R.id.lv_folderlist);
    ll_folder_back = view.findViewById(R.id.ll_back_folder);
    tv_folder_back = view.findViewById(R.id.tv_folderBack);

    folderFragmentListener = new FolderFragmentListener(this);
    if(selectedFolder != null){
      initViews();
    }
    registerForContextMenu(lv_folderList);

    /*
    fragmentListener = new FirstFragmentListener(this);
    btnFirst.setOnClickListener(fragmentListener);
     */

    //rückgabe des Fragmentviews
    return view;
  }


  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    folderFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(@NonNull MenuItem item) {
    return folderFragmentListener.onContextItemSelected(item);
  }

  public void loadMusicAsFolders(){
    folderManager.loadSavedMusicAsFolder(this.getContext());
    selectedFolder = folderManager.getRootFolder();
    if(selectedFolder != null)Log.d(TAG,"loadedMusicFromXML: rootfolder: \n"+selectedFolder.toString());
  }

  public void reloadAllMusic(){
    //refresh per asynctask and dont block ui thread
    RefreshTask refreshTask = new RefreshTask(new FolderFragment.RefreshTaskListener() {
      @Override
      public void onCompletion() {
        //after completion update selected folder and listview
        selectedFolder = folderManager.getRootFolder();
        if(selectedFolder != null && selectedFolder.getChildren_folders() != null
        && selectedFolder.getChildren_folders().length > 0){

        } else {
          Toast.makeText(FolderFragment.this.getContext(), R.string.no_songs_found,Toast.LENGTH_LONG).show();
        }
        System.out.println("rootFolder: "+selectedFolder.toString());
        folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
        initViews();
      }
    });
    refreshTask.execute(folderManager);
  }



  private void initViews(){
    if(DEBUG)Log.d(TAG, "rootFolder: "+selectedFolder.toString());
    String[] children = selectedFolder.getAllChildrenAsStrings();
    if(DEBUG)Log.d(TAG, "rootFolder: "+ Arrays.toString(children));

    lv_folderList.setOnItemClickListener(folderFragmentListener);
    ll_folder_back.setOnClickListener(folderFragmentListener);
  }

  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Folder[] matchingFolders = folderManager.getAllFoldersMatchingQuery(query);
      Song[] matchingSongs = folderManager.getAllSongsMatchingQuery(query);
      selectedFolder = new Folder("searchFolder",null,null,matchingFolders,matchingSongs);
    } else {
      selectedFolder = folderManager.getRootFolder();
    }
    //refresh adapter
    folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
    initViews();
  }

  public void sortSongsByName() {
    if(selectedFolder != null) {
      selectedFolder.sortSongsByName();
      folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
      initViews();
    }
  }

  public void sortSongsByArtist() {
    if(selectedFolder != null) {
      selectedFolder.sortSongsByArtist();
      folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
      initViews();
    }
  }

  public void sortSongsByDuration() {
    if(selectedFolder != null) {
      selectedFolder.sortSongsByDuration();
      folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
      initViews();
    }
  }

  public void sortSongsByGenre() {
    if(selectedFolder != null) {
      selectedFolder.sortSongsByGenre();
      folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
      initViews();
    }
  }

  public boolean onBackpressed() {
    return folderFragmentListener.onBackPressed();
  }

  public void reverse() {
    if(selectedFolder != null){
      selectedFolder.reverse();
      folderFragmentListener = new FolderFragmentListener(FolderFragment.this);
      initViews();
    }

  }


  /** */
  public interface RefreshTaskListener {
    void onCompletion();
  }
}