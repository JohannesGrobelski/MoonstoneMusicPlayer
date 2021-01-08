package com.example.moonstonemusicplayer.controller;

import android.os.AsyncTask;

import androidx.loader.content.AsyncTaskLoader;

import com.example.moonstonemusicplayer.model.MusicPlayer;

public class RefreshTask extends AsyncTask<MainActivityListener,Void,Void> {
  MainActivityListener mainActivityListener;

  @Override
  protected Void doInBackground(MainActivityListener... mainActivityListeners) {
    this.mainActivityListener = mainActivityListeners[0];
    this.mainActivityListener.musicPlayer.loadLocalMusic();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.mainActivityListener.finnishRefresh();
  }
}
