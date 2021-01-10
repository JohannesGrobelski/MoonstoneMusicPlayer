package com.example.moonstonemusicplayer.controller;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MusicManager;
import com.example.moonstonemusicplayer.model.PlayListModel;
import com.example.moonstonemusicplayer.model.Song;
import com.example.moonstonemusicplayer.view.MainActivity;

import java.util.ArrayList;
import java.util.List;


/** MainActivityListener
 *  Handles input from the User (through Views in {@link com.example.moonstonemusicplayer.view.MainActivity}),
 *  changes the model {@link MusicManager}) according to the input and
 *  and, if necessary, sends messages to the {@link MusicManager}).
 */
public class MainActivityListener
    implements AdapterView.OnItemClickListener, View.OnClickListener,
    SeekBar.OnSeekBarChangeListener, SearchView.OnQueryTextListener,
    SearchView.OnCloseListener {
  private static final boolean DEBUG = true;
  private static final String TAG = MainActivityListener.class.getSimpleName();

  private final MainActivity mainActivity;

  MusicManager musicManager;

  private ServiceConnection serviceConnection;
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
    musicManager = new MusicManager(mainActivity.getBaseContext());
    bindSongListAdapterToSongListView(mainActivity.lv_songlist);
    destroyAndCreateNewService();

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
          AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
              .setTitle("Lädt lokale Audiodatein neu ein.")
              .setMessage("Dies kann einige Minuten dauern.")
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  RefreshTask refreshTask = new RefreshTask(new RefreshTashListener() {
                    @Override
                    public void onCompletion() {
                      songListAdapter.notifyDataSetChanged();
                    }
                  });
                  refreshTask.execute(musicManager);
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
        }
        break;
      }


      case R.id.miDeleteAllItems: {
        musicManager.deleteAllSongs();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortTitle: {
        musicManager.sortByTitle();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortArtist: {
        musicManager.sortByArtist();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSortGenre: {
        musicManager.sortByGenre();
        songListAdapter.notifyDataSetChanged();
        break;
      }
      case R.id.miSwitchAscDesc: {
        musicManager.reverseList();
        songListAdapter.notifyDataSetChanged();
        break;
      }
    }
    songListAdapter.notifyDataSetChanged();
    return true;
  }

  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    mainActivity.getMenuInflater().inflate(R.menu.options_menu,menu);

    //create searchview
    MenuItem searchItem = menu.findItem(R.id.miSearch);
    mainActivity.searchView = (SearchView) searchItem.getActionView();
    mainActivity.searchView.setOnQueryTextListener(this);
    mainActivity.searchView.setOnSearchClickListener(this);
    mainActivity.searchView.setOnCloseListener(this);
    return true;
  }


  @Override
  /** plays song that was clicked by user in songlistView*/
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(isServiceBound){
      playSong(musicManager.getDisplayedSongList().get(position));
    }

    //destroy searchview and show music controlls, close searchview and close virtual keyboard
    mainActivity.searchView.setIconified(true);
    mainActivity.searchView.clearFocus();
    mainActivity.showMusicControlls();
  }

  @Override
  /** implements music controll: play/pause, next/prev*/
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.btn_prev:
        prevSong();
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
        nextSong();
        break;
      case R.id.btn_shuffle:{
        toogleShuffleMode();
        break;
      }
      case R.id.btn_repeat: {
        nextRepeatMode();
        break;
      }
      case R.id.miSearch: {
        mainActivity.hideMusicControlls();
        break;
      }
    }
  }


  @Override
  /** called if query in search view is submitted*/
  public boolean onQueryTextSubmit(String query) { return false; }

  @Override
  /** called if input in search view changes => search songs in db according to input*/
  public boolean onQueryTextChange(String query) {
    musicManager.searchSong(query);
    songListAdapter.notifyDataSetChanged();
    return false;
  }

  @Override
  /** called when search view is closed => open music controlls, reset filter query*/
  public boolean onClose() {
    mainActivity.showMusicControlls();
    return false;
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {}
  @Override public void onStopTrackingTouch(SeekBar seekBar) {}
  @Override
  /** implements the music controll (seekto): seeks to position in mediaPlayerService*/
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if(isServiceBound){
      if(mediaPlayerService.mediaPlayerReady() && fromUser){
        seekTo(progress * 1000);
      }
    }
  }


  //messages from mediaPlayerService

  /** media player error => set back UI */
  private void mediaPlayerServiceError(int cause){
    mainActivity.seekBar.setProgress(0);
    mainActivity.tv_seekbar_progress.setText("0:00");
    mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_play_button));
    Toast.makeText(mainActivity,"error: "+cause,Toast.LENGTH_LONG).show();
    //TODO: react to cause and display message
  }

  /** media player: audioFocusChange => change UI */
  private void audioFocusChange(int state){
    if(state == AudioManager.AUDIOFOCUS_GAIN){
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
    } else if(state == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT || state == AudioManager.AUDIOFOCUS_LOSS){
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_play_button));
      if(seekbarAnimationThread != null) seekbarAnimationThread = null;
    }
  }

  /** song finnished => set back UI and autoplay */
  private void finishSong(PlayListModel.REPEATMODE repeatmode){
    mainActivity.seekBar.setProgress(0);
    mainActivity.tv_seekbar_progress.setText("0:00");
    if(DEBUG)Log.d(TAG,"finishSong: "+repeatmode);

    if(repeatmode.equals(PlayListModel.REPEATMODE.NONE)){
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_play_button));
    } else {
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getTitle());
      mainActivity.setArtist(mediaPlayerService.getCurrentSong().getArtist());
      mainActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      mainActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
    }
  }

  //messages to mediaPlayerService


  private void playSong(Song song){
    if(isServiceBound){
      mediaPlayerService.playSong(song);
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getTitle());
      mainActivity.setArtist(mediaPlayerService.getCurrentSong().getArtist());
      mainActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      mainActivity.seekBar.setMax((int) (song.getDuration_ms() / 1000));
    }
  }

  private void prevSong(){
    if(isServiceBound){
      mediaPlayerService.prevSong();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getTitle());
      mainActivity.setArtist(mediaPlayerService.getCurrentSong().getArtist());
      mainActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      mainActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
    }
  }

  private void nextSong(){
    if(isServiceBound){
      mediaPlayerService.nextSong();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getTitle());
      mainActivity.setArtist(mediaPlayerService.getCurrentSong().getArtist());
      mainActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
      mainActivity.seekBar.setMax((int) (mediaPlayerService.getCurrentSong().getDuration_ms() / 1000));
    }
  }

  private void toogleShuffleMode(){
    if(isServiceBound){
      boolean shuffleMode = mediaPlayerService.toogleShuffleMode();
      Log.d(TAG,"toogleShuffle: "+shuffleMode);
      if(shuffleMode)mainActivity.btn_shuffle.setBackgroundTintList(mainActivity.getResources().getColorStateList(R.color.colorPrimary));
      else mainActivity.btn_shuffle.setBackgroundTintList(mainActivity.getResources().getColorStateList(android.R.color.darker_gray));
    }
  }

  private void nextRepeatMode(){
    if(isServiceBound){
      PlayListModel.REPEATMODE repeatmode = mediaPlayerService.nextRepeatMode();
      mainActivity.btn_repeat.setBackgroundTintList(mainActivity.getResources().getColorStateList(R.color.colorPrimary));
      mainActivity.btn_repeat.setText("");
      switch (repeatmode){
        case NONE: {mainActivity.btn_repeat.setBackgroundTintList(mainActivity.getResources().getColorStateList(android.R.color.darker_gray));break;}
        case ONESONG: {mainActivity.btn_repeat.setText("   1");}
      }
    }
  }

  /** implements the music controll (resume): resumes audio in mediaPlayerService*/
  private void resumeAudio(){
    if(isServiceBound){
      mediaPlayerService.resume();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_pause));
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_title.setText(mediaPlayerService.getCurrentSong().getTitle());
      mainActivity.setArtist(mediaPlayerService.getCurrentSong().getArtist());
      mainActivity.tv_seekbar_max.setText(mediaPlayerService.getCurrentSong().getDurationString());
    }
  }

  /** implements the music controll (pause): pauses audio in mediaPlayerService*/
  private void pauseAudio(){
    if(isServiceBound){
      mediaPlayerService.pause();
      mainActivity.btn_play_pause.setBackground(mainActivity.getResources().getDrawable(R.drawable.ic_play_button));
      if(seekbarAnimationThread != null) seekbarAnimationThread = null;
    }
  }

  private void seekTo(int seekPosition){
    if(isServiceBound){
      mediaPlayerService.seekTo(seekPosition);
      animateMediaplayerProgressOnSeekbar();
      mainActivity.tv_seekbar_progress.setText(Song.getDurationString(seekPosition));
    }
  }

  /** destroys mediaplayerservice and starts new one */
  private void destroyAndCreateNewService(){
    //if an service is bound destroy it ...
    if(isServiceBound){
      if(DEBUG)Log.d(TAG,"destroyMediaPlayerService (bound: "+isServiceBound+")");
      mediaPlayerService.onDestroy();
      isServiceBound = false;
    }

    // ... and start a new one.
    serviceConnection = createServiceConnection();
    Intent playerIntent = new Intent(mainActivity,MediaPlayerService.class);

    //start service
    mainActivity.startService(playerIntent);
    boolean bindService = mainActivity.getApplicationContext().
        bindService(playerIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    if(DEBUG)Log.d(TAG,"startMediaPlayerService: "+bindService);
  }



  /** create a serviceConnection for musicPlayerService and bind the service, prepare play*/
  private ServiceConnection createServiceConnection(){
    return new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        if(DEBUG)Log.d(TAG,"onServiceConnected");
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mediaPlayerService = binder.getService();
        binder.setListener(new BoundServiceListener() {
          @Override public void onError(int cause) { mediaPlayerServiceError(cause);}
          @Override public void finishedSong(PlayListModel.REPEATMODE repeatmode){ finishSong(repeatmode);}
          @Override public void onAudioFocusChange(int state) { audioFocusChange(state);}

          @Override
          public void transferPlayListFromActivityToService() {
            //transfer playlist from MusicManager to MediaPlayerService; problem: limit of 1MB of data in a Bundle
            //solution: save data in static variable and get it later in service

            if(DEBUG)Log.d(TAG,"startMediaPlayerService create Playlist: "+musicManager.getPlayList().size());
            if(DEBUG)Log.d(TAG,"startMediaPlayerService transfer Playlist: "+musicManager.getPlayList().size());
            mediaPlayerService.setPlayList(musicManager.getPlayList());
          }
        });
        Log.d(TAG,"onServiceConnected: binder: "+String.valueOf(binder==null));
        isServiceBound = true;
        //transfer data
        if(DEBUG)Log.d(TAG,"startMediaPlayerService transfer Playlist: "+musicManager.getPlayList().size());
        mediaPlayerService.setPlayList(musicManager.getPlayList());
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        isServiceBound = false;
      }
    };
  }

  /** animate the progress seek bar through a thread changes seekbar progress every second*/
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
        }
        mHandler.postDelayed(this, 1000);
      }
    };
    mainActivity.runOnUiThread(seekbarAnimationThread);
  }


  /** bind songlistview to songlistadapter using the songList of musicplayer*/
  private void bindSongListAdapterToSongListView(ListView lv_songlist){
    songListAdapter = new SongListAdapter(mainActivity,musicManager.getDisplayedSongList());
    lv_songlist.setAdapter(songListAdapter);
  }

  //Permissions

  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    int permissionCheck = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
      } else {
        mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      Toast.makeText(mainActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  public void onConfigurationChanged(Configuration newConfig) {

  }

  public void onDestroy() {
    if(isServiceBound){
      if(DEBUG)Log.d(TAG,"activity destroyed => destroy service");
      mediaPlayerService.onDestroy();
      isServiceBound = false;
    }
  }


  /** interface used to send messages from service to activity*/
  public interface BoundServiceListener {

    public void onError(int cause);
    public void finishedSong(PlayListModel.REPEATMODE repeatmode);
    public void onAudioFocusChange(int state);
    public void transferPlayListFromActivityToService();
  }

  /** */
  public interface RefreshTashListener {
    public void onCompletion();
  }
}
