package com.example.moonstonemusicplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivityListener;

/** MainActivity
  * Defines the Mainscreen auf the app.
  * Contains all Views and sets Listeners for them.
  * Delegates the creation and management (itemselection) of the optionsmenu to the  {@link com.example.moonstonemusicplayer.controller.MainActivityListener}.
*/
public class MainActivity extends AppCompatActivity {
  MainActivityListener mainActivityListener;
  public ListView lv_songlist;

  public Button btn_prev,btn_play_pause,btn_next,btn_shuffle,btn_repeat;
  public SeekBar seekBar;
  public TextView tv_seekbar_progress,tv_seekbar_max,tv_title,tv_artist;
  private LinearLayout LL_MusicControlls;
  public SearchView searchView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    lv_songlist = findViewById(R.id.lv_songlist);
    btn_prev = findViewById( R.id.btn_prev);
    btn_play_pause = findViewById( R.id.btn_play_pause);
    btn_next = findViewById( R.id.btn_next);
    btn_shuffle = findViewById( R.id.btn_shuffle);
    btn_repeat = findViewById( R.id.btn_repeat);

    seekBar = findViewById(R.id.seekBar);
    tv_seekbar_progress = findViewById(R.id.tv_seekbar_progress);
    tv_seekbar_max = findViewById(R.id.tv_seekbar_max);
    tv_title = findViewById(R.id.tv_title);
    tv_artist = findViewById(R.id.tv_artist);
    LL_MusicControlls = findViewById(R.id.LL_MusicControlls);

    mainActivityListener = new MainActivityListener(this);
    lv_songlist.setOnItemClickListener(mainActivityListener);
    btn_shuffle.setOnClickListener(mainActivityListener);
    btn_prev.setOnClickListener(mainActivityListener);
    btn_play_pause.setOnClickListener(mainActivityListener);
    btn_next.setOnClickListener(mainActivityListener);
    btn_repeat.setOnClickListener(mainActivityListener);
    seekBar.setOnSeekBarChangeListener(mainActivityListener);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    mainActivityListener.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return mainActivityListener.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return mainActivityListener.onOptionsItemSelected(item);
  }





  public void hideMusicControlls(){LL_MusicControlls.setVisibility(View.GONE);}
  public void showMusicControlls(){LL_MusicControlls.setVisibility(View.VISIBLE);}

}