package com.example.moonstonemusicplayer.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivityListener;

public class MainActivity extends AppCompatActivity {
  MainActivityListener mainActivityListener;

  ListView listView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    mainActivityListener = new MainActivityListener(this);
  }
}