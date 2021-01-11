package com.example.moonstonemusicplayer.controller.MainActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

public class MainActivityListener {
  private static final boolean DEBUG = true;
  private static final String TAG = MainActivityListener.class.getSimpleName();

  private final MainActivity mainActivity;
  private Fragment[] fragments;


  public MainActivityListener(MainActivity mainActivity,Fragment[] fragments) {
    this.mainActivity = mainActivity;
    this.fragments = fragments;
    if(DEBUG)Log.d(TAG,"fragments null: "+String.valueOf(fragments==null));

    requestForPermission();
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    mainActivity.getMenuInflater().inflate(R.menu.options_menu_mainactivity,menu);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case R.id.mi_loadLocaleAudioFile: {
        if(fragments != null && fragments[1] != null){
          ((FolderFragment) fragments[1]).loadMusicNew();
        } else {
          Log.e(TAG,"fragment null");
        }
        //folderManager.getRootFolder().print(0);
        break;
      }
      case R.id.miDeleteAllAudioFiles: {
        ((FolderFragment) fragments[1]).loadMusicNew();
        break;
      }
    }
    //songListAdapter.notifyDataSetChanged();
    return true;
  }

  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    int permissionCheck = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
      } else {
        mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      //Toast.makeText(mainActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }
}
