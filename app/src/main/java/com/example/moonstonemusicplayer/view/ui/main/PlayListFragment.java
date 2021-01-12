package com.example.moonstonemusicplayer.view.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.PlaylistListManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlaylistManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;


/**
 * A fragment to view and select the saved playlists.
 */
public class PlayListFragment extends Fragment {

  private static final String ARG_SECTION_NUMBER = "section_number";
  private static final String TAG = PlayListFragment.class.getSimpleName();

  private PageViewModel pageViewModel;
  public PlaylistListManager playlistListManager;
  PlaylistFragmentListener playlistFragmentListener;

  LinearLayout ll_playlistBack;
  public ListView lv_playlist;

  public static PlayListFragment newInstance(int index) {
    PlayListFragment fragment = new PlayListFragment();
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
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_playlist, container, false);

    lv_playlist = root.findViewById(R.id.lv_playlistSongList);
    ll_playlistBack =root.findViewById(R.id.ll_back_playlist);

    playlistFragmentListener = new PlaylistFragmentListener(this);

    lv_playlist.setOnItemClickListener(playlistFragmentListener);
    ll_playlistBack.setOnClickListener(playlistFragmentListener);
    return root;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    playlistListManager = new PlaylistListManager(this.getContext());
  }

}