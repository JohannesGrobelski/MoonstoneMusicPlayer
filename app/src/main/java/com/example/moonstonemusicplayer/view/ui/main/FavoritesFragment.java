package com.example.moonstonemusicplayer.view.ui.main;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment.FavoriteFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment.FavoriteListAdapter;
import com.example.moonstonemusicplayer.model.MainActivity.FavoritesFragment.FavoritesManager;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FavoritesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoritesFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final String TAG = FavoritesFragment.class.getSimpleName();
  private PageViewModel pageViewModel;

  public FavoritesManager favoritesManager;
  FavoriteListAdapter favoriteListAdapter;
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
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_SECTION_NUMBER, index);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
    int index = 1;
    if (getArguments() != null) {
      index = getArguments().getInt(ARG_SECTION_NUMBER);
    }
    pageViewModel.setIndex(index);
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

  public void sortFavoritesByName() {
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
}