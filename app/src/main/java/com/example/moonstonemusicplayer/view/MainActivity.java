package com.example.moonstonemusicplayer.view;

import android.os.Bundle;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.MainActivityListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import androidx.appcompat.widget.Toolbar;
import com.example.moonstonemusicplayer.view.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {
  public SearchView searchView;
  MainActivityListener mainActivityListener;

  public SectionsPagerAdapter sectionsPagerAdapter;
  public ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

    viewPager = findViewById(R.id.view_pager_main);
    viewPager.setAdapter(sectionsPagerAdapter);
    TabLayout tabs = findViewById(R.id.mainactivity_tabs);
    tabs.setupWithViewPager(viewPager);
    FloatingActionButton fab = findViewById(R.id.fab);

    mainActivityListener = new MainActivityListener(this,sectionsPagerAdapter.getFragments());

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
      }
    });
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
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
    super.onBackPressed();
  }


  @Override
  protected void onResume() {
    super.onResume();
    mainActivityListener.onResume();
  }
}