package com.example.moonstonemusicplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

/** MainActivity
  * Defines the Mainscreen auf the app.
  * Contains all Views and sets Listeners for them.
  * Delegates the creation and management (itemselection) of the optionsmenu to the  {@link PlayListActivityListener}.
*/
public class PlayListActivity extends AppCompatActivity {
  PlayListActivityListener playListActivityListener;
  public ListView lv_songlist;

  public Button btn_prev,btn_play_pause,btn_next,btn_shuffle,btn_repeat;
  public SeekBar seekBar;
  public TextView tv_seekbar_progress,tv_seekbar_max,tv_title,tv_artist;
  private LinearLayout LL_MusicControlls;
  public SearchView searchView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_playlist);

    lv_songlist = findViewById(R.id.lv_songlist);
    btn_prev = findViewById( R.id.btn_prev);
    btn_play_pause = findViewById( R.id.btn_play_pause);
    btn_next = findViewById( R.id.btn_next);
    btn_shuffle = findViewById( R.id.btn_shuffle);
    btn_repeat = findViewById( R.id.btn_repeat);

    seekBar = findViewById(R.id.seekBar);
    tv_seekbar_progress = findViewById(R.id.tv_seekbar_progress);
    tv_seekbar_max = findViewById(R.id.tv_seekbar_max);
    tv_title = findViewById(R.id.tv_name);
    tv_artist = findViewById(R.id.tv_artist);
    LL_MusicControlls = findViewById(R.id.LL_MusicControlls);

    int song_index = 0;
    if(getIntent().hasExtra(FolderFragment.SONGINDEXEXTRA)){
       song_index = getIntent().getIntExtra(FolderFragment.SONGINDEXEXTRA,0);
    }
    playListActivityListener = new PlayListActivityListener(this,FolderFragment.getFolderSonglist(),song_index);

    lv_songlist.setOnItemClickListener(playListActivityListener);
    btn_shuffle.setOnClickListener(playListActivityListener);
    btn_prev.setOnClickListener(playListActivityListener);
    btn_play_pause.setOnClickListener(playListActivityListener);
    btn_next.setOnClickListener(playListActivityListener);
    btn_repeat.setOnClickListener(playListActivityListener);
    seekBar.setOnSeekBarChangeListener(playListActivityListener);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  public void onBackPressed() {
    playListActivityListener.onBackPressed();
    super.onBackPressed();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    playListActivityListener.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return playListActivityListener.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return playListActivityListener.onOptionsItemSelected(item);
  }


  public void setArtist(String artist){
    if(artist.isEmpty()) tv_artist.setText("unknown artist");
    else tv_artist.setText(artist);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    playListActivityListener.onDestroy();
  }

  public void hideMusicControlls(){LL_MusicControlls.setVisibility(View.GONE);}
  public void showMusicControlls(){LL_MusicControlls.setVisibility(View.VISIBLE);}

}