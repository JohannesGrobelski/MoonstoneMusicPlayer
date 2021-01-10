package com.example.moonstonemusicplayer.controller.MainActivity;

import android.view.Menu;
import android.view.MenuItem;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.view.MainActivity;

public class MainActivityListener {
  private final MainActivity mainActivity;
  FolderManager folderManager;

  public MainActivityListener(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    folderManager = new FolderManager(mainActivity.getBaseContext());
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    mainActivity.getMenuInflater().inflate(R.menu.options_menu_mainactivity,menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case R.id.mi_loadLocaleAudioFile: {
        folderManager.loadLocalMusic();
        folderManager.getRootFolder().print(0);
        break;
      }
      case R.id.miDeleteAllAudioFiles: {

        break;
      }
    }
    //songListAdapter.notifyDataSetChanged();
    return true;
  }
}
