package com.example.moonstonemusicplayer.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.MainActivityListener;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.VideoModel;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils.YouTubeDownloader;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.OnlineMusicFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.SectionsPagerAdapter;
import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

public class MainActivity extends AppCompatActivity {
  private static int PERMISSION_REQUEST_CODE = 678;

  private static final String TAG = MainActivity.class.getSimpleName();
  public SearchView searchView;
  MainActivityListener mainActivityListener;

  public SectionsPagerAdapter sectionsPagerAdapter;
  public NonSwipeableViewPager viewPager;
  TabLayout tabs;
  public int tabSelected = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

    viewPager = findViewById(R.id.view_pager_main);
    viewPager.setSwipeEnabled(false); // Disable swiping
    viewPager.setAdapter(sectionsPagerAdapter);
    viewPager.setCurrentItem(1);
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
    initYTDL();
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
  }

  public void requestWritePermission() {
    // Permission is not granted, request it
    ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            PERMISSION_REQUEST_CODE);
  }

  public void downloadSong(VideoModel currentVideoModel) {
    if(checkPermissions()){
      YouTubeDownloader youTubeDownloader = new YouTubeDownloader(this);
      youTubeDownloader.downloadVideo(currentVideoModel.getVideoURL(), currentVideoModel.getTitle());
    }
  }

  public void downloadSongTest(){


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

  private void initYTDL(){
    try {
      YoutubeDL.getInstance().init(this);
      YoutubeDL.getInstance().updateYoutubeDL(this, YoutubeDL.UpdateChannel._NIGHTLY);
      FFmpeg.getInstance().init(this);
    } catch (YoutubeDLException e) {
      throw new RuntimeException(e);
    }
  }
}