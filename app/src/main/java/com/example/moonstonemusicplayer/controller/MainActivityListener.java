package com.example.moonstonemusicplayer.controller;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MusicPlayer;
import com.example.moonstonemusicplayer.model.Song;
import com.example.moonstonemusicplayer.view.MainActivity;


/** MainActivityListener
 *  Handles input from the User (through Views in {@link com.example.moonstonemusicplayer.view.MainActivity}),
 *  changes the model {@link com.example.moonstonemusicplayer.model.MusicPlayer}) according to the input and
 *  and, if necessary, sends messages to the {@link com.example.moonstonemusicplayer.model.MusicPlayer}).
 */
public class MainActivityListener implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
  private static final boolean DEBUG = true;
  private static final String TAG = MainActivityListener.class.getSimpleName();
  private final MainActivity mainActivity;

  private MusicPlayer musicPlayer;

  private ServiceConnection serviceConnection = createServiceConnection();
  private MediaPlayerService mediaPlayerService;
  boolean isServiceBound = false;

  private SongListAdapter songListAdapter;
  private Thread seekbarAnimationThread;


  /**
   * Initiate the musicplayer and musicplayerservice.
   * @param mainActivity
   */
  public MainActivityListener(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    musicPlayer = new MusicPlayer(mainActivity.getBaseContext());
    bindSongListAdapterToSongListView(mainActivity.lv_songlist);

    /*
    musicPlayer.addSong(new Song("Lyric Pieces"
          ,"Grieg Kobold"
          , "https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg"
          ,86000));
    musicPlayer.addSong(new Song("Grand Duo Concertant for clarinet and piano - 2. Andante con moto"
          ,"Weber"
          ,"https://upload.wikimedia.org/wikipedia/commons/9/9a/Weber_-_Grand_Duo_Concertant_for_clarinet_and_piano_-_2._Andante_con_moto.ogg"
          ,383000));
           songListAdapter.notifyDataSetChanged();
     */
  }


  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case R.id.mi_loadLocaleAudioFile: {
        if(requestForPermission()){
          musicPlayer.loadLocalMusic();
          songListAdapter.notifyDataSetChanged();
        }
        break;
      }

    }
    songListAdapter.notifyDataSetChanged();
    return true;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    mainActivity.getMenuInflater().inflate(R.menu.options_menu,menu);
    return true;
  }

  @Override
  /** plays song that was clicked by user in songlistView*/
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    musicPlayer.setCurrentSongIndex(position);
    destroyAndCreateNewService();
  }

  @Override
  /** implements music controll: play/pause, next/prev*/
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btn_prev:
        musicPlayer.prevSong();
        destroyAndCreateNewService();
        break;
      case R.id.btn_play_pause:
        if(isServiceBound){
          if (mediaPlayerService.isPlayingMusic()) {
            pauseAudio();
          } else {
            resumeAudio();
          }
        }
        break;
      case R.id.btn_next:
        musicPlayer.nextSong();
        destroyAndCreateNewService();
        break;
    }
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {}
  @Override public void onStopTrackingTouch(SeekBar seekBar) {}
  @Override
  /** implements the music controll (seekTo)*/
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(mediaPlayerService.mediaPlayerReady() && fromUser){
      mediaPlayerService.seekTo(progress * 1000);
    }
  }

  private void resumeAudio(){
    if(isServiceBound){
      mediaPlayerService.resume();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(musicPlayer.getCurrentSong().getTitle());
      mainActivity.tv_artist.setText(musicPlayer.getCurrentSong().getArtist());
    }
  }

  private void pauseAudio(){
    if(isServiceBound){
      mediaPlayerService.pause();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_play));
      if(seekbarAnimationThread != null) seekbarAnimationThread = null;
    }
  }

  /** destroys mediaplayerservice and starts new one */
  private void destroyAndCreateNewService(){
    //if an service is bound destroy it ...
    if(DEBUG)Log.d(TAG,"destroyMediaPlayerService (bound: "+isServiceBound+")");
    if(isServiceBound){
      mediaPlayerService.onDestroy();
      isServiceBound = false;
    }

    // ... and start a new one.
    if(DEBUG)Log.d(TAG,"startMediaPlayerService");
    serviceConnection = createServiceConnection();
    Intent playerIntent = new Intent(mainActivity,MediaPlayerService.class);
    playerIntent.putExtra(MediaPlayerService.FILEPATHEXTRA,musicPlayer.getCurrentSong().getURI());
    mainActivity.startService(playerIntent);
    mainActivity.bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
  }

  /** create a serviceConnection for musicPlayerService and bind the service, prepare play*/
  private ServiceConnection createServiceConnection(){
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        if(DEBUG)Log.d(TAG,"onServiceConnected");
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getService();
        isServiceBound = true;

        //set views
        mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(android.R.drawable.ic_media_pause));
        mainActivity.tv_title.setText(musicPlayer.getCurrentSong().getTitle());
        mainActivity.tv_artist.setText(musicPlayer.getCurrentSong().getArtist());

        //set Seekbar and animate progress
        mainActivity.seekBar.setMax((int) musicPlayer.getCurrentSong().getDuration_ms()/1000);
        mainActivity.tv_seekbar_max.setText(musicPlayer.getCurrentSong().getDurationString(
            (int) musicPlayer.getCurrentSong().getDuration_ms()));
        animateMediaplayerProgressOnSeekbar();
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        isServiceBound = false;
      }
    };
  }

  /** animate the progress seek bar */
  private void animateMediaplayerProgressOnSeekbar(){
    final Handler mHandler = new Handler();
    //Make sure you update Seekbar on UI thread
    seekbarAnimationThread = null;
    seekbarAnimationThread = new Thread() {
      @Override
      public void run() {
        //one second has past ... update seekbar and song:lastPositoin
        if(mediaPlayerService.mediaPlayerReady()){
          int mCurrentPosition = mediaPlayerService.getCurrentPosition() / 1000;
          mainActivity.seekBar.setProgress(mCurrentPosition);
          mainActivity.tv_seekbar_progress.setText(Song.getDurationString(mediaPlayerService.getCurrentPosition()));
          musicPlayer.getCurrentSong().setDuration_ms(mCurrentPosition);
        }
        mHandler.postDelayed(this, 1000);
      }
    };
    mainActivity.runOnUiThread(seekbarAnimationThread);
  }


  /** bind songlistview to songlistadapter using the songList of musicplayer*/
  private void bindSongListAdapterToSongListView(ListView lv_songlist){
    songListAdapter = new SongListAdapter(mainActivity,musicPlayer.getCurrentSongList());
    lv_songlist.setAdapter(songListAdapter);
  }

  //Permissions

  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    boolean hasStoragePermission;
    int permissionCheck = ContextCompat.checkSelfPermission(
        mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    hasStoragePermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
    if (!hasStoragePermission) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,
          Manifest.permission.READ_EXTERNAL_STORAGE)) {
        //showExplanation("Permission Needed", "Rationale", Manifest.permission.READ_PHONE_STATE, 1234);
      } else {
        mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      Toast.makeText(mainActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return hasStoragePermission;
  }

}
