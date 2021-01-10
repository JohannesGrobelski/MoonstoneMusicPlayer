package com.example.moonstonemusicplayer.controller;

import android.os.AsyncTask;

import androidx.loader.content.AsyncTaskLoader;

import com.example.moonstonemusicplayer.model.MusicPlayer;
import com.example.moonstonemusicplayer.view.MainActivity;

public class RefreshTask extends AsyncTask<MusicPlayer,Void,Void> {
  MusicPlayer musicPlayer;
  MainActivityListener.RefreshTashListener refreshTashListener;

  public RefreshTask(MainActivityListener.RefreshTashListener refreshTashListener){
    this.refreshTashListener = refreshTashListener;
  }

  @Override
  protected Void doInBackground(MusicPlayer... MusicPlayers) {
    this.musicPlayer = MusicPlayers[0];
    this.musicPlayer.loadLocalMusic();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.refreshTashListener.onCompletion();
  }


}
