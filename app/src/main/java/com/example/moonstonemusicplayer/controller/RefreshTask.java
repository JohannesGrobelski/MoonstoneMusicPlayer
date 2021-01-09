package com.example.moonstonemusicplayer.controller;

import android.os.AsyncTask;

public class RefreshTask extends AsyncTask<MainActivityListener,Void,Void> {
  MainActivityListener mainActivityListener;

  @Override
  protected Void doInBackground(MainActivityListener... mainActivityListeners) {
    this.mainActivityListener = mainActivityListeners[0];
    this.mainActivityListener.musicManager.loadLocalMusic(null); //TODO:
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.mainActivityListener.finnishRefresh();
  }
}
