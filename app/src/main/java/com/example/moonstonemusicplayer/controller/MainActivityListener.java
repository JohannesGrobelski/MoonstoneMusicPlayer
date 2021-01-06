package com.example.moonstonemusicplayer.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Song;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.MediaPlayerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivityListener implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
  private final MainActivity mainActivity;

  private MediaPlayerService mediaPlayerService;
  private ServiceConnection serviceConnection;

  boolean serviceBound = false;
  String mediaPath = "";

  List<Song> songList = new ArrayList<>();
  int currentSongIndex;
  SongListAdapter songListAdapter;

  public MainActivityListener(MainActivity mainActivity) {
    this.mainActivity = mainActivity;

    bindSongListAdapterToSongListView();

    songList.add(new Song("Lyric Pieces"
        ,"Grieg Kobold"
        , "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg"
        ,86000));
    songList.add(new Song("Grand Duo Concertant for clarinet and piano - 2. Andante con moto"
        ,"Weber"
        ,"https://upload.wikimedia.org/wikipedia/commons/9/9a/Weber_-_Grand_Duo_Concertant_for_clarinet_and_piano_-_2._Andante_con_moto.ogg"
        ,383000));

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
    }
  }

  public void pauseAudio(){
    if(serviceBound){
      mediaPlayerService.pause();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_play));
    }
  }

  private void animateMediaplayerProgressOnSeekbar(){
    final Handler mHandler = new Handler();
    //Make sure you update Seekbar on UI thread
    mainActivity.runOnUiThread(new Runnable() {

    @Override
    public void run() {
        if(mediaPlayerService.mediaPlayerReady()){
          int mCurrentPosition = mediaPlayerService.getCurrentPosition() / 1000;
          mainActivity.seekBar.setProgress(mCurrentPosition);
          mainActivity.tv_seekbar_progress.setText(Song.getDurationString(mediaPlayerService.getCurrentPosition()));
        }
        mHandler.postDelayed(this, 1000);
      }
    });
  }


  private void bindMediaPlayerService(){
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getService();
        serviceBound = true;
        //set Seekbar
        mainActivity.seekBar.setMax((int) songList.get(currentSongIndex).getDuration_ms()/1000);
        mainActivity.tv_seekbar_max.setText(songList.get(currentSongIndex).getDurationString(
            (int) songList.get(currentSongIndex).getDuration_ms()));
        resumeAudio();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        serviceBound = false;
      }
    };

    Intent playerIntent = new Intent(mainActivity,MediaPlayerService.class);
    playerIntent.putExtra(MediaPlayerService.FILEPATHEXTRA,songList.get(currentSongIndex).getURI());
    mainActivity.startService(playerIntent);
    mainActivity.bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
  }

  private void bindSongListAdapterToSongListView(){
    songListAdapter = new SongListAdapter(mainActivity,songList);
    mainActivity.lv_songlist.setAdapter(songListAdapter);
  }

  @Override
  /** plays song that was played in list*/
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Toast.makeText(mainActivity,"play audio: "+songList.get(position).getTitle(),Toast.LENGTH_LONG).show();
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
    if(currentSongIndex == -1)currentSongIndex = songList.size()-1;
    playAudio();
  }

  private void nextSong(){
    currentSongIndex = (++currentSongIndex)%songList.size();
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


}
