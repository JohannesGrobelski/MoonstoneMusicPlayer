/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.ArtistFragment;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.Artist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivityListener;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.ArtistFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ArtistFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = ArtistFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  public static final String ARTISTALBUMLISTEXTRA = "artistalbumlistextra";

  private final ArtistFragment artistFragment;
  private ArtistListAdapter artistListAdapter;

  private static List<Song> AlbumSongList;

  public ArtistFragmentListener(ArtistFragment artistFragment) {
    this.artistFragment = artistFragment;
    List<Artist> artistList = artistFragment.artistManager.getArtistList();
    setAdapterArtistList(artistList);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    //set back text
    Object clickItem = artistListAdapter.getItem(position);
    if(clickItem != null) {
      if(clickItem instanceof Song) {
        startAlbumSonglist(artistFragment.artistManager.getCurrentArtist().getSongList(),position);
      } else if(clickItem instanceof Artist){
        artistFragment.artistManager.setCurrentArtist((Artist) clickItem);
        setAdapterSongList(artistFragment.artistManager.getCurrentArtist().getSongList());
      }

      else { Log.e(TAG,"favorite list contains something different than a songs or album");}
    }
  }

  public void setAdapterArtistList(List<Artist> itemList){
    List<Object> objectList = new ArrayList<>();
    objectList.addAll(itemList);
    artistListAdapter = new ArtistListAdapter(artistFragment.getContext(),objectList);
    artistFragment.lv_artists.setAdapter(artistListAdapter);
  }

  public void setAdapterAlbumList(List<Album> itemList){
    List<Object> objectList = new ArrayList<>();
    objectList.addAll(itemList);
    artistListAdapter = new ArtistListAdapter(artistFragment.getContext(),objectList);
    artistFragment.lv_artists.setAdapter(artistListAdapter);
  }

  public void setAdapterSongList(List<Song> itemList){
    List<Object> objectList = new ArrayList<>();
    objectList.addAll(itemList);
    artistListAdapter = new ArtistListAdapter(artistFragment.getContext(),objectList);
    artistFragment.lv_artists.setAdapter(artistListAdapter);
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startAlbumSonglist(List<Song> albumSongList, int song_index){
    AlbumSongList = new ArrayList<>(albumSongList);
    Intent intent = new Intent(artistFragment.getActivity(), PlayListActivityListener.class);
    intent.putExtra(ARTISTALBUMLISTEXTRA,song_index);
    artistFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static File[] getAlbumSonglist(){
    List<File> fileList = new ArrayList<>();
    for(Song song : AlbumSongList){
      fileList.add(new File(song.getPath()));
    }
    return fileList.toArray(new File[0]);
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    //only show context menu if clicked on song
    /*
    if(AlbumFragment.albumManager.getCurrentAlbum() != null){
      //create menu item with groupid to distinguish between fragments
      menu.add(2, 21, 0, "aus Favoriten löschen");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override

        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Song song = AlbumFragment.albumManager.getAlbum().get(index);
          DBPlaylists.getInstance(AlbumFragment.getContext()).deleteFromAlbum(song);

          AlbumFragment.albumManager = new AlbumManager(AlbumFragment.getContext());
          setAdapter(AlbumFragment.albumManager.getAlbum());

          return false;
        }
      });
    }
     */
  }

  public boolean onBackPressed(){
    if(artistFragment.artistManager.getCurrentAlbum() != null){
      artistFragment.artistManager.setCurrentAlbum(null);
      List<Album> albumList = artistFragment.artistManager.getAlbumList();
      setAdapterAlbumList(albumList);
      return true;
    } else if(artistFragment.artistManager.getCurrentArtist() != null){
      artistFragment.artistManager.setCurrentAlbum(null);
      artistFragment.artistManager.setCurrentArtist(null);
      List<Artist> artistList = artistFragment.artistManager.getArtistList();
      setAdapterArtistList(artistList);
      return true;
    }
    return false;
  }

  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_album){
      onBackPressed();
    }
  }
}
