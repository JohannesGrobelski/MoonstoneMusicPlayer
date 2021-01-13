package com.example.moonstonemusicplayer.controller.MainActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;
import com.google.android.material.tabs.TabLayout;

public class MainActivityListener implements SearchView.OnQueryTextListener,
    View.OnClickListener, SearchView.OnCloseListener, TabLayout.OnTabSelectedListener {
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

    //create searchview
    MenuItem searchItem = menu.findItem(R.id.miSearch);
    mainActivity.searchView = (SearchView) searchItem.getActionView();
    mainActivity.searchView.setOnQueryTextListener(this);
    mainActivity.searchView.setOnSearchClickListener(this);
    mainActivity.searchView.setOnCloseListener(this);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mi_loadLocaleAudioFile: {
        if (fragments != null && fragments[1] != null) {
          requestForPermission();
          //show alert dialog before refreshing audio files
          AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
              .setTitle("Reloads local audio files.")
              .setMessage("This can take a few minutes.")
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  ((FolderFragment) fragments[0]).reloadAllMusic();
                }
              })
              .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  //abort
                }
              })
          .create();
          alertDialog.show();
          break;
        } else {
          Log.e(TAG, "fragment null");
        }
        //folderManager.getRootFolder().print(0);
        break;
      }
      }
          return true;
  }
    //songListAdapter.notifyDataSetChanged();


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

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String query) {
    //search in folderfragment
    ((FolderFragment) fragments[0]).searchMusic(query);
    return false;
  }

  @Override
  public void onClick(View v) {

  }

  @Override
  public boolean onClose() {
    return false;
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    Log.e(TAG,"selected Tab: "+tab.getPosition());
    switch (tab.getPosition()) {
      case 0:
        Log.e(TAG,"TAB1");
        break;
      case 1:
        Log.e(TAG,"TAB2");
        break;
      case 2:
        Log.e(TAG,"TAB3");
        break;
    }
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {}

  @Override
  public void onTabReselected(TabLayout.Tab tab) {
    Log.e(TAG,"selected Tab: "+tab.getPosition());
    switch (tab.getPosition()) {
      case 0:
        Log.e(TAG,"TAB1");
        break;
      case 1:
        Log.e(TAG,"TAB2");
        break;
      case 2:
        Log.e(TAG,"TAB3");
        break;
    }
  }
}
