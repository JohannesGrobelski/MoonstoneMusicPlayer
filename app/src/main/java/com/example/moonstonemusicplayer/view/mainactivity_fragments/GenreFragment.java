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
import com.example.moonstonemusicplayer.controller.MainActivity.GenreFragment.GenreFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.Genre;
import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.GenreManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GenreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GenreFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String TAG = GenreFragment.class.getSimpleName();
  private static final boolean DEBUG = false;

  public GenreManager genreManager;
  public GenreFragmentListener genreFragmentListener;

  public ListView lv_albums;
  public LinearLayout ll_album_back;

  public GenreFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param
   * @return A new instance of fragment albumsFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static GenreFragment newInstance(int index) {
    GenreFragment fragment = new GenreFragment();
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

    genreFragmentListener = new GenreFragmentListener(this);
    registerForContextMenu(lv_albums);

    ll_album_back.setOnClickListener(genreFragmentListener);
    lv_albums.setOnItemClickListener(genreFragmentListener);
    return rootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    genreManager = new GenreManager(context);
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {

  }

  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Genre[] matchingGenres = genreManager.getAllGenresMatchingQuery(query);

      genreManager.setGenreList(new ArrayList<>(Arrays.asList(matchingGenres)));
      genreFragmentListener.setAdapterGenreList(genreManager.getGenreList());
    } else {
      genreFragmentListener.setAdapterGenreList((genreManager.getAllGenres()));
    }
  }

  public void sortSongsByName() {
    genreManager.sortSongsByName();
    if(genreManager.getCurrentGenre() != null){
      genreFragmentListener.setAdapterSongList(genreManager.getCurrentGenre().getSongList());
    }
  }

  public void sortSongsByArtist() {
    genreManager.sortSongsByArtist();
    if(genreManager.getCurrentGenre() != null){
      genreFragmentListener.setAdapterSongList(genreManager.getCurrentGenre().getSongList());
    }
  }

  public void sortSongsByDuration() {
    genreManager.sortSongsByDuration();
    if(genreManager.getCurrentGenre() != null){
      genreFragmentListener.setAdapterSongList(genreManager.getCurrentGenre().getSongList());
    }
  }

  public void sortSongsByGenre() {
    genreManager.sortSongsByGenre();
    if(genreManager.getCurrentGenre() != null){
      genreFragmentListener.setAdapterSongList(genreManager.getCurrentGenre().getSongList());
    }
  }

  public void reverse() {
    genreManager.reverse();
    if(genreManager.getCurrentGenre() != null){
      genreFragmentListener.setAdapterSongList(genreManager.getCurrentGenre().getSongList());
    }
  }

  public boolean onBackpressed() {
    return genreFragmentListener.onBackPressed();
  }
}