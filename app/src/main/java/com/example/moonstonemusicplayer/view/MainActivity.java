package com.example.moonstonemusicplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivityListener;

public class MainActivity extends AppCompatActivity {
  MainActivityListener mainActivityListener;

  public ListView lv_songlist;

  public Button btn_prev,btn_play_pause,btn_next;
  public SeekBar seekBar;
  public TextView tv_seekbar_progress,tv_seekbar_max,tv_title,tv_artist;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    lv_songlist = findViewById(R.id.lv_songlist);
    btn_prev = findViewById( R.id.btn_prev);
    btn_play_pause = findViewById( R.id.btn_play_pause);
    btn_next = findViewById( R.id.btn_next);
    seekBar = findViewById(R.id.seekBar);
    tv_seekbar_progress = findViewById(R.id.tv_seekbar_progress);
    tv_seekbar_max = findViewById(R.id.tv_seekbar_max);
    tv_title = findViewById(R.id.tv_title);
    tv_artist = findViewById(R.id.tv_artist);

    mainActivityListener = new MainActivityListener(this);
    lv_songlist.setOnItemClickListener(mainActivityListener);
    btn_prev.setOnClickListener(mainActivityListener);
    btn_play_pause.setOnClickListener(mainActivityListener);
    btn_next.setOnClickListener(mainActivityListener);
    seekBar.setOnSeekBarChangeListener(mainActivityListener);


  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return mainActivityListener.onCreateOptionsMenu(menu);

  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return mainActivityListener.onOptionsItemSelected(item);
  }
}