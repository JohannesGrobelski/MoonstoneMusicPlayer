package com.example.moonstonemusicplayer.controller.MainActivity.GenreFragment;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.Genre;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.GenreFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenreFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = GenreFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  public static final String GENRELISTEXTRA = "genrelistextra";

  private final GenreFragment genreFragment;
  private GenreListAdapter genreListAdapter;

  private static List<Song> GenreSongList;

  public GenreFragmentListener(GenreFragment genreFragment) {
    this.genreFragment = genreFragment;
    List<Genre> albumList = genreFragment.genreManager.getGenreList();
    setAdapterGenreList(albumList);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //set back text
    Object clickItem = genreListAdapter.getItem(position);
    if(clickItem != null) {
      if(clickItem instanceof Song) {
        startGenreSonglist(genreFragment.genreManager.getCurrentGenre().getSongList(),position);
      } else if(clickItem instanceof Genre){
        genreFragment.genreManager.setCurrentGenre((Genre) clickItem);
        setAdapterSongList(genreFragment.genreManager.getCurrentGenre().getSongList());
      }
      else { Log.e(TAG,"favorite list contains something different than a songs or album");}
    }
  }

  /** helper method to set the adapter */
  public void setAdapterGenreList(List<Genre> itemList){
    List<Object> objectList = new ArrayList<>(itemList);
    genreListAdapter = new GenreListAdapter(Objects.requireNonNull(genreFragment.getContext()),objectList);
    genreFragment.lv_albums.setAdapter(genreListAdapter);
  }

  /** helper method to set the adapter */
  public void setAdapterSongList(List<Song> itemList){
    List<Object> objectList = new ArrayList<>(itemList);
    genreListAdapter = new GenreListAdapter(Objects.requireNonNull(genreFragment.getContext()),objectList);
    genreFragment.lv_albums.setAdapter(genreListAdapter);
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startGenreSonglist(List<Song> albumSongList, int song_index){
    GenreSongList = new ArrayList<>(albumSongList);
    Intent intent = new Intent(genreFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(GENRELISTEXTRA,song_index);
    genreFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static File[] getGenreSonglist(){
    List<File> fileList = new ArrayList<>();
    for(Song song : GenreSongList){
      fileList.add(new File(song.getPath()));
    }
    return fileList.toArray(new File[0]);
  }

  public boolean onBackPressed(){
    if(genreFragment.genreManager.getCurrentGenre() != null){
      genreFragment.genreManager.setCurrentGenre(null);
      List<Genre> albumList = genreFragment.genreManager.getGenreList();
      setAdapterGenreList(albumList);
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
