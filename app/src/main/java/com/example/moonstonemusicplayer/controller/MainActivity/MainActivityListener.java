/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.MediaPlayerService;
import com.example.moonstonemusicplayer.controller.PlayListActivity.PlaylistJsonHandler;
import com.example.moonstonemusicplayer.model.Database.PlaylistUtil;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AlbumFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.ArtistFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AudiobookFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.GenreFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/** Init the views from mainactivity and implement actions on them (mostly based on which fragment is active):
 *  * options menu:
 *    * define actions for menu items clicked (onOptionsItemSelected)
 *  * implement a request for runtime storage permissions (requestForPermission)
 *  * search view:
 *    * define what happens on input for search view (implementation onQueryTextChange)
 *  * define what happens if back button is pressed
 */
public class MainActivityListener implements SearchView.OnQueryTextListener {
  private static final boolean DEBUG = true;
  private static final String TAG = MainActivityListener.class.getSimpleName();

  private final MainActivity mainActivity;
  private final Fragment[] fragments;

  private MediaPlayerService mediaPlayerService;
  private boolean serviceBound = false;

  // Views for mini player
  private LinearLayout miniPlayerControls;
  private TextView miniPlayerTitle;
  private TextView miniPlayerArtist;
  private ImageButton miniPlayerPlayPause;
  private SeekBar miniPlayerSeekBar;
  private Handler seekBarHandler = new Handler();
  private Runnable seekBarRunnable;

  private ActivityResultLauncher<Intent> importPlaylistLauncher;

  // Create ServiceConnection to bind to MediaPlayerService
  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
      mediaPlayerService = binder.getService();
      serviceBound = true;
      updatePlayPauseButton(); // Update button state once service is bound
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceBound = false;
    }
  };

  /** Init fields, ask for permissions (@this. requests runtime storage permissions)
   *
   * @param mainActivity
   * @param fragments
   */
  public MainActivityListener(MainActivity mainActivity,Fragment[] fragments) {
    this.mainActivity = mainActivity;
    this.fragments = fragments;
    if(DEBUG)Log.d(TAG,"fragments null: "+ (fragments == null));

    initializeMiniPlayerViews();

    // Bind to MediaPlayerService
    Intent playerIntent = new Intent(mainActivity, MediaPlayerService.class);
    mainActivity.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    // Set up play/pause button click listener
    ImageButton playPauseButton = mainActivity.findViewById(R.id.mini_player_play_pause);
    playPauseButton.setOnClickListener(v -> handlePlayPause());

    // Register activity result launcher
    importPlaylistLauncher = mainActivity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                handleImportPlaylistResult(result.getData());
              }
            }
    );
  }

  // Clean up when activity is destroyed
  public void onDestroy() {
    stopSeekBarUpdate();
    if (serviceBound) {
      mainActivity.unbindService(serviceConnection);
      serviceBound = false;
    }
  }

  public void onResume(){
    updateMiniPlayerState();
  }

  // Call this method when returning from PlaylistActivity
  public void updateMiniPlayerState() {
    if (serviceBound && mediaPlayerService != null) {
      // Check if there's a current song
      Song currentSong = mediaPlayerService.getCurrentSong();

      if (currentSong != null) {
        // Show controls and update song info
        miniPlayerControls.setVisibility(View.VISIBLE);
        miniPlayerTitle.setText(currentSong.getName());

        // Handle artist name
        String artist = currentSong.getArtist();
        if (artist != null && !artist.isEmpty() && !artist.contains("<unknown>")) {
          miniPlayerArtist.setVisibility(View.VISIBLE);
          miniPlayerArtist.setText(artist);
        } else {
          miniPlayerArtist.setVisibility(View.GONE);
        }

        // Update SeekBar
        miniPlayerSeekBar.setMax(currentSong.getDuration_ms());
        miniPlayerSeekBar.setProgress(mediaPlayerService.getCurrentPosition());

        // Start or stop seekbar updates based on playback state
        if (mediaPlayerService.isPlayingMusic()) {
          startSeekBarUpdate();
        } else {
          stopSeekBarUpdate();
        }

        // Update play/pause button
        miniPlayerPlayPause.setImageResource(
                mediaPlayerService.isPlayingMusic() ?
                        R.drawable.ic_pause_white :
                        R.drawable.ic_play_button_white
        );
      } else {
        // No song playing, hide controls
        miniPlayerControls.setVisibility(View.GONE);
      }
    } else {
      // Service not bound, hide controls
      miniPlayerControls.setVisibility(View.GONE);
      miniPlayerControls.setVisibility(View.GONE);
    }
  }

  /** Init options menu and search view.
   *
   * @param menu
   * @return
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    mainActivity.getMenuInflater().inflate(R.menu.options_menu_mainactivity,menu);

    //create searchview
    MenuItem searchItem = menu.findItem(R.id.miSearch);
    mainActivity.searchView = (SearchView) searchItem.getActionView();
    mainActivity.searchView.setOnQueryTextListener(this);

    return true;
  }

  /** Define actions that are taken dependend on selected menu item in menu (these include all options across different fragments).
   *  In all cases there is an action method called in the corresponding fragment.
   *  Breakdown of the actions taken for a selected option:
   *  - load locale audio file: create a dialog and a positive response action (Folderfragment reload Allmusic)
   *  - sort name, artist, duration, genre, reverse: call sort method of fragment
   * @param item
   * @return if menu item exists
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.miSortNameMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch (currentItem) {
          case 0: {
            ((FolderFragment) fragments[0]).sortSongsByName();
            break;
          }
          case 1: {
            ((AudiobookFragment) fragments[1]).sortSongsByName();
            break;
          }
          case 2: {
            ((PlayListFragment) fragments[2]).sortSongsByName();
            break;
          }
          //case 2: {((FavoritesFragment) fragments[2]).sortSongsByName();break;}
          case 3: {
            ((AlbumFragment) fragments[3]).sortSongsByName();
            break;
          }
          case 4: {
            ((ArtistFragment) fragments[4]).sortSongsByName();
            break;
          }
          case 5: {
            ((GenreFragment) fragments[5]).sortSongsByName();
            break;
          }
        }
        break;
      }
      case R.id.miSortArtistMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch (currentItem) {
          case 0: {
            ((FolderFragment) fragments[0]).sortSongsByArtist();
            break;
          }
          case 1: {
            ((AudiobookFragment) fragments[1]).sortSongsByArtist();
            break;
          }
          case 2: {
            ((PlayListFragment) fragments[2]).sortSongsByArtist();
            break;
          }
          case 3: {
            ((AlbumFragment) fragments[3]).sortSongsByArtist();
            break;
          }
          case 4: {
            ((ArtistFragment) fragments[4]).sortSongsByArtist();
            break;
          }
          case 5: {
            ((GenreFragment) fragments[5]).sortSongsByArtist();
            break;
          }
        }
        break;
      }
      case R.id.miSortDurationMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch (currentItem) {
          case 0: {
            ((FolderFragment) fragments[0]).sortSongsByDuration();
            break;
          }
          case 1: {
            ((AudiobookFragment) fragments[1]).sortSongsByDuration();
            break;
          }
          case 2: {
            ((PlayListFragment) fragments[2]).sortSongsByDuration();
            break;
          }
          case 3: {
            ((AlbumFragment) fragments[3]).sortSongsByDuration();
            break;
          }
          case 4: {
            ((ArtistFragment) fragments[4]).sortSongsByDuration();
            break;
          }
          case 5: {
            ((GenreFragment) fragments[5]).sortSongsByDuration();
            break;
          }
        }
        break;
      }
      case R.id.miSortGenreMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch (currentItem) {
          case 0: {
            ((FolderFragment) fragments[0]).sortSongsByGenre();
            break;
          }
          case 1: {
            ((AudiobookFragment) fragments[1]).sortSongsByGenre();
            break;
          }
          case 2: {
            ((PlayListFragment) fragments[2]).sortSongsByGenre();
            break;
          }
          case 3: {
            ((AlbumFragment) fragments[3]).sortSongsByGenre();
            break;
          }
          case 4: {
            ((ArtistFragment) fragments[4]).sortSongsByGenre();
            break;
          }
          case 5: {
            ((GenreFragment) fragments[5]).sortSongsByGenre();
            break;
          }
        }
        break;
      }
      case R.id.miReverseMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch (currentItem) {
          case 0: {
            ((FolderFragment) fragments[0]).reverse();
            break;
          }
          case 1: {
            ((AudiobookFragment) fragments[1]).reverse();
            break;
          }
          case 2: {
            ((PlayListFragment) fragments[2]).reverse();
            break;
          }
          case 3: {
            ((AlbumFragment) fragments[3]).reverse();
            break;
          }
          case 4: {
            ((ArtistFragment) fragments[4]).reverse();
            break;
          }
          case 5: {
            ((GenreFragment) fragments[5]).reverse();
            break;
          }
        }
        break;
      }
      case R.id.action_import_playlists: {
        handleImportPlaylists();
        break;
      }
      case R.id.action_export_playlists: {
        handleExportPlaylists();
        break;
      }
    }
    return true;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  /**
   *
   * @param query
   * @return
   */
  @Override
  public boolean onQueryTextChange(String query) {
    //search in different fragments

    //Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.view_pager_main);
    int currentItem = mainActivity.viewPager.getCurrentItem();
    Log.d("search","in fragment: "+currentItem);
    switch(currentItem){
      case 0: {
        Log.v(TAG, "search the current fragment FolderFragment");
        ((FolderFragment) fragments[0]).searchMusic(query);
        break;
      }
      case 1: {
        Log.v(TAG, "search the current fragment FolderFragment");
        ((AudiobookFragment) fragments[1]).searchMusic(query);
        break;
      }
      case 2: {
        Log.v(TAG, "search the current fragment PlayListFragment");
        ((PlayListFragment) fragments[2]).searchMusic(query);
        break;
      }
      case 3: {
        Log.v(TAG, "search the current fragment AlbumFragment");
        ((AlbumFragment) fragments[3]).searchMusic(query);
        break;
      }
      case 4: {
        Log.v(TAG, "search the current fragment ArtistFragment");
        ((ArtistFragment) fragments[4]).searchMusic(query);
        break;
      }
      case 5: {
        Log.v(TAG, "search the current fragment GenreFragment");
        ((GenreFragment) fragments[5]).searchMusic(query);
        break;
      }
    }
    return false;
  }



  /**
   * @return false if nothing was done => normal onBackPressed behavior
   */
  public boolean onBackPressed() {
    int currentItem = mainActivity.viewPager.getCurrentItem();
    if (isSearchBarOpen()) {
      closeSearchBar();
      return true;
    } else {
      switch(currentItem){
        case 0: {
          return ((FolderFragment) fragments[0]).onBackpressed();
        }
        case 1: {
          return ((AudiobookFragment) fragments[1]).onBackpressed();
        }
        case 2: {
          return ((PlayListFragment) fragments[2]).onBackpressed();
        }
        case 3: {
          return false;
        }
        case 4: {
          return ((ArtistFragment) fragments[4]).onBackpressed();
        }
        case 5: {
          return ((GenreFragment) fragments[5]).onBackpressed();
        }
      }
    }
    return false;
  }

  private void closeSearchBar() {
    if(mainActivity.searchView != null) {
      mainActivity.searchView.setIconified(true);
      mainActivity.searchView.clearFocus();
    }
  }

  private void handlePlayPause() {
    if (serviceBound && mediaPlayerService != null) {
      if (mediaPlayerService.isPlayingMusic()) {
        mediaPlayerService.pause();
      } else {
        mediaPlayerService.resume();
      }
      updatePlayPauseButton();
    }
  }

  private void updatePlayPauseButton() {
    ImageButton playPauseButton = mainActivity.findViewById(R.id.mini_player_play_pause);
    if (serviceBound && mediaPlayerService != null) {
      playPauseButton.setImageResource(
              mediaPlayerService.isPlayingMusic() ?
                      R.drawable.ic_pause_white :
                      R.drawable.ic_play_button_white
      );
    }
  }

  private void initializeMiniPlayerViews() {
    miniPlayerControls = mainActivity.findViewById(R.id.mini_player_controls);
    miniPlayerTitle = mainActivity.findViewById(R.id.mini_player_title);
    miniPlayerArtist = mainActivity.findViewById(R.id.mini_player_artist);
    miniPlayerPlayPause = mainActivity.findViewById(R.id.mini_player_play_pause);
    miniPlayerSeekBar = mainActivity.findViewById(R.id.mini_player_seekbar);

    // Set click listener for play/pause
    miniPlayerPlayPause.setOnClickListener(v -> {
      if (serviceBound && mediaPlayerService != null) {
        if (mediaPlayerService.isPlayingMusic()) {
          mediaPlayerService.pause();
        } else {
          mediaPlayerService.resume();
        }
        updateMiniPlayerState();
      }
    });

    // Click on the LinearLayout (except button) to resume PlaylistActivity
    miniPlayerControls.setOnClickListener(v -> {
      /*if (serviceBound && mediaPlayerService != null) {
        Intent intent = new Intent(mainActivity, PlayListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        // Add required extras based on current playback
        // We need to determine which fragment's songlist is currently playing
        // and add the appropriate extra
        Song currentSong = mediaPlayerService.getCurrentSong();
        if (currentSong != null) {
          if (currentSong.getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS) {
            intent.putExtra(AudiobookFragment.FOLDERAUDIOBOOKINDEXEXTRA, 0);
          } else {
            intent.putExtra(FolderFragment.FOLDERSONGINDEXEXTRA, 0);
          }
        }

        mainActivity.startActivity(intent);
      }

       */
    });


    // Set up SeekBar change listener
    miniPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && serviceBound && mediaPlayerService != null) {
          mediaPlayerService.seekTo(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        // Optional: pause updates while user is dragging
        stopSeekBarUpdate();
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        // Resume updates after user finishes dragging
        if (serviceBound && mediaPlayerService != null &&
                mediaPlayerService.isPlayingMusic()) {
          startSeekBarUpdate();
        }
      }
    });
  }

  private void startSeekBarUpdate() {
    seekBarRunnable = new Runnable() {
      @Override
      public void run() {
        if (serviceBound && mediaPlayerService != null) {
          int currentPosition = mediaPlayerService.getCurrentPosition();
          miniPlayerSeekBar.setProgress(currentPosition);

          // Update every 1000ms (1 second)
          seekBarHandler.postDelayed(this, 1000);
        }
      }
    };
    seekBarHandler.post(seekBarRunnable);
  }

  private void stopSeekBarUpdate() {
    seekBarHandler.removeCallbacks(seekBarRunnable);
  }

  private boolean isSearchBarOpen() {
    return !mainActivity.searchView.isIconified();
  }

  private void handleExportPlaylists() {
    try {
      List<Playlist> playlists = PlaylistUtil.getAllPlaylists(mainActivity);
      playlists = playlists.stream().filter(playlist -> !playlist.getName().equals(RECENTLY_ADDED_PLAYLIST_NAME) && !playlist.getName().equals(RECENTLY_PLAYED_PLAYLIST_NAME)).collect(Collectors.toList());
      PlaylistJsonHandler.exportPlaylists(mainActivity, playlists);
      Toast.makeText(mainActivity, "Playlists exported successfully", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Toast.makeText(mainActivity, "Failed to export playlists", Toast.LENGTH_SHORT).show();
      Log.e(TAG, "Export failed: " + e.getMessage());
    }
  }

  private void handleImportPlaylists() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("application/json");
    intent.addCategory(Intent.CATEGORY_OPENABLE);

    try {
      importPlaylistLauncher.launch(
              Intent.createChooser(intent, "Select playlist file")
      );
    } catch (android.content.ActivityNotFoundException ex) {
      Toast.makeText(mainActivity, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleImportPlaylistResult(Intent data) {
    try {
      Uri uri = data.getData();
      InputStream inputStream = mainActivity.getContentResolver().openInputStream(uri);
      File tempFile = createTempFileFromInputStream(inputStream);

      PlaylistJsonHandler.importPlaylists(mainActivity, tempFile);
      Toast.makeText(mainActivity, "Playlists imported successfully", Toast.LENGTH_SHORT).show();

      // Cleanup temp file
      tempFile.delete();

      // Reload playlist fragment
      if (mainActivity.sectionsPagerAdapter.getFragments()[1] instanceof PlayListFragment) {
        ((PlayListFragment) mainActivity.sectionsPagerAdapter.getFragments()[1])
                .playlistFragmentListener.reloadPlaylistManager();
      }
    } catch (Exception e) {
      Toast.makeText(mainActivity, "Failed to import playlists", Toast.LENGTH_SHORT).show();
      Log.e(TAG, "Import failed: " + e.getMessage());
    }
  }


  private File createTempFileFromInputStream(InputStream inputStream) throws IOException {
    File tempFile = File.createTempFile("playlist_import", ".json", mainActivity.getCacheDir());

    BufferedInputStream bis = new BufferedInputStream(inputStream);
    FileOutputStream fos = new FileOutputStream(tempFile);
    BufferedOutputStream bos = new BufferedOutputStream(fos);

    byte[] buffer = new byte[4096];
    int count;
    while ((count = bis.read(buffer)) != -1) {
      bos.write(buffer, 0, count);
    }

    bos.flush();
    bos.close();
    fos.close();
    bis.close();
    inputStream.close();

    return tempFile;
  }

}
