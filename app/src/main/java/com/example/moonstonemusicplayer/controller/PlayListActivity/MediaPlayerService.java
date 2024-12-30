/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.Notification.Constants;
import com.example.moonstonemusicplayer.model.Database.Playcountlist.DBPlaycountList;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.model.PlaytimePersistence;
import com.example.moonstonemusicplayer.view.PlayListActivityListener;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/** MediaPlayerService
 * Plays the List<Song> specified by MusicPlayer.
 */
public class MediaPlayerService extends Service
       implements MediaPlayer.OnCompletionListener,
                  MediaPlayer.OnPreparedListener,
                  MediaPlayer.OnErrorListener,
                  MediaPlayer.OnSeekCompleteListener,
                  MediaPlayer.OnInfoListener,
                  MediaPlayer.OnBufferingUpdateListener,
                  AudioManager.OnAudioFocusChangeListener {

  private static final String CHANNEL_ID = "MoonstoneMediaPlayerServiceChannelID_8941891918918941351";
  Notification statusNotification;

  public static final String ACTION_NOTIFICATION_ORDER ="NOTIFICATION_ORDER";


  public static final String STARTING_INDEX = "STARTING_INDEX";
  private static final String TAG = MediaPlayerService.class.getSimpleName();
  private static final boolean DEBUG = true;

  private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
  private boolean isMediaPlayerPrepared = false;
  private PlayListModel playListModel = null;

  private Song currentSong;
  private String mediaPlayerCurrentDataSourceUri = "";
  /**/

  private AudioManager audioManager;
  private int startIndex;
  private int resumePosition = -1;

  private static final int notificationId = 8888;
  private NotificationManager notificationManager;

  private MediaSessionCompat mediaSession;
  private PlaybackStateCompat.Builder stateBuilder;

  private NotificationCompat.Builder notificationBuilder;

  /** inits mediaplayer and sets Listeners */
  private void initMediaPlayer(){
    onDestroy();
    mediaPlayer = new MediaPlayer();
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);
    mediaPlayer.setOnPreparedListener(this);
    mediaPlayer.setOnSeekCompleteListener(this);
    mediaPlayer.setOnInfoListener(this);
    mediaPlayer.setOnBufferingUpdateListener(this);

    //setzte Mediaplayer zurück damit er nicht auf falsche AudioDatei verweist
    mediaPlayer.reset();
    //stelle wiedergabe lautstärke auf musik ein
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
      if(playListModel.getCurrentSongFile().exists()){
        //weise Mediendatei der Datenquelle zu
        String uri = Uri.fromFile(playListModel.getCurrentSongFile()).toString();
        mediaPlayer.setDataSource(uri);
        mediaPlayerCurrentDataSourceUri = uri;

        //bereitet MediaPlayer für Wiedergabe vor
        mediaPlayer.prepareAsync();
        resumePosition = 0;

        if(((LocalBinder) iBinder) != null){
          ((LocalBinder) iBinder).boundServiceListener.selectedSong(playListModel.getCurrentSongFile().getPath());
        }

      } else {
        Toast.makeText(this, getResources().getString(R.string.file_does_not_exist),Toast.LENGTH_LONG).show();
      }
    } catch (IOException e) {
      e.printStackTrace();
      stopSelf();
    }
    initMediaSession();
  }

  private void updateMediaPlayer() {
    if(mediaPlayer == null){
      initMediaPlayer();
    }
    //setzte Mediaplayer zurück damit er nicht auf falsche AudioDatei verweist
    mediaPlayer.reset();
    //stelle wiedergabe lautstärke auf musik ein
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    try {
      if(playListModel.getCurrentSongFile().exists()){
        //weise Mediendatei der Datenquelle zu
        String uri = Uri.fromFile(playListModel.getCurrentSongFile()).toString();
        mediaPlayer.setDataSource(uri);
        mediaPlayerCurrentDataSourceUri = uri;

        //bereitet MediaPlayer für Wiedergabe vor
        mediaPlayer.prepareAsync();
        resumePosition = 0;

        if(((LocalBinder) iBinder) != null){
          ((LocalBinder) iBinder).boundServiceListener.selectedSong(playListModel.getCurrentSongFile().getPath());
        }

      } else {
        Toast.makeText(this, getResources().getString(R.string.file_does_not_exist),Toast.LENGTH_LONG).show();
      }
    } catch (IOException e) {
      e.printStackTrace();
      stopSelf();
    }
  }

    //public interface

  /**
     * will jump forward in current song by secondsForward seconds
     *
     */
    public void jumpXSecondsForward(int secondsForward) {
        if (DEBUG)
            Log.d(TAG, "resume: " + resumePosition);
        if (mediaPlayer == null) {
            initMediaPlayer();
            jumpXSecondsBackward(secondsForward);
        } else {
            if (requestAudioFocus()) {
                if (!isMediaPlayerPrepared) {
                    mediaPlayer.prepareAsync();
                } else {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.seekTo(currentPosition + (secondsForward  * 1000));
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                }
            }
        }
    }

    /**
     * will jump backwards in current song by secondsBackward seconds
     *
     */
    public void jumpXSecondsBackward(int secondsBackward) {
        if (DEBUG)
            Log.d(TAG, "resume: " + resumePosition);
        if (mediaPlayer == null) {
            initMediaPlayer();
            jumpXSecondsBackward(secondsBackward);
        } else {
            if (requestAudioFocus()) {
                if (!isMediaPlayerPrepared) {
                    mediaPlayer.prepareAsync();
                } else {
                  int currentPosition = mediaPlayer.getCurrentPosition();
                  mediaPlayer.seekTo(currentPosition - (secondsBackward  * 1000));
                  updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                }
            }
        }
    }

  public boolean mediaPlayerReady(){return (mediaPlayer != null && isPlayingMusic());}

  public boolean mediaPlayerNotNull(){return (mediaPlayer != null);}


  public boolean isPlayingMusic() {
    if(mediaPlayer != null) return mediaPlayer.isPlaying();
    else return false;
  }

  public int getCurrentPosition() {
    if(mediaPlayerReady())return mediaPlayer.getCurrentPosition();
    else return 0;
  }

  private void playMedia(){
    if(mediaPlayer != null && !mediaPlayer.isPlaying() && requestAudioFocus()){
      mediaPlayer.start();
    }
  }

  private void stopMedia(){
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      {mediaPlayer.stop(); isMediaPlayerPrepared=false;}
    }
    if(playListModel.getCurrentSong() != null && playListModel.getCurrentSong().getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS){
      PlaytimePersistence.savePlaytime(this, mediaPlayerCurrentDataSourceUri, mediaPlayer.getCurrentPosition() / 1000);
    }
    updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
  }

  //Listener interface
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG,"onStartCommand: binder: "+ (iBinder == null));
    if(intent.hasExtra(STARTING_INDEX)){
      startIndex = intent.getIntExtra(STARTING_INDEX,0);
      if(DEBUG)Log.d(TAG,"starting song: "+startIndex);
    }

    String intentAction = intent.getAction();
    if(intentAction != null){
      if (intentAction.equals(Constants.ACTION.PREV_ACTION)) {
        Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Clicked Previous");
        prevSong();
      } else if (intentAction.equals(Constants.ACTION.PLAY_ACTION)) {//toogles play,resume
        Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Clicked Play");
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
          pause();
          ((LocalBinder) iBinder).boundServiceListener.pauseSong();
        } else {
          resume();
          ((LocalBinder) iBinder).boundServiceListener.resumeSong();
        }
      } else if (intentAction.equals(Constants.ACTION.NEXT_ACTION)) {
        Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Clicked Next");
        nextSong();
      } else if (intentAction.equals(
          Constants.ACTION.STOPFOREGROUND_ACTION)) {
        Log.i(TAG, "Received Stop Foreground Intent");
        Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
        stopForeground(true);
        stopSelf();
        stopMedia();
        ((LocalBinder) iBinder).boundServiceListener.stopSong();
      }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    final IntentFilter theFilter = new IntentFilter();
    theFilter.addAction(ACTION_NOTIFICATION_ORDER);
    //init audiomanager
    this.audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    Log.d(TAG,"AudioManager.requestAudioFocus...");
    requestAudioFocus();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if(mediaPlayer != null){
      stopMedia();
      mediaPlayer.release();
      mediaPlayer = null;
      if(DEBUG)Log.d(TAG,"onDestroy");
    }
    stopSelf(); //beende Service
    removeAudioFocus();
    if(notificationManager != null)notificationManager.cancelAll();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    Log.d(TAG,"onBind: binder: "+ (iBinder == null));
    return iBinder;
  }

  /** wird aufgerufen um den BufferStatus einer Medienresource, die über Netzwerkgestreamt wird anzuzeigen*/
  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
  }

  /** wird aufgerufen wenn Medienresource fertig abgespielt wurde*/
  @Override
  public void onCompletion(MediaPlayer mp) {
    if(mediaPlayer != null){
      autoPlay();
      if(((LocalBinder) iBinder) != null){
        if(DEBUG)Log.d(TAG,"onCompletion: "+(iBinder != null));
        ((LocalBinder) iBinder).boundServiceListener.finishedSong(playListModel.repeatmode);
      }
    }
  }

  /** wird aufgerufen wenn es Fehler gibt */
  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    switch (what){
      case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
        break;
      case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
        break;
      case MediaPlayer.MEDIA_ERROR_UNKNOWN:
        break;
    }
    if(((LocalBinder) iBinder) != null)((LocalBinder) iBinder).boundServiceListener.onError(what);
    return false;
  }

  /** wird aufgerufen um uns Informationen zu geben */
  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    return false;
  }

  /** wird aufgerufen wenn medienresource bereit ist zum abspielen */
  @Override
  public void onPrepared(MediaPlayer mp) {
    playMedia();
    isMediaPlayerPrepared = true;
    //seekto if player was stopped
    if(resumePosition != 0){
      mediaPlayer.seekTo(resumePosition);
    } else {
      //get playposition from PlayTimePersistence
      if(playListModel.getCurrentSong() != null && playListModel.getCurrentSong().getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS){
        int playtime = PlaytimePersistence.getPlaytime(this, Uri.fromFile(playListModel.getCurrentSongFile()).toString());
        if(playtime < playListModel.getCurrentSong().getDuration_ms() / 1000){
          resumePosition = playtime;
          mediaPlayer.seekTo(resumePosition * 1000);
        }
      }
    }
    mediaPlayer.setVolume(1.0f,1.0f);
  }

  /** wird aufgerufen beim abschluss einer onSeek Operation*/
  @Override
  public void onSeekComplete(MediaPlayer mp) {}

  /** aufgerufen wenn sich der Audiofokus ändert, z.B durch eingehenden Anruf */
  @Override
  public void onAudioFocusChange(int focusState) {
    if(DEBUG)Log.d(TAG,"onAudioFocusChange");
    switch (focusState){
      case AudioManager.AUDIOFOCUS_GAIN:
        //setze Wiedergabe fort
        Toast.makeText(getApplicationContext(), "GAIN audiofocus", Toast.LENGTH_LONG).show();
        resume();
        break;
      case AudioManager.AUDIOFOCUS_LOSS:
        //verlieren Audiofokus verloren und auf unbestimmte Zeit verloren
        if(mediaPlayer.isPlaying()){mediaPlayer.stop(); isMediaPlayerPrepared=false;}
        resumePosition = mediaPlayer.getCurrentPosition();
        Toast.makeText(getApplicationContext(), "LOST audiofocus", Toast.LENGTH_LONG).show();
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        //verlieren Audiofokus für kurze,unbestimmte Zeit verloren (z.B. YouTube Wiedergabe gestartet)
        if(mediaPlayer.isPlaying())mediaPlayer.pause();
        resumePosition = mediaPlayer.getCurrentPosition();
        Toast.makeText(getApplicationContext(), "TRANS LOST audiofocus", Toast.LENGTH_LONG).show();
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        //verlieren Audiofokus für kurze Zeit (z.B. Klingelton)
        mediaPlayer.setVolume(0.1f,0.1f);
        Toast.makeText(getApplicationContext(), "TRANS DUCK audiofocus", Toast.LENGTH_LONG).show();
        break;
    }
    if(((LocalBinder) iBinder) != null)((LocalBinder) iBinder).boundServiceListener.onAudioFocusChange(focusState);
  }

  private boolean requestAudioFocus(){
    if(audioManager!=null){
      int result = 0;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result = audioManager.requestAudioFocus(new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                )
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this).build()
        );
      } else {
        result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
      }
      return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    } else return true;
  }

  private boolean removeAudioFocus(){
    if(audioManager!=null)return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    else return true;
  }

  public Song getCurrentSong() {
    if(playListModel == null)return null;
    return playListModel.getCurrentSong();
  }


  public File getCurrentSongFile() {return playListModel.getCurrentSongFile();}

  public void seekTo(int i) {
    if(DEBUG)Log.d(TAG,"seekTo: "+i);
    mediaPlayer.seekTo(i);
    resumePosition = i;
    updateMediaMetadata(playListModel.getCurrentSong());
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
  }

  public void playSong(File songFile) {
    playListModel.setCurrentSongIndexBySong(songFile);
    //Toast.makeText(this,"clicked: "+playListModel.getCurrentSong().getName(),Toast.LENGTH_LONG).show();
    Song song = BrowserManager.getSongFromAudioFile(songFile);
    DBPlaylists.getInstance(this.getApplicationContext()).addToRecentlyPlayed(this.getApplicationContext(),song);
    updateMediaPlayer();
    showNotification();
    updateMediaMetadata(playListModel.getCurrentSong());
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
    updatePlaycount(playListModel.getCurrentSong());
  }

  public void pause() {
    showNotification();
    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      mediaPlayer.pause();
      resumePosition = mediaPlayer.getCurrentPosition();
    }
  }

  public void resume() {
    showNotification();
    updateMediaMetadata(playListModel.getCurrentSong());
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
    //setze Wiedergabe fort
    if(mediaPlayer == null){
      initMediaPlayer();
    }
    else {
      if(requestAudioFocus()){
        if(!isMediaPlayerPrepared){//state stop
          mediaPlayer.prepareAsync();
        } else {
          mediaPlayer.seekTo(resumePosition);
          updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
          mediaPlayer.setVolume(1.0f,1.0f);
          if(!mediaPlayer.isPlaying())mediaPlayer.start();
        }
      }
    }
  }

  public void nextSong() {
    playListModel.nextSong();
    updateMediaPlayer();
    showNotification();
    updateMediaMetadata(playListModel.getCurrentSong());
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
    updatePlaycount(playListModel.getCurrentSong());
  }

  public void prevSong() {
    playListModel.prevSong();
    updateMediaPlayer();
    showNotification();
    updateMediaMetadata(playListModel.getCurrentSong());
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
    updatePlaycount(playListModel.getCurrentSong());
  }


  public boolean toogleShuffleMode() {return playListModel.toogleShuffleMode();}
  public PlayListModel.REPEATMODE nextRepeatMode() { return playListModel.nextRepeatMode();}

  /** defines what happens when a song is finnished */
  private void autoPlay(){
    switch(playListModel.repeatmode){
      case ONESONG: {
        playSong(playListModel.getCurrentSongFile());
        break;
      }
      case ALL: {
        nextSong();
        break; 
      }
    }
  }

  public void setPlayList(List<File> playList) {
    if(DEBUG)Log.d(TAG,"startMediaPlayerService init Playlist: "+playList.size());
    this.playListModel = new PlayListModel(playList);
    this.playListModel.setCurrentSongIndexBySong(playList.get(startIndex));
  }


  /** used to bind service to activity*/
  public class LocalBinder extends Binder {
    public com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener.BoundServiceListener boundServiceListener;

    public MediaPlayerService getService() {
      return MediaPlayerService.this;
    }

    /** set Listener-Object*/
    public void setListener(com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener.BoundServiceListener listener) {
      boundServiceListener = listener;
    }

  }

  public void updatePlaylist(Playlist updatedPlaylist){
    List<File> playList = updatedPlaylist.getPlaylist().stream().map(BrowserManager::getFileFromSong).collect(Collectors.toList());
    int currentSongIndex = 0;
    for(int i=0; i<updatedPlaylist.getPlaylist().size(); i++){
      if(updatedPlaylist.getPlaylist().get(i).getName().equals(playListModel.getCurrentSong().getName())){
        currentSongIndex = i;
        currentSong = updatedPlaylist.getPlaylist().get(i);
      }
    }
    this.playListModel = new PlayListModel(playList);
    this.playListModel.setCurrentSongIndex(currentSongIndex);
  }

  private void updatePlaycount(Song song){
    DBPlaycountList.getInstance(getApplicationContext()).playedSong(getApplicationContext(),song);
  }


  private void initMediaSession(){
    mediaSession = new MediaSessionCompat(this, TAG);

    // Set the callback to handle media actions
    mediaSession.setCallback(new MediaSessionCompat.Callback() {
      @Override
      public void onPlay() {
        super.onPlay();
        resume();
        updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
      }

      @Override
      public void onPause() {
        super.onPause();
        pause();
        updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
      }

      @Override
      public void onSkipToNext() {
        super.onSkipToNext();
        nextSong();
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
      }

      @Override
      public void onSkipToPrevious() {
        super.onSkipToPrevious();
        prevSong();
        updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
      }

      @Override
      public void onSeekTo(long position) {
        super.onSeekTo(position);
        seekTo((int) position);
        updatePlaybackState(mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED);
      }
    });

    mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

    // Set the session as active
    mediaSession.setActive(true);
  }

  private void updatePlaybackState(int state) {
    long position = mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;

    stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_SEEK_TO)
            .setState(state, position, 1.0f);

    mediaSession.setPlaybackState(stateBuilder.build());
  }

  private void updateMediaMetadata(Song currentSong) {
    if (currentSong == null) return;

    MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
            .setTitle(currentSong.getName())
            .setSubtitle(currentSong.getArtist())
            .setDescription(currentSong.getAlbum())
            .build();

    mediaSession.setMetadata(new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getName())
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbum())
            .build());
  }

  private void updateNotification(String message, int progress) {
    notificationBuilder.setContentText(message)
            .setProgress(100, progress, true);
    notificationManager.notify(notificationId, notificationBuilder.build());
  }

  private void showNotification(){
    // Create notification intent
    final Intent notificationIntent = new Intent(MediaPlayerService.this, PlayListActivityListener.class);
    notificationIntent.setAction(Intent.ACTION_MAIN);
    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);

    // Create action intents
    Intent previousIntent = new Intent(this, MediaPlayerService.class);
    previousIntent.setAction(Constants.ACTION.PREV_ACTION);
    PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
            previousIntent, PendingIntent.FLAG_IMMUTABLE);

    Intent playIntent = new Intent(this, MediaPlayerService.class);
    playIntent.setAction(Constants.ACTION.PLAY_ACTION);
    PendingIntent pplayIntent = PendingIntent.getService(this, 0,
            playIntent, PendingIntent.FLAG_IMMUTABLE);

    Intent nextIntent = new Intent(this, MediaPlayerService.class);
    nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
    PendingIntent pnextIntent = PendingIntent.getService(this, 0,
            nextIntent, PendingIntent.FLAG_IMMUTABLE);

    Intent closeIntent = new Intent(this, MediaPlayerService.class);
    closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
    PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
            closeIntent, PendingIntent.FLAG_IMMUTABLE);

    // Create notification channel
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    CharSequence name = getString(R.string.channel_name);
    String description = getString(R.string.channel_description);
    int importance = NotificationManager.IMPORTANCE_LOW;
    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
    notificationChannel.setDescription(description);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManager.createNotificationChannel(notificationChannel);

    // Get song information
    String songTitle = playListModel.getCurrentSong() == null ? "Unknown" : playListModel.getCurrentSong().getName();
    String artist = playListModel.getCurrentSong().getArtist();
    if (artist.isEmpty() || artist.contains("<unknown>")) {
      artist = "Unknown Artist";
    }
    String album = playListModel.getCurrentSong().getAlbum();

    // Create the notification using NotificationCompat
    notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(songTitle)
            .setContentText(artist)
            .setSubText(album)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setDeleteIntent(pcloseIntent);


    // Add actions
    notificationBuilder.addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent);
    notificationBuilder.addAction(mediaPlayer != null && mediaPlayer.isPlaying() ?
                    android.R.drawable.ic_media_play : android.R.drawable.ic_media_pause,
            "Play/Pause",
            pplayIntent);
    notificationBuilder.addAction(android.R.drawable.ic_media_next, "Next", pnextIntent);

    // Load album art
    Bitmap albumArt = null;
    try {
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      retriever.setDataSource(playListModel.getCurrentSongFile().getPath());
      byte[] art = retriever.getEmbeddedPicture();
      if (art != null) {
        albumArt = BitmapFactory.decodeByteArray(art, 0, art.length);
      }
      retriever.release();
    } catch (Exception e) {
      Log.e(TAG, "Error loading album art", e);
    }

    // If no album art was found, use a default image
    if (albumArt == null) {
      albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_moonstonemusicplayerlogo);
      notificationBuilder.setSmallIcon(R.drawable.ic_moonstonemusicplayerlogo);
    } else {
      notificationBuilder.setSmallIcon(IconCompat.createWithBitmap(albumArt));
    }

    // Set small and large icon (album art)
    notificationBuilder.setLargeIcon(albumArt);
    notificationBuilder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);

    // Create the media style
    androidx.media.app.NotificationCompat.MediaStyle mediaStyle =
            new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowCancelButton(true)
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2);

    // Apply the media style
    notificationBuilder.setStyle(mediaStyle);

    // Get the current playback position and duration
    // Calculate playback progress

    long duration = 0;
    long position = 0;
    if (mediaPlayer != null) {
      try {
        duration = mediaPlayer.getDuration();
        position = getCurrentPosition();
      } catch (IllegalStateException e) {
        Log.e(TAG, "Error retrieving duration or position", e);
      }
    }

    notificationBuilder.setProgress((int) duration / 1000, (int) position, false);

    Notification notification = notificationBuilder.build();
    notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
    // Build and show the notification
    notificationManager.notify(notificationId, notification);
  }
}
