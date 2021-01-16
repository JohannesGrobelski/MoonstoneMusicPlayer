package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment.FavoriteFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment.FavoritesManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String TAG = FavoritesFragment.class.getSimpleName();

  public FavoritesManager favoritesManager;
  public FavoriteFragmentListener favoriteFragmentListener;

  public ListView lv_favorites;

  public FavoritesFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param
   * @return A new instance of fragment FavoritesFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static FavoritesFragment newInstance(int index) {
    FavoritesFragment fragment = new FavoritesFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);

    lv_favorites = rootView.findViewById(R.id.lv_favorites);
    favoriteFragmentListener = new FavoriteFragmentListener(this);
    registerForContextMenu(lv_favorites);

    lv_favorites.setOnItemClickListener(favoriteFragmentListener);
    return rootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    favoritesManager = new FavoritesManager(context);
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
    favoriteFragmentListener.onCreateContextMenu(menu, v, menuInfo);
  }


  public void searchMusic(String query) {
    if(!query.isEmpty()){
      Log.d("search music",query);
      Song[] matchingSongs = favoritesManager.getAllSongsMatchingQuery(query);

      favoritesManager.setFavorites(new ArrayList<Song>(Arrays.asList(matchingSongs)));
      favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
    } else {
      Log.d("search music","empty");
      favoriteFragmentListener.setAdapter(favoritesManager.getAllFavorites());
    }
  }

  public void sortSongsByName() {
    favoritesManager.sortFavoritesByName();
    favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
  }

  public void sortSongsByArtist() {
    favoritesManager.sortSongsByArtist();
    favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
  }

  public void sortSongsByDuration() {
    favoritesManager.sortSongsByDuration();
    favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
  }

  public void sortSongsByGenre() {
    favoritesManager.sortSongsByGenre();
    favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
  }

  public void reverse() {
    favoritesManager.reverse();
    favoriteFragmentListener.setAdapter(favoritesManager.getFavorites());
  }
}