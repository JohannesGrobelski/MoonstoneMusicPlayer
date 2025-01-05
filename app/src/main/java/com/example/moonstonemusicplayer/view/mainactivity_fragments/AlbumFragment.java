/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.AlbumsFragment.AlbumFragmentListener;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.AlbumManager;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.ColorSettingsFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlbumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String TAG = AlbumFragment.class.getSimpleName();

  public AlbumManager albumManager;
  public AlbumFragmentListener albumFragmentListener;

  public ListView lv_albums;
  public LinearLayout ll_album_back;

  public AlbumFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param
   * @return A new instance of fragment albumsFragment.
   */
  public static AlbumFragment newInstance() {
    AlbumFragment fragment = new AlbumFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_albums, container, false);

    lv_albums = rootView.findViewById(R.id.lv_albums);
    ll_album_back = rootView.findViewById(R.id.ll_back_album);

    albumFragmentListener = new AlbumFragmentListener(this);
    registerForContextMenu(lv_albums);

    ll_album_back.setOnClickListener(albumFragmentListener);
    lv_albums.setOnItemClickListener(albumFragmentListener);

    //set color of iv_albumBack icon
    rootView.findViewById(R.id.iv_albumBack).setBackground(
            DrawableUtils.getTintedDrawable(
                    rootView.getContext(),
                    R.drawable.ic_folder_up,
                    ColorSettingsFragment.getPrimaryColor(rootView.getContext())
            )
    );

    return rootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    albumManager = new AlbumManager(context);
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    albumFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }


  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Log.d("search music",query);
      Album[] matchingAlbums = albumManager.getAllAlbumsMatchingQuery(query);

      albumManager.setAlbumList(new ArrayList<>(Arrays.asList(matchingAlbums)));
      albumFragmentListener.setAdapterAlbumList(albumManager.getAlbumList());
    } else {
      Log.d("search music","empty");
      albumFragmentListener.setAdapterAlbumList((albumManager.getAllAlbums()));
    }
  }

  public void sortSongsByName() {
    albumManager.sortSongsByName();
    if(albumManager.getCurrentAlbum() != null){
      albumFragmentListener.setAdapterSongList(albumManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByArtist() {
    albumManager.sortSongsByArtist();
    if(albumManager.getCurrentAlbum() != null){
      albumFragmentListener.setAdapterSongList(albumManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByDuration() {
    albumManager.sortSongsByDuration();
    if(albumManager.getCurrentAlbum() != null){
      albumFragmentListener.setAdapterSongList(albumManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByGenre() {
    albumManager.sortSongsByGenre();
    if(albumManager.getCurrentAlbum() != null){
      albumFragmentListener.setAdapterSongList(albumManager.getCurrentAlbum().getSongList());
    }
  }

  public void reverse() {
    albumManager.reverse();
    if(albumManager.getCurrentAlbum() != null){
      albumFragmentListener.setAdapterSongList(albumManager.getCurrentAlbum().getSongList());
    }
  }

  public boolean onBackpressed() {
    return albumFragmentListener.onBackPressed();
  }
}
