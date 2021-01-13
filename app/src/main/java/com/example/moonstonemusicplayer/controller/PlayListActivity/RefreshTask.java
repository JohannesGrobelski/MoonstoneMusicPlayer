package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.os.AsyncTask;

import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlaylistManager;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

/** loads local music and messages per folderfragment listener*/
public class RefreshTask extends AsyncTask<FolderManager,Void,Void> {
  private FolderManager folderManager;
  private FolderFragment.RefreshTaskListener refreshTaskListener;

  public RefreshTask(FolderFragment.RefreshTaskListener refreshTaskListener){
    this.refreshTaskListener = refreshTaskListener;
  }

  @Override
  protected Void doInBackground(FolderManager... folderManagers) {
    this.folderManager = folderManagers[0];
    this.folderManager.loadLocalMusicAsFolder();
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    this.refreshTaskListener.onCompletion();
  }


}
