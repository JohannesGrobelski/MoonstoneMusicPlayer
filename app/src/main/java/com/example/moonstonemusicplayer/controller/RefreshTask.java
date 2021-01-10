package com.example.moonstonemusicplayer.controller;

import android.os.AsyncTask;

import com.example.moonstonemusicplayer.model.MusicManager;

public class RefreshTask extends AsyncTask<MusicManager,Void,Void> {
  private com.example.moonstonemusicplayer.model.MusicManager MusicManager;
  private PlayListActivityListener.RefreshTaskListener refreshTaskListener;

  public RefreshTask(PlayListActivityListener.RefreshTaskListener refreshTaskListener){
    this.refreshTaskListener = refreshTaskListener;
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
    this.refreshTaskListener.onCompletion();
  }


}
