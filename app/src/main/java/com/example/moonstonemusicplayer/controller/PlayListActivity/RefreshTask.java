package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.os.AsyncTask;

import com.example.moonstonemusicplayer.model.PlayListActivity.PlaylistManager;

public class RefreshTask extends AsyncTask<PlaylistManager,Void,Void> {
  private PlaylistManager PlaylistManager;
  private PlayListActivityListener.RefreshTaskListener refreshTaskListener;

  public RefreshTask(PlayListActivityListener.RefreshTaskListener refreshTaskListener){
    this.refreshTaskListener = refreshTaskListener;
  }

  @Override
  protected Void doInBackground(PlaylistManager... playlistManagers) {
    this.PlaylistManager = playlistManagers[0];
    //TODO: this.MusicManager.loadLocalMusic();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.refreshTaskListener.onCompletion();
  }


}
