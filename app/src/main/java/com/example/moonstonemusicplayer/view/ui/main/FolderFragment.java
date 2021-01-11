package com.example.moonstonemusicplayer.view.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FolderFragment extends Fragment {
  private static final boolean DEBUG = true;
  FolderFragmentListener folderFragmentListener;

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final String TAG = FolderFragment.class.getSimpleName();

  FolderManager folderManager;

  public Folder selectedFolder;
  public ListView lv_folderList;
  public Button btn_folder_back;

  public static Song[] playlist;

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  public FolderFragment() {/*empty constructor, no context*/}

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    if(this.getContext() != null){
      folderManager = new FolderManager(this.getContext());
      loadMusicAsFolders();
    } else {
      Log.e(TAG,"context null");
    }
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
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_SECTION_NUMBER, index);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  public void loadMusicAsFolders(){
    folderManager.loadSavedMusicAsFolder(this.getContext());
    selectedFolder = folderManager.getRootFolder();
    if(selectedFolder != null)Log.d(TAG,"loadedMusicFromXML: rootfolder: \n"+selectedFolder.toString());
  }

  public void loadMusicNew(){
    folderManager.loadLocalMusicAsFolder(this.getContext());
    selectedFolder = folderManager.getRootFolder();
    initViews();
  }

  private void initViews(){
    if(DEBUG)Log.d(TAG, "rootFolder: "+selectedFolder.toString());
    String[] children = selectedFolder.getAllChildrenAsStrings();
    if(DEBUG)Log.d(TAG, "rootFolder: "+ Arrays.toString(children));

    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1,children);
    lv_folderList.setAdapter(arrayAdapter);
    lv_folderList.setOnItemClickListener(folderFragmentListener);
    btn_folder_back.setOnClickListener(folderFragmentListener);
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
    btn_folder_back = view.findViewById(R.id.btn_folder_back);

    folderFragmentListener = new FolderFragmentListener(this);
    if(selectedFolder != null){
      initViews();
    }

    /*
    fragmentListener = new FirstFragmentListener(this);
    btnFirst.setOnClickListener(fragmentListener);
     */

    //rückgabe des Fragmentviews
    return view;
  }

  public void startPlaylist(Song[] playlist){
    Intent intent = new Intent(getActivity(), PlayListActivity.class);
    startActivity(intent);
  }

  public static Song[] getPlaylist(){
    Song[] playlistCopy = playlist.clone();
    playlist = null;
    return playlistCopy;
  }

}