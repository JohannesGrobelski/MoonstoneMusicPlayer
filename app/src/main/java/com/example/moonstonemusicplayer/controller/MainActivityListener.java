package com.example.moonstonemusicplayer.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.MediaPlayerService;

public class MainActivityListener {
  private final MainActivity mainActivity;

  private MediaPlayerService mediaPlayerService;
  boolean serviceBound = false;
  String mediaPath = "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg";

  public MainActivityListener(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }

  //binde den client an den AudioPlayer-Service
  private ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
      mediaPlayerService = binder.getService();
      serviceBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      serviceBound = false;
    }
  };

  public void playAudio(String media){
    //Prüfe ob Service aktiv
    if(!serviceBound){
      Intent playerIntent = new Intent(mainActivity,MediaPlayerService.class);
      playerIntent.putExtra(MediaPlayerService.FILEPATHEXTRA,media);
      mainActivity.startService(playerIntent);
      mainActivity.bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }
  }
}
