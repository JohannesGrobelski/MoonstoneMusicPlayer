/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.MainActivityListener;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.moonstonemusicplayer.view.mainactivity_fragments.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {
  private final int PERMISSION_REQUEST_CODE = 678;
  private final int PERMISSION_REQUEST_MEDIA_AUDIO = 679;
  private static final int IMPORT_PLAYLIST_REQUEST_CODE = 680;
  public static final String SONG_DIRECT_EXTRA = "SONG_DIRECT_EXTRA";


  private static final String TAG = MainActivity.class.getSimpleName();
  public SearchView searchView;
  MainActivityListener mainActivityListener;

  public SectionsPagerAdapter sectionsPagerAdapter;
  public NonSwipeableViewPager viewPager;
  TabLayout tabs;
  public int tabSelected = -1;

  private ActivityResultLauncher<IntentSenderRequest> deletetionIntentSenderLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

    viewPager = findViewById(R.id.view_pager_main);
    viewPager.setSwipeEnabled(false); // Disable swiping
    viewPager.setAdapter(sectionsPagerAdapter);
    viewPager.setCurrentItem(0);
    tabs = findViewById(R.id.mainactivity_tabs);
    tabs.setupWithViewPager(viewPager);
    FloatingActionButton fab = findViewById(R.id.fab);


    mainActivityListener = new MainActivityListener(this,sectionsPagerAdapter.getFragments());

    tabs.addOnTabSelectedListener(
        new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            super.onTabSelected(tab);
            tabSelected = tab.getPosition();
            if(tabSelected == 1){//playlist fragment
              if(sectionsPagerAdapter.getFragments()[1] != null
                  && sectionsPagerAdapter.getFragments()[1] instanceof PlayListFragment){
                    ((PlayListFragment) sectionsPagerAdapter.getFragments()[1])
                      .playlistFragmentListener.reloadPlaylistManager();
              }
            }
          }
        });

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // Handle the intent when the activity is created
    handleIncomingIntent(getIntent());

    showMediaAudioPermission();

    PlayListFragment.preloadPlaylistManager(this);

    deletetionIntentSenderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                // Handle the successful deletion
                int position = viewPager.getCurrentItem();
                switch (position){
                  case 0: {
                    ((FolderFragment) sectionsPagerAdapter.getFragments()[position]).refreshFolderList();
                  }
                }
              } else {
                // Handle the cancellation or failure
              }
            });
  }

  @Override
  public void onDestroy(){
      super.onDestroy();
      mainActivityListener.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mainActivityListener.onResume();
  }



  public void showMediaAudioPermission() {
    int permissionCheck = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_MEDIA_AUDIO);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this,
              Manifest.permission.READ_MEDIA_AUDIO)) {
        showMediaLocationExplanation("Permission Needed", "Rationale", Manifest.permission.READ_MEDIA_AUDIO, PERMISSION_REQUEST_MEDIA_AUDIO);
      } else {
        requestPermission(Manifest.permission.READ_MEDIA_AUDIO, PERMISSION_REQUEST_MEDIA_AUDIO);
      }
    } else {
      Log.d(TAG,"READ_MEDIA_AUDIO Permission (already) Granted!");
    }
  }

  private void requestPermission(String permissionName, int permissionRequestCode) {
    ActivityCompat.requestPermissions(this,
            new String[]{permissionName}, permissionRequestCode);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    Log.d("MainActivity","onCreateOptionsMenu");
    return mainActivityListener.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return mainActivityListener.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if(!mainActivityListener.onBackPressed()){
      super.onBackPressed();
    }
  }
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Permission Granted - Retry", Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
      }
    }
    if (requestCode == PERMISSION_REQUEST_MEDIA_AUDIO) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Permission Granted - Retry", Toast.LENGTH_LONG).show();
        reloadBrowserFragment();
      } else {
        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
      }
    }
  }

  public ActivityResultLauncher<IntentSenderRequest> getDeletetionIntentSenderLauncher() {
    return deletetionIntentSenderLauncher;
  }

  public void requestWritePermission() {
    // Permission is not granted, request it
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            PERMISSION_REQUEST_CODE);
  }

  public boolean checkPermissions() {
    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this,"Permission is granted", Toast.LENGTH_LONG).show();
      return true;
    } else {
      Toast.makeText(this,"Permission is revoked", Toast.LENGTH_LONG).show();
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
      return false;
    }
  }

  private void showCustomMediaLocationPermissionDialog() {
    new AlertDialog.Builder(this)
            .setTitle("Media Location Access")
            .setMessage("This app needs access to media location to function properly (please allow the audio permission under app permissions).")
            .setPositiveButton("OK", (dialog, which) -> {
              finish();
            })
            .show();
  }

  private void showMediaLocationExplanation(String title,
                               String message,
                               final String permission,
                               final int permissionRequestCode) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                requestPermission(permission, permissionRequestCode);
              }
            });
    builder.create().show();
  }

  private void reloadBrowserFragment() {
    Fragment browserFragment = null;
    browserFragment = getSupportFragmentManager().findFragmentByTag(sectionsPagerAdapter.getFragments()[1].getTag());
    final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.detach(browserFragment);
    ft.attach(browserFragment);
    ft.commit();
  }

  private void handleIncomingIntent(android.content.Intent intent) {
    if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
      Uri audioUri = intent.getData(); // Get the URI of the selected file

      String filePath = getRealPathFromURI(this, audioUri);
      if (filePath != null) {
        // Launch PlayListActivity with the selected file
        Toast.makeText(this, "Incoming: "+filePath, Toast.LENGTH_LONG).show();
        Intent intentPlaylistActivity = new Intent(this, PlayListActivityListener.class);
        intentPlaylistActivity.putExtra(SONG_DIRECT_EXTRA,filePath);
        this.startActivity(intentPlaylistActivity);
      } else {
        Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private String getRealPathFromURI(Context context, Uri uri) {
    Cursor cursor = null;
    try {
      String[] proj = {MediaStore.MediaColumns.DATA};
      cursor = context.getContentResolver().query(uri, proj, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        return cursor.getString(column_index);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

}
