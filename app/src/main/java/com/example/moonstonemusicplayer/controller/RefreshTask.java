package com.example.moonstonemusicplayer.controller;

import android.os.AsyncTask;

import com.example.moonstonemusicplayer.model.MusicManager;

public class RefreshTask extends AsyncTask<MusicManager,Void,Void> {
  com.example.moonstonemusicplayer.model.MusicManager MusicManager;
  MainActivityListener.RefreshTashListener refreshTashListener;

  public RefreshTask(MainActivityListener.RefreshTashListener refreshTashListener){
    this.refreshTashListener = refreshTashListener;
  }

  @Override
  protected Void doInBackground(MusicManager... MusicManagers) {
    this.MusicManager = MusicManagers[0];
    this.MusicManager.loadLocalMusic();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.refreshTashListener.onCompletion();
  }


}
