/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;
import com.woxthebox.draglistview.DragListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.NextSongToPlayUtility;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlaylistManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** MainActivityListener
 *  Handles input from the User (through Views in {@link PlayListActivity}),
 *  changes the model {@link PlaylistManager}) according to the input and
 *  and, if necessary, sends messages to the {@link PlaylistManager}).
 */
public class PlayListActivityListener
    implements AdapterView.OnItemClickListener, View.OnClickListener,
    SeekBar.OnSeekBarChangeListener, SearchView.OnQueryTextListener,
    SearchView.OnCloseListener {
  private static final boolean DEBUG = true;
  private static final String TAG = PlayListActivityListener.class.getSimpleName();

  private static final int SINGLE_TAP_TIMEOUT = 400; // Custom timeout for single tap in milliseconds


  public final PlayListActivity playListActivity;

  PlaylistManager playlistManager;

  private ServiceConnection serviceConnection;
  private MediaPlayerService mediaPlayerService;
  boolean isServiceBound = false;

  private String playlistName;

  private SongListAdapter songListAdapter;
  private Thread seekbarAnimationThread;

  @SuppressLint("ClickableViewAccessibility")
  public PlayListActivityListener(PlayListActivity playListActivity, File[] playlist, int starting_song_index, String playlist_name) {
    if(DEBUG)Log.d(TAG,"selected song: "+playlist[starting_song_index].getName());
    this.playListActivity = playListActivity;
    this.playlistName = playlist_name;
    playlistManager = new PlaylistManager(playListActivity.getBaseContext(),playlist);
    List<Object> songList = new ArrayList<>();
    songList.addAll(playlistManager.getPlayList());
    bindSongListAdapterToSongListView(songList);
    songListAdapter.setSelectedSongPath(playlist[starting_song_index].getPath());
    destroyAndCreateNewService(starting_song_index);
    playListActivity.btn_prev.setOnTouchListener(new OnTouchListener() {
        private GestureDetector gestureDetector = new GestureDetector(playListActivity, new GestureDetector.SimpleOnGestureListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mediaPlayerService.jumpXSecondsBackward(10);
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
              prevSong();
              return super.onSingleTapUp(e);
            }
        });

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
    });
    playListActivity.btn_next.setOnTouchListener(new OnTouchListener() {
        private GestureDetector gestureDetector = new GestureDetector(playListActivity, new GestureDetector.SimpleOnGestureListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mediaPlayerService.jumpXSecondsForward(10);
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
              nextSong();
              return super.onSingleTapUp(e);
            }
        });

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }
    });


  }


  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      /*
      case R.id.miSortTitle: {
        playlistManager.sortByTitle();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortArtist: {
        playlistManager.sortByArtist();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortGenre: {
        playlistManager.sortByGenre();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortDuration: {
        playlistManager.sortByDuration();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSwitchAscDesc: {
        playlistManager.reverseList();
        songListAdapter.notifyDataSetChanged();
        break;
      }
       */
    }
    songListAdapter.notifyDataSetChanged();
    return true;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    playListActivity.getMenuInflater().inflate(R.menu.options_menu_playlistactivity,menu);

    //create searchview
    MenuItem searchItem = menu.findItem(R.id.miSearch);
    playListActivity.searchView = (SearchView) searchItem.getActionView();
    playListActivity.searchView.setOnQueryTextListener(this);
    playListActivity.searchView.setOnSearchClickListener(this);
    playListActivity.searchView.setOnCloseListener(this);
    return true;
  }


  @Override
  /** plays song that was clicked by user in songlistView*/
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(isServiceBound){
      playSong(playlistManager.getDisplayedSongList().get(position));
    }

    //destroy searchview and show music controlls, close searchview and close virtual keyboard
    /*
    playListActivity.searchView.setIconified(true);
    playListActivity.searchView.clearFocus();
     */
    playListActivity.showMusicControlls();
  }

  @Override
  /** implements music controll: play/pause, next/prev*/
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btn_prev:
        prevSong();
        break;
      case R.id.btn_play_pause:
        if(isServiceBound){
          if (mediaPlayerService.isPlayingMusic()) {
            pauseAudio();
          } else {
            resumeAudio();
          }
        }
        break;
      case R.id.btn_next:
        nextSong();
        break;
      case R.id.btn_shuffle:{
        toogleShuffleMode();
        break;
      }
      case R.id.btn_repeat: {
        nextRepeatMode();
        break;
      }
      case R.id.miSearch: {
        playListActivity.hideMusicControlls();
        break;
      }
    }
  }

  @Override
  /** called if query in search view is submitted*/
  public boolean onQueryTextSubmit(String query) { return false; }

  @Override
  /** called if input in search view changes => search songs in db according to input*/
  public boolean onQueryTextChange(String query) {
    playlistManager.searchSong(query);
    songListAdapter.notifyDataSetChanged();
    return false;
  }

  @Override
  /** called when search view is closed => open music controlls, reset filter query*/
  public boolean onClose() {
    playListActivity.showMusicControlls();
    return false;
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {}
  @Override public void onStopTrackingTouch(SeekBar seekBar) {}
  @Override
  /** implements the music controll (seekto): seeks to position in mediaPlayerService*/
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(isServiceBound){
      if(mediaPlayerService.mediaPlayerNotNull() && fromUser){
        seekTo(progress * 1000);
      }
    }
  }

  public void onItemClick(int position) {
    File songFile = playlistManager.getDisplayedSongList().get(position);
    if(isServiceBound){
      playSong(songFile);
    }

    playListActivity.showMusicControlls();
    songListAdapter.setSelectedSongPath(songFile.getAbsolutePath());
    songListAdapter.notifyDataSetChanged();
  }

  //messages from mediaPlayerService

  /** media player error => set back UI */
  private void mediaPlayerServiceError(int cause){
    playListActivity.seekBar.setProgress(0);
    playListActivity.tv_seekbar_progress.setText("0:00");
    playListActivity.btn_play_pause.setBackground(playListActivity.getResources().getDrawable(R.drawable.ic_play_button));
    Toast.makeText(playListActivity,"error: "+cause,Toast.LENGTH_LONG).show();
    //TODO: react to cause and display message
  }

  /** media player: audioFocusChange => change UI */
  private void audioFocusChange(int state){
    if(state == AudioManager.AUDIOFOCUS_GAIN){
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_pause,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      animateMediaplayerProgressOnSeekbar();
    } else if(state == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || state == AudioManager.AUDIOFOCUS_LOSS){
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_play_button,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      if(seekbarAnimationThread != null) seekbarAnimationThread = null;
    }
  }

  /** song finnished => set back UI and autoplay */
  private void finishSong(PlayListModel.REPEATMODE repeatmode){
    playListActivity.seekBar.setProgress(0);
    playListActivity.tv_seekbar_progress.setText("0:00");
    if(DEBUG)Log.d(TAG,"finishSong: "+repeatmode);

    if(repeatmode.equals(PlayListModel.REPEATMODE.NONE)){
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_play_button,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
    } else {
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_pause,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getName());
      playListActivity.tv_artist.setText(mediaPlayerService.getCurrentSong().getArtist());
      playListActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      playListActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
    }
    songListAdapter.setSelectedSongPath(mediaPlayerService.getCurrentSong().getPath());
    songListAdapter.notifyDataSetChanged();
  }

  //messages to mediaPlayerService


  private void playSong(File song){
    if(isServiceBound){
      mediaPlayerService.playSong(song);
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_pause,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getName());
      playListActivity.tv_artist.setText(mediaPlayerService.getCurrentSong().getArtist());
      playListActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());

      int max = (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000);
      playListActivity.seekBar.setMax(max);

      //View view = playListActivity.dlv_songlist.getChildAt(mediaPlayerService.getCurrentPosition());
      // if(view != null)view.setBackgroundColor(playListActivity.getResources().getColor(android.R.color.darker_gray));
    }
  }

  private void prevSong(){
    if(isServiceBound){
      mediaPlayerService.prevSong();
      playListActivity.btn_play_pause.setBackground(playListActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getName());
      playListActivity.tv_artist.setText(mediaPlayerService.getCurrentSong().getArtist());
      playListActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      playListActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
      songListAdapter.setSelectedSongPath(mediaPlayerService.getCurrentSong().getPath());
      songListAdapter.notifyDataSetChanged();
    }
  }

  private void nextSong(){
    if(isServiceBound){
      mediaPlayerService.nextSong();
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_pause,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getName());
      playListActivity.tv_artist.setText(mediaPlayerService.getCurrentSong().getArtist());
      playListActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      playListActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
      songListAdapter.setSelectedSongPath(mediaPlayerService.getCurrentSong().getPath());
      songListAdapter.notifyDataSetChanged();
    }
  }

  private void toogleShuffleMode(){
    if(isServiceBound){
      boolean shuffleMode = mediaPlayerService.toogleShuffleMode();
      Log.d(TAG,"toogleShuffle: "+shuffleMode);
      if(shuffleMode){
        playListActivity.btn_shuffle.setBackground(
                DrawableUtils.getTintedDrawable(
                        playListActivity,
                        R.drawable.ic_shuffle,
                        SettingsFragment.getPrimaryColor(playListActivity)
                )
        );
      } else {
        playListActivity.btn_shuffle.setBackground(
                DrawableUtils.getTintedDrawable(
                        playListActivity,
                        R.drawable.ic_shuffle,
                        playListActivity.getResources().getColor(android.R.color.darker_gray)
                )
        );
      }
    }
  }

  private void nextRepeatMode(){
    if(isServiceBound){
      PlayListModel.REPEATMODE repeatmode = mediaPlayerService.nextRepeatMode();
      playListActivity.btn_repeat.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_replay,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      playListActivity.btn_repeat.setText("");
      switch (repeatmode){
        case NONE: {
          playListActivity.btn_repeat.setBackground(
                  DrawableUtils.getTintedDrawable(
                          playListActivity,
                          R.drawable.ic_replay,
                          playListActivity.getResources().getColor(android.R.color.darker_gray)
                  )
          );
          break;
        }
        case ONESONG: {
          playListActivity.btn_repeat.setText("   1");
          playListActivity.btn_repeat.setTextColor(SettingsFragment.getPrimaryColor(playListActivity));
          break;
        }
      }
    }
  }

  /** implements the music controll (resume): resumes audio in mediaPlayerService*/
  private void resumeAudio(){
    if(isServiceBound){
      mediaPlayerService.resume();
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_pause,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getName());
      playListActivity.tv_artist.setText(mediaPlayerService.getCurrentSong().getArtist());
      playListActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
    }
  }

  /** implements the music controll (pause): pauses audio in mediaPlayerService*/
  private void pauseAudio(){
    if(isServiceBound){
      mediaPlayerService.pause();
      playListActivity.btn_play_pause.setBackground(
              DrawableUtils.getTintedDrawable(
                      playListActivity,
                      R.drawable.ic_play_button,
                      SettingsFragment.getPrimaryColor(playListActivity)
              )
      );
      if(seekbarAnimationThread != null) seekbarAnimationThread = null;
    }
  }

  private void seekTo(int seekPosition){
    Log.d(TAG,"seekTo: "+seekPosition);
    if(isServiceBound){
      mediaPlayerService.seekTo(seekPosition);
      animateMediaplayerProgressOnSeekbar();
      playListActivity.tv_seekbar_progress.setText(Song.getDurationString(seekPosition));
    }
  }

  /** destroys mediaplayerservice and starts new one */
  private void destroyAndCreateNewService(int starting_index){
    //if an service is bound destroy it ...
    if(isServiceBound){
      if(DEBUG)Log.d(TAG,"destroyMediaPlayerService (bound: "+isServiceBound+")");
      mediaPlayerService.onDestroy();
      isServiceBound = false;
    }

    // ... and start a new one.
    serviceConnection = createServiceConnection();
    Intent playerIntent = new Intent(playListActivity,MediaPlayerService.class);
    if(starting_index != -1){
      playerIntent.putExtra(MediaPlayerService.STARTING_INDEX,starting_index);
    }

    //start service
    playListActivity.startService(playerIntent);
    boolean bindService = playListActivity.getApplicationContext().
        bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    if(DEBUG)Log.d(TAG,"startMediaPlayerService: "+bindService);
  }



  /** create a serviceConnection for musicPlayerService and bind the service, prepare play*/
  private ServiceConnection createServiceConnection(){
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        if(DEBUG)Log.d(TAG,"onServiceConnected");
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getService();
        binder.setListener(new BoundServiceListener() {
          @Override public void onError(int cause) { mediaPlayerServiceError(cause);}

          @Override
          public void selectedSong(String selectedSongPath) {
            if(DEBUG)Log.d(TAG,"song playing: "+selectedSongPath);
            songListAdapter.setSelectedSongPath(selectedSongPath);
            songListAdapter.notifyDataSetChanged(); //NOTE: this is necessary
          }

          @Override public void finishedSong(PlayListModel.REPEATMODE repeatmode){
            finishSong(repeatmode);
          }
          @Override public void onAudioFocusChange(int state) { audioFocusChange(state);}


          @Override
          public void transferPlayListFromActivityToService() {
            //transfer playlist from MusicManager to MediaPlayerService; problem: limit of 1MB of data in a Bundle
            //solution: save data in static variable and get it later in service

            if(DEBUG)Log.d(TAG,"startMediaPlayerService create Playlist: "+ playlistManager.getPlayList().size());
            if(DEBUG)Log.d(TAG,"startMediaPlayerService transfer Playlist: "+ playlistManager.getPlayList().size());
            mediaPlayerService.setPlayList(playlistManager.getPlayList());
          }

          @Override
          public void stopSong() {
            playListActivity.seekBar.setProgress(0);
            playListActivity.btn_play_pause.setBackground(
                    DrawableUtils.getTintedDrawable(
                            playListActivity,
                            R.drawable.ic_play_button,
                            SettingsFragment.getPrimaryColor(playListActivity)
                    )
            );
          }

          @Override
          public void resumeSong() {
            playListActivity.btn_play_pause.setBackground(
                    DrawableUtils.getTintedDrawable(
                            playListActivity,
                            R.drawable.ic_pause,
                            SettingsFragment.getPrimaryColor(playListActivity)
                    )
            );
          }

          @Override
          public void pauseSong() {
            playListActivity.btn_play_pause.setBackground(
                    DrawableUtils.getTintedDrawable(
                            playListActivity,
                            R.drawable.ic_play_button,
                            SettingsFragment.getPrimaryColor(playListActivity)
                    )
            );
          }
        });
        Log.d(TAG,"onServiceConnected: binder: "+ (binder == null));
        isServiceBound = true;
        //transfer data
        if(DEBUG)Log.d(TAG,"startMediaPlayerService transfer Playlist: "+ playlistManager.getPlayList().size());
        mediaPlayerService.setPlayList(playlistManager.getPlayList());
        playSong(mediaPlayerService.getCurrentSongFile());
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        isServiceBound = false;
      }
    };
  }

  /** animate the progress seek bar through a thread changes seekbar progress every second*/
  private void animateMediaplayerProgressOnSeekbar(){
    final Handler mHandler = new Handler();
    //Make sure you update Seekbar on UI thread
    seekbarAnimationThread = null;
    seekbarAnimationThread = new Thread() {
      @Override
      public void run() {
        //one second has past ... update seekbar and song:lastPositoin
        if(mediaPlayerService.mediaPlayerReady()){
          int mCurrentPosition = mediaPlayerService.getCurrentPosition() / 1000;
          playListActivity.seekBar.setProgress(mCurrentPosition);
          playListActivity.tv_seekbar_progress.setText(Song.getDurationString(mediaPlayerService.getCurrentPosition()));
        }
        mHandler.postDelayed(this, 1000);
      }
    };
    playListActivity.runOnUiThread(seekbarAnimationThread);
  }


  /** bind songlistview to songlistadapter using the songList of musicplayer*/
  private void bindSongListAdapterToSongListView(List<Object> itemList){
    // Ensure this runs on the main thread
    playListActivity.runOnUiThread(() -> {
      songListAdapter = new SongListAdapter(this, itemList);

      // Set up drag listener if needed
      playListActivity.dlv_songlist.setDragListListener(new DragListView.DragListListener() {
        @Override
        public void onItemDragStarted(int position) {
          // Handle drag start#
        }

        @Override
        public void onItemDragging(int itemPosition, float x, float y) {
          // Handle item dragging
        }

        @Override
        public void onItemDragEnded(int fromPosition, int toPosition) {
          // Handle drag end - update your data model here if needed
          if (fromPosition != toPosition) {
            Playlist updatedPlaylist = new Playlist(playlistName, songListAdapter.getItemList().stream().map(i -> BrowserManager.getSongFromAudioFile(((File) i))).collect(Collectors.toList()));

            //update playlist in mediaservice
            mediaPlayerService.updatePlaylist(updatedPlaylist);

            //update playlist in playlistmanager
            playlistManager.updatePlaylist(updatedPlaylist);

            //NOTE: drag list view does already manipulate the data list (do not change playlistListAdapter.getItemList())!!!
            if(!playlistName.isEmpty()){
              DBPlaylists dbPlaylists = DBPlaylists.getInstance(playListActivity);
              dbPlaylists.changePlaylistOrder(updatedPlaylist.getName(), songListAdapter.getItemList().stream().map(f -> BrowserManager.getSongFromAudioFile((File) f)).collect(Collectors.toList()));
            }
          }
        }
      });

      playListActivity.dlv_songlist.setLayoutManager(new LinearLayoutManager(playListActivity));

      playListActivity.dlv_songlist.setAdapter(songListAdapter, true);
      playListActivity.dlv_songlist.setCanDragHorizontally(false);
    });

  }

  //Permissions
  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    int permissionCheck = ContextCompat.checkSelfPermission(playListActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(playListActivity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
      } else {
        playListActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      Toast.makeText(playListActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  public void onConfigurationChanged(Configuration newConfig) {

  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    menu.add(0, 1, 0, "add to favorites");
    menu.add(0, 2, 0, "add to playlist");
    menu.add(0, 3, 0, "play as next song");
  }

  public boolean onContextItemSelected(MenuItem item) {
    //only react to context menu in this fragment
    if(item.getGroupId() == 0){
      //calculate the index of the song clicked
      File selectedSongFile = playlistManager.getDisplayedSongList().get(songListAdapter.getLastLongClickedPosition());
      Song selectedSong = BrowserManager.getSongFromAudioFile(selectedSongFile);
      switch (item.getItemId()){
        case 1: {
          DBPlaylists.getInstance(playListActivity).addToFavorites(playListActivity,selectedSong);
          break;
        }
        case 2:  {
          showAlertDialogAddToPlaylists(selectedSong);
          break;
        }
        case 3: {
          NextSongToPlayUtility.setSongToPlayNext(selectedSong);
          break;
        }
      }
    }
    return true;
  }

  private void showAlertDialogAddToPlaylists(final Song song){
    final String[] allPlaylistNames = DBPlaylists.getInstance(playListActivity).getAllPlaylistNames();

    LayoutInflater inflater = playListActivity.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.add_to_playlist_layout, null);
    ListView lv_playlist_alert = dialogView.findViewById(R.id.lv_playlists_alert);
    final EditText et_addNewPlaylist = dialogView.findViewById(R.id.et_addNewPlaylist);

    lv_playlist_alert.setAdapter(new ArrayAdapter<String>(playListActivity,android.R.layout.simple_list_item_1,allPlaylistNames));

    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(playListActivity);
    dialogBuilder.setView(dialogView);
    dialogBuilder.setNegativeButton(android.R.string.no,null);
    dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String text = et_addNewPlaylist.getText().toString();
        if(!text.isEmpty()){
          DBPlaylists.getInstance(playListActivity).addToPlaylist(playListActivity,song,text);
        }
      }
    });
    dialogBuilder.setTitle("Add Song to a playlist:");

    final AlertDialog alertDialog  = dialogBuilder.show();

    lv_playlist_alert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DBPlaylists.getInstance(playListActivity).addToPlaylist(playListActivity,song,allPlaylistNames[position]);
        alertDialog.dismiss();
      }
    });
  }

  public void onDestroy() {
    if(isServiceBound){
      //mediaPlayerService.onDestroy();
      isServiceBound = false;
    }
  }

  /** stop service */
  public void onBackPressed() {
    //mediaPlayerService.onDestroy();
  }

  public void dispatchMediaButtonEvent(KeyEvent event) {
    if(mediaPlayerService.isPlayingMusic()){
      pauseAudio();
    } else {
      resumeAudio();
    }
  }


  /** interface used to send messages from service to activity*/
  public interface BoundServiceListener {

    void onError(int cause);
    void selectedSong(String selectedSongUri);
    void finishedSong(PlayListModel.REPEATMODE repeatmode);
    void onAudioFocusChange(int state);
    void transferPlayListFromActivityToService();
    void stopSong();
    void resumeSong();
    void pauseSong();
  }


}
