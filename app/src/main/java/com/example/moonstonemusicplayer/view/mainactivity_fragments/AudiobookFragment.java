/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment.AudiobookFragmentListener;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudiobookFragment extends Fragment {
  public static final String FOLDERAUDIOBOOKINDEXEXTRA = "FOLDERAUDIOBOOKINDEXEXTRA";

  private static final String TAG = AudiobookFragment.class.getSimpleName();
  private static final boolean DEBUG = false;
  private AudiobookFragmentListener audiobookFragmentListener;

  //Views
  public SwipeRefreshLayout srl_folder;
  public ListView lv_folderList;
  private LinearLayout ll_folder_back;
  private TextView tv_folder_back;

  public AudiobookFragment() {/*empty constructor, no context*/}

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment AudiobookFragment.
   */
  public static AudiobookFragment newInstance() {
    AudiobookFragment fragment = new AudiobookFragment();
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, //zum Layout verbinden
                           @Nullable ViewGroup container,    //wo das Layout angezeigt wird
                           @Nullable Bundle savedInstanceState) { //falls es gespeicherte Werte gibt


    //Das Layout verbinden für das Fragment
    View view = inflater.inflate(R.layout.fragment_folder, container, false);
    srl_folder = view.findViewById(R.id.srl_folder);
    //Referenz des listviews
    lv_folderList = view.findViewById(R.id.lv_folderlist);
    ll_folder_back = view.findViewById(R.id.ll_back_folder);

    tv_folder_back = view.findViewById(R.id.tv_folderBack);

    //set color of folderback icon
    view.findViewById(R.id.iv_folderBack).setBackground(
            DrawableUtils.getTintedDrawable(
                    view.getContext(),
                    R.drawable.ic_folder_up,
                    SettingsFragment.getPrimaryColor(view.getContext())
            )
    );

    view.findViewById(R.id.iv_folderBack).setBackground(
            DrawableUtils.getTintedDrawable(
                    view.getContext(),
                    R.drawable.ic_folder_up,
                    SettingsFragment.getPrimaryColor(view.getContext())
            )
    );

    audiobookFragmentListener = new AudiobookFragmentListener(this);
    initViews();
    registerForContextMenu(lv_folderList);

    //rückgabe des Fragmentviews
    return view;
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    audiobookFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(@NonNull MenuItem item) {
    return audiobookFragmentListener.onContextItemSelected(item);
  }

  private void initViews(){
    lv_folderList.setOnItemClickListener(audiobookFragmentListener);
    ll_folder_back.setOnClickListener(audiobookFragmentListener);

    srl_folder.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        audiobookFragmentListener.refreshAfterSongDeletion();
      }
    });
  }

  public void searchMusic(String query) {
    audiobookFragmentListener.searchMusic(query);
  }

  public boolean onBackpressed() {
    return audiobookFragmentListener.onBackPressed();
  }

  public void reverse() {
    audiobookFragmentListener.reverse();
  }

  public void sortSongsByName() {
    audiobookFragmentListener.sortSongsByName();
  }

  public void sortSongsByArtist() {
    audiobookFragmentListener.sortSongsByArtist();
  }

  public void sortSongsByDuration() {
    audiobookFragmentListener.sortSongsByDuration();
  }

  public void sortSongsByGenre() {
    audiobookFragmentListener.sortSongsByGenre();
  }

  public void refreshFolderList(){
    audiobookFragmentListener.refreshAfterSongDeletion();
  }
}
