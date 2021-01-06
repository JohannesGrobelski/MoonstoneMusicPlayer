package com.example.moonstonemusicplayer.controller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainActivityListener implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
  public final String[] EXTERNAL_PERMS = {};
  public final int EXTERNAL_REQUEST = 138;

  private final MainActivity mainActivity;

  private MediaPlayerService mediaPlayerService;
  private ServiceConnection serviceConnection;

  SongManager songManager;
  private int currentSongIndex;

  boolean serviceBound = false;
  String mediaPath = "";

  SongListAdapter songListAdapter;

  Thread seekbarAnimation;

  public MainActivityListener(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    songManager = new SongManager(mainActivity.getBaseContext());
    bindSongListAdapterToSongListView();


    /*
    songManager.addSong(new Song("Lyric Pieces"
          ,"Grieg Kobold"
          , "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg"
          ,86000));
    songManager.addSong(new Song("Grand Duo Concertant for clarinet and piano - 2. Andante con moto"
          ,"Weber"
          ,"https://upload.wikimedia.org/wikipedia/commons/9/9a/Weber_-_Grand_Duo_Concertant_for_clarinet_and_piano_-_2._Andante_con_moto.ogg"
          ,383000));
     */

    songListAdapter.notifyDataSetChanged();
  }

  /** destroys mediaplayerservice and starts new one */
  public void playAudio(){
    if(serviceBound){
      mediaPlayerService.onDestroy();
      serviceBound = false;
    }
    bindMediaPlayerService();
    resumeAudio();
  }

  public void resumeAudio(){
    if(serviceBound){
      mediaPlayerService.resume();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(songManager.getSong(currentSongIndex).getTitle());
      mainActivity.tv_artist.setText(songManager.getSong(currentSongIndex).getArtist());
    }
  }

  public void pauseAudio(){
    if(serviceBound){
      mediaPlayerService.pause();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_play));
      if(seekbarAnimation != null)seekbarAnimation = null;
    }
  }

  private void animateMediaplayerProgressOnSeekbar(){
    final Handler mHandler = new Handler();
    //Make sure you update Seekbar on UI thread
    if(seekbarAnimation != null)seekbarAnimation = null;
    seekbarAnimation = new Thread() {
      @Override
      public void run() {
        //one second has past ... update seekbar and song:lastPositoin
        if(mediaPlayerService.mediaPlayerReady()){
          int mCurrentPosition = mediaPlayerService.getCurrentPosition() / 1000;
          mainActivity.seekBar.setProgress(mCurrentPosition);
          mainActivity.tv_seekbar_progress.setText(Song.getDurationString(mediaPlayerService.getCurrentPosition()));
          songManager.getSong(currentSongIndex).setDuration_ms(mCurrentPosition);
        }
        mHandler.postDelayed(this, 1000);
      }
    };
    mainActivity.runOnUiThread(seekbarAnimation);
  }


  private void bindMediaPlayerService(){
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getService();
        serviceBound = true;
        //set Seekbar
        mainActivity.seekBar.setMax((int) songManager.getSong(currentSongIndex).getDuration_ms()/1000);
        mainActivity.tv_seekbar_max.setText(songManager.getSong(currentSongIndex).getDurationString(
            (int) songManager.getSong(currentSongIndex).getDuration_ms()));
        resumeAudio();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        serviceBound = false;
      }
    };

    Intent playerIntent = new Intent(mainActivity,MediaPlayerService.class);
    playerIntent.putExtra(MediaPlayerService.FILEPATHEXTRA,songManager.getSong(currentSongIndex).getURI());
    mainActivity.startService(playerIntent);
    mainActivity.bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private void bindSongListAdapterToSongListView(){
    songListAdapter = new SongListAdapter(mainActivity,songManager.getSongList(),currentSongIndex);
    mainActivity.lv_songlist.setAdapter(songListAdapter);
  }

  @Override
  /** plays song that was played in list*/
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Toast.makeText(mainActivity,"play audio: "+songManager.getSong(position).getTitle(),Toast.LENGTH_LONG).show();
    currentSongIndex = position;
    playAudio();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btn_prev:
        prevSong();
        break;
      case R.id.btn_play_pause:
        if(serviceBound && !mediaPlayerService.getMediaFilePath().isEmpty()){
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
    }
  }


  private void prevSong(){
    currentSongIndex = (--currentSongIndex);
    if(currentSongIndex == -1)currentSongIndex = songManager.getSongCount()-1;
    playAudio();
  }

  private void nextSong(){
    currentSongIndex = (++currentSongIndex)%songManager.getSongCount();
    playAudio();
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {}
  @Override public void onStopTrackingTouch(SeekBar seekBar) {}
  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(mediaPlayerService.mediaPlayerReady() && fromUser){
      mediaPlayerService.seekTo(progress * 1000);
    }
  }


  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case R.id.mi_loadLocaleAudioFile: {
        if(requestForPermission()){
          songManager.findAllAudioFiles(null);
        }
        break;
      }

    }
    songListAdapter.notifyDataSetChanged();
    return true;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    mainActivity.getMenuInflater().inflate(R.menu.options_menu,menu);
    return true;
  }



  //Permissions

  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    boolean hasStoragePermission;
    int permissionCheck = ContextCompat.checkSelfPermission(
        mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    hasStoragePermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
    if (!hasStoragePermission) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
          Manifest.permission.READ_EXTERNAL_STORAGE)) {
        //showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, 1234);
      } else {
        mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      Toast.makeText(mainActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return hasStoragePermission;
  }

}
