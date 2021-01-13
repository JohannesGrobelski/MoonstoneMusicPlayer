package com.example.moonstonemusicplayer.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ContextMenu;
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
import com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment.FavoriteFragmentListener;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistFragmentListener;
import com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener;
import com.example.moonstonemusicplayer.view.ui.main.FolderFragment;

import static com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment.FavoriteFragmentListener.FAVORITELISTEXTRA;

/** MainActivity
  * Defines the Mainscreen auf the app.
  * Contains all Views and sets Listeners for them.
  * Delegates the creation and management (itemselection) of the optionsmenu to the  {@link PlayListActivityListener}.
*/
public class PlayListActivity extends AppCompatActivity {
  private static final String TAG = PlayListActivity.class.getName();
  private static final boolean DEBUG = true;
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
    btn_prev = findViewById( R.id.notification_btn_prev);
    btn_play_pause = findViewById( R.id.notification_btn_play_pause);
    btn_next = findViewById( R.id.notification_btn_next);
    btn_shuffle = findViewById( R.id.btn_shuffle);
    btn_repeat = findViewById( R.id.btn_repeat);

    seekBar = findViewById(R.id.seekBar);
    tv_seekbar_progress = findViewById(R.id.tv_seekbar_progress);
    tv_seekbar_max = findViewById(R.id.tv_seekbar_max);
    tv_title = findViewById(R.id.notification_tv_name);
    tv_artist = findViewById(R.id.tv_artist);
    LL_MusicControlls = findViewById(R.id.LL_MusicControlls);

    int song_index = 0;
    if(getIntent().hasExtra(FolderFragment.FOLDERSONGINDEXEXTRA)){
      song_index = getIntent().getIntExtra(FolderFragment.FOLDERSONGINDEXEXTRA,0);
      playListActivityListener = new PlayListActivityListener(this,FolderFragment.getFolderSonglist(),song_index);
    }
    else if(getIntent().hasExtra(FAVORITELISTEXTRA)){
      song_index = getIntent().getIntExtra(FAVORITELISTEXTRA,0);
      playListActivityListener = new PlayListActivityListener(this, FavoriteFragmentListener.getFavoriteSonglist(),song_index);
    }
    else if(getIntent().hasExtra(PlaylistFragmentListener.PLAYLISTINDEXEXTRA)){
      song_index = getIntent().getIntExtra(PlaylistFragmentListener.PLAYLISTINDEXEXTRA,0);
      playListActivityListener = new PlayListActivityListener(this,PlaylistFragmentListener.getPlaylistSonglist(),song_index);
    }

    lv_songlist.setOnItemClickListener(playListActivityListener);
    btn_shuffle.setOnClickListener(playListActivityListener);
    btn_prev.setOnClickListener(playListActivityListener);
    btn_play_pause.setOnClickListener(playListActivityListener);
    btn_next.setOnClickListener(playListActivityListener);
    btn_repeat.setOnClickListener(playListActivityListener);
    seekBar.setOnSeekBarChangeListener(playListActivityListener);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
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
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    playListActivityListener.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(@NonNull MenuItem item) {
    return playListActivityListener.onContextItemSelected(item);
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