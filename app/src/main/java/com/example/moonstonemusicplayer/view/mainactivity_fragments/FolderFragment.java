package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment.FolderFragmentListener;

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


  //Views
  public ListView lv_folderList;
  private LinearLayout ll_folder_back;
  private TextView tv_folder_back;

  public FolderFragment() {/*empty constructor, no context*/}

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
    initViews();
    registerForContextMenu(lv_folderList);

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


  private void initViews(){
    lv_folderList.setOnItemClickListener(folderFragmentListener);
    ll_folder_back.setOnClickListener(folderFragmentListener);
  }

  public void searchMusic(String query) {
    folderFragmentListener.searchMusic(query);
  }

  public void sortSongsByName() {
    folderFragmentListener.sortSongsByName();
  }

  public void sortSongsByArtist() {
    folderFragmentListener.sortSongsByArtist();
  }

  public void sortSongsByDuration() {
    folderFragmentListener.sortSongsByDuration();
  }

  public void sortSongsByGenre() {
    folderFragmentListener.sortSongsByGenre();
  }

  public boolean onBackpressed() {
    return folderFragmentListener.onBackPressed();
  }

  public void reverse() {
    folderFragmentListener.reverse();
  }
}