/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.AlbumsFragment.AlbumFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.ArtistFragment.ArtistFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment.AudiobookFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment.FolderFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.GenreFragment.GenreFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistFragmentListener;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AudiobookFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;
import com.woxthebox.draglistview.DragListView;

import static com.example.moonstonemusicplayer.controller.MainActivity.GenreFragment.GenreFragmentListener.GENRELISTEXTRA;
import static com.example.moonstonemusicplayer.view.mainactivity_fragments.AudiobookFragment.FOLDERAUDIOBOOKINDEXEXTRA;
import static com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment.FOLDERSONGINDEXEXTRA;

import java.io.File;

/** MainActivity
  * Defines the Mainscreen auf the app.
  * Contains all Views and sets Listeners for them.
  * Delegates the creation and management (itemselection) of the optionsmenu to the  {@link com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener}.
*/
public class PlayListActivity extends AppCompatActivity {


  private static final String TAG = PlayListActivity.class.getName();
  private static final boolean DEBUG = true;
  com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener playListActivityListener;

  public Toolbar playlist_toolbar;
  public DragListView dlv_songlist;

  public Button btn_prev,btn_play_pause,btn_next,btn_shuffle,btn_repeat;
  public SeekBar seekBar;
  public TextView tv_seekbar_progress,tv_seekbar_max,tv_title,tv_artist;
  private LinearLayout LL_MusicControlls;
  public SearchView searchView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_playlist);
    playlist_toolbar = findViewById(R.id.playlist_toolbar);

    dlv_songlist = findViewById(R.id.dlv_songlist);
    btn_prev = findViewById( R.id.btn_prev);
    btn_play_pause = findViewById( R.id.btn_play_pause);
    btn_next = findViewById( R.id.btn_next);
    btn_shuffle = findViewById( R.id.btn_shuffle);
    btn_repeat = findViewById( R.id.btn_repeat);

    seekBar = findViewById(R.id.seekBar);
    tv_seekbar_progress = findViewById(R.id.tv_seekbar_progress);
    tv_seekbar_max = findViewById(R.id.tv_seekbar_max);
    tv_title = findViewById(R.id.tv_name);
    tv_artist = findViewById(R.id.tv_artist);
    LL_MusicControlls = findViewById(R.id.LL_MusicControlls);

    playlist_toolbar.setBackgroundColor(SettingsFragment.getPrimaryColor(this));

    int song_index = 0;
    String playlist_name = "";
    //fetch songlist from fragment that called playlist activity
    if(getIntent().hasExtra(FolderFragment.FOLDERSONGINDEXEXTRA)) {
      song_index = getIntent().getIntExtra(FOLDERSONGINDEXEXTRA, 0);
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, FolderFragmentListener.getFolderSonglist(), song_index, playlist_name);
    } else if(getIntent().hasExtra(AudiobookFragment.FOLDERAUDIOBOOKINDEXEXTRA)) {
      song_index = getIntent().getIntExtra(FOLDERAUDIOBOOKINDEXEXTRA, 0);
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, AudiobookFragmentListener.getAudiobookAudiobooklist(), song_index, playlist_name);
    } else if(getIntent().hasExtra(GENRELISTEXTRA)){
      song_index = getIntent().getIntExtra(GENRELISTEXTRA,0);
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, GenreFragmentListener.getGenreSonglist(),song_index, playlist_name);
    }
    else if(getIntent().hasExtra(PlaylistFragmentListener.PLAYLISTINDEXEXTRA)){
      song_index = getIntent().getIntExtra(PlaylistFragmentListener.PLAYLISTINDEXEXTRA,0);
      if(getIntent().hasExtra(PlaylistFragmentListener.PLAYLISTNAMEEXTRA)){
        playlist_name = getIntent().getStringExtra(PlaylistFragmentListener.PLAYLISTNAMEEXTRA);
      }
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, PlaylistFragmentListener.getPlaylistSonglist(),song_index,playlist_name);
    }
    else if(getIntent().hasExtra(AlbumFragmentListener.ALBUMLISTEXTRA)){
      song_index = getIntent().getIntExtra(AlbumFragmentListener.ALBUMLISTEXTRA,0);
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, AlbumFragmentListener.getAlbumSonglist(),song_index, playlist_name);
    }
    else if(getIntent().hasExtra(ArtistFragmentListener.ARTISTALBUMLISTEXTRA)){
      song_index = getIntent().getIntExtra(ArtistFragmentListener.ARTISTALBUMLISTEXTRA,0);
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, ArtistFragmentListener.getAlbumSonglist(),song_index, playlist_name);
    } else if(getIntent().hasExtra(MainActivity.SONG_DIRECT_EXTRA)){
      song_index = 0;
      String fileURL = getIntent().getStringExtra(MainActivity.SONG_DIRECT_EXTRA);
      File[] songSingleton = new File[]{new File(fileURL)};
      playListActivityListener = new com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener(this, songSingleton,song_index, playlist_name);
    } else {
      //no extras found, missing info -> go back to mainactivity
      Toast.makeText(this, "Missing Info", Toast.LENGTH_LONG);
      finish();
    }

    btn_shuffle.setOnClickListener(playListActivityListener);
    btn_prev.setOnClickListener(playListActivityListener);
    btn_play_pause.setOnClickListener(playListActivityListener);
    btn_next.setOnClickListener(playListActivityListener);
    btn_repeat.setOnClickListener(playListActivityListener);
    seekBar.setOnSeekBarChangeListener(playListActivityListener);

    registerForContextMenu(dlv_songlist);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    Toolbar toolbar = findViewById(R.id.playlist_toolbar);
    setSupportActionBar(toolbar);

    //set color of music controls
    //LL_MusicControlls.setBackgroundColor(SettingsFragment.getPrimaryColor(this));
    btn_prev.setBackground(
            DrawableUtils.getTintedDrawable(
                    this,
                    R.drawable.ic_previous,
                    SettingsFragment.getPrimaryColor(this)
            )
    );
    btn_next.setBackground(
            DrawableUtils.getTintedDrawable(
                    this,
                    R.drawable.ic_next,
                    SettingsFragment.getPrimaryColor(this)
            )
    );
    btn_play_pause.setBackground(
            DrawableUtils.getTintedDrawable(
                    this,
                    R.drawable.ic_pause,
                    SettingsFragment.getPrimaryColor(this)
            )
    );
    btn_shuffle.setBackground(
            DrawableUtils.getTintedDrawable(
                    this,
                    R.drawable.ic_shuffle,
                    SettingsFragment.getPrimaryColor(this)
            )
    );
    btn_repeat.setBackground(
            DrawableUtils.getTintedDrawable(
                    this,
                    R.drawable.ic_replay,
                    SettingsFragment.getPrimaryColor(this)
            )
    );
    seekBar.setThumbTintList(ColorStateList.valueOf(SettingsFragment.getPrimaryColor(this)));
    seekBar.setProgressTintList(ColorStateList.valueOf(SettingsFragment.getPrimaryColor(this)));

    tv_seekbar_progress.setTextColor(SettingsFragment.getPrimaryColor(this));
    tv_seekbar_max.setTextColor(SettingsFragment.getPrimaryColor(this));
    tv_title.setTextColor(SettingsFragment.getPrimaryColor(this));
    tv_artist.setTextColor(SettingsFragment.getPrimaryColor(this));
  }

  public void setPlayListActivityListener(com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener playlistActivityListener){
    this.playListActivityListener = playlistActivityListener;
  }

  @Override
  public void onBackPressed() {
    playListActivityListener.onBackPressed();
    super.onBackPressed();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    playListActivityListener.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return playListActivityListener.onCreateOptionsMenu(menu);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    playListActivityListener.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(@NonNull MenuItem item) {
    return playListActivityListener.onContextItemSelected(item);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return playListActivityListener.onOptionsItemSelected(item);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(playListActivityListener != null){
      playListActivityListener.onDestroy();
    }
  }


  public void hideMusicControlls(){LL_MusicControlls.setVisibility(View.GONE);}
  public void showMusicControlls(){LL_MusicControlls.setVisibility(View.VISIBLE);}


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      return super.onKeyDown(keyCode, event);
    }
    switch (keyCode) {
      case KeyEvent.KEYCODE_MEDIA_PLAY:
        playListActivityListener.dispatchMediaButtonEvent(event);
        return true;
    }
    return super.onKeyDown(keyCode, event);
  }

}
