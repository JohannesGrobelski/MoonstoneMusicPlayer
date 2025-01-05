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
import com.example.moonstonemusicplayer.controller.MainActivity.ArtistFragment.ArtistFragmentListener;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.Artist;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.ArtistManager;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.ColorSettingsFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ArtistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String TAG = ArtistFragment.class.getSimpleName();

  public ArtistManager artistManager;
  public ArtistFragmentListener artistFragmentListener;

  public ListView lv_artists;
  public LinearLayout ll_artists_back;

  public ArtistFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param
   * @return A new instance of fragment albumsFragment.
   */
  public static ArtistFragment newInstance() {
    ArtistFragment fragment = new ArtistFragment();
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

    lv_artists = rootView.findViewById(R.id.lv_albums);
    ll_artists_back = rootView.findViewById(R.id.ll_back_album);

    artistFragmentListener = new ArtistFragmentListener(this);
    registerForContextMenu(lv_artists);

    ll_artists_back.setOnClickListener(artistFragmentListener);
    lv_artists.setOnItemClickListener(artistFragmentListener);

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
    artistManager = new ArtistManager(context);
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    artistFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }


  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Log.d("search music",query);
      Artist[] matchingArtists = artistManager.getAllArtistsMatchingQuery(query);

      artistManager.setArtistList(new ArrayList<>(Arrays.asList(matchingArtists)));
      artistFragmentListener.setAdapterAlbumList(artistManager.getAlbumList());
    } else {
      Log.d("search music","empty");
      artistFragmentListener.setAdapterArtistList((artistManager.getAllArtists()));
    }
  }

  public void sortSongsByName() {
    artistManager.sortSongsByName();
    if(artistManager.getCurrentAlbum() != null){
      artistFragmentListener.setAdapterSongList(artistManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByArtist() {
    artistManager.sortSongsByArtist();
    if(artistManager.getCurrentAlbum() != null){
      artistFragmentListener.setAdapterSongList(artistManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByDuration() {
    artistManager.sortSongsByDuration();
    if(artistManager.getCurrentAlbum() != null){
      artistFragmentListener.setAdapterSongList(artistManager.getCurrentAlbum().getSongList());
    }
  }

  public void sortSongsByGenre() {
    artistManager.sortSongsByGenre();
    if(artistManager.getCurrentAlbum() != null){
      artistFragmentListener.setAdapterSongList(artistManager.getCurrentAlbum().getSongList());
    }
  }

  public void reverse() {
    artistManager.reverse();
    if(artistManager.getCurrentAlbum() != null){
      artistFragmentListener.setAdapterSongList(artistManager.getCurrentAlbum().getSongList());
    }
  }

  public boolean onBackpressed() {
    return artistFragmentListener.onBackPressed();
  }
}
