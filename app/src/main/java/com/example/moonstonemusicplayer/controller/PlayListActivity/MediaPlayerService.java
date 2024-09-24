package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.Notification.Constants;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.model.PlaytimePersistence;
import com.example.moonstonemusicplayer.view.PlayListActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;


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
  private NotificationManager notificationManager;

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

  }

  //public interface
  public void resume() {
    if(DEBUG)Log.d(TAG,"resume: "+resumePosition);
    showNotification();
    //setze Wiedergabe fort
    if(mediaPlayer == null)initMediaPlayer();
    else {
      if(requestAudioFocus()){
        if(!isMediaPlayerPrepared){//state stop
          mediaPlayer.prepareAsync();
        } else {
          mediaPlayer.seekTo(resumePosition);
          mediaPlayer.setVolume(1.0f,1.0f);
          if(!mediaPlayer.isPlaying())mediaPlayer.start();
        }
      }
    }
  }

  public void pause() {
    showNotification();
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      mediaPlayer.pause();
      resumePosition = mediaPlayer.getCurrentPosition();
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

  public Song getCurrentSong() {return playListModel.getCurrentSong();}


  public File getCurrentSongFile() {return playListModel.getCurrentSongFile();}

  public void seekTo(int i) {
    if(DEBUG)Log.d(TAG,"seekTo: "+i);
    mediaPlayer.seekTo(i);
    resumePosition = i;
  }

  public void playSong(File songFile) {
    playListModel.setCurrentSong(songFile);
    //Toast.makeText(this,"clicked: "+playListModel.getCurrentSong().getName(),Toast.LENGTH_LONG).show();
    Song song = BrowserManager.getSongFromAudioFile(songFile);
    DBPlaylists.getInstance(this.getApplicationContext()).addToRecentlyPlayed(this.getApplicationContext(),song);
    initMediaPlayer();
    showNotification();
  }

  public void nextSong() {
    playListModel.nextSong();
    initMediaPlayer();
    showNotification();
  }

  public void prevSong() {
    playListModel.prevSong();
    initMediaPlayer();
    showNotification();
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
    this.playListModel.setCurrentSong(playList.get(startIndex));
  }


  /** used to bind service to activity*/
  public class LocalBinder extends Binder {
    public PlayListActivityListener.BoundServiceListener boundServiceListener;

    public MediaPlayerService getService() {
      return MediaPlayerService.this;
    }

    /** set Listener-Object*/
    public void setListener(PlayListActivityListener.BoundServiceListener listener) {
      boundServiceListener = listener;
    }

  }
  
  /** Certainly! Here's the * createMediaNotification() method that *includes media control actions for Play, * Pause, Previous, Next, and Seek. This *method assumes that you're inside a *MediaService class and have a *MediaPlayer instance available.*/
private Notification createMediaNotification() {
    // Create intents for media control actions
    PendingIntent playPauseIntent = PendingIntent.getService(
        this,
        0,
        new Intent(this, YourMediaService.class).setAction("ACTION_PLAY_PAUSE"),
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    PendingIntent previousIntent = PendingIntent.getService(
        this,
        0,
        new Intent(this, YourMediaService.class).setAction("ACTION_PREVIOUS"),
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    PendingIntent nextIntent = PendingIntent.getService(
        this,
        0,
        new Intent(this, YourMediaService.class).setAction("ACTION_NEXT"),
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    PendingIntent seekForwardIntent = PendingIntent.getService(
        this,
        0,
        new Intent(this, YourMediaService.class).setAction("ACTION_SEEK_FORWARD"),
        PendingIntent.FLAG_UPDATE_CURRENT
    );

    PendingIntent seekBackwardIntent = PendingIntent.getService(
        this,
        0,
        new Intent(this, YourMediaService.class).setAction("ACTION_SEEK_BACKWARD"),
        PendingIntent.FLAG_UPDATE_CURRENT
    );
    
       //get album image and album (if possible)
    Song song = playListModel.getCurrentSong();
    String albumName = song.getAlbum();


    // Determine play/pause icon and action
    int playPauseIcon = mediaPlayer.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
    String playPauseLabel = mediaPlayer.isPlaying() ? "Pause" : "Play";
    
    //set up the texts in the notification
    //set up the texts in the notification
        String truncatedSongTitle = playListModel.getCurrentSong() == null ? "test123" : playListModel.getCurrentSong().getName().length() > 15 ? 
                                                                  playListModel.getCurrentSong().getName().substring(0, 15)+"..." : playListModel.getCurrentSong().getName();

    // Build the notification with media controls
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "media_playback_channel")
        .setSmallIcon(R.drawable.ic_media)  // Notification icon
        .setContentTitle(truncatedSongTitle)      // Set the media title
        .setContentText(playListModel.getCurrentSong()      // Set the media artist
        .setLargeIcon(albumArtBitmap)       // Optional: Album art
        .addAction(new NotificationCompat.Action(
            R.drawable.ic_skip_previous, "Previous", previousIntent)) // Previous button
        .addAction(new NotificationCompat.Action(
            R.drawable.ic_rewind, "Rewind", seekBackwardIntent))       // Seek backward button
        .addAction(new NotificationCompat.Action(
            playPauseIcon, playPauseLabel, playPauseIntent))           // Play/Pause button
        .addAction(new NotificationCompat.Action(
            R.drawable.ic_fast_forward, "Forward", seekForwardIntent)) // Seek forward button
        .addAction(new NotificationCompat.Action(
            R.drawable.ic_skip_next, "Next", nextIntent))              // Next button
        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(2, 3, 4))  // Show play/pause, forward, and rewind in compact view
        .setOngoing(true)  // Makes the notification persistent
        .setPriority(NotificationCompat.PRIORITY_HIGH)  // Set high priority to ensure it's visible
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT);  // Mark this as a transport control notification

    return builder.build();
}

  public void showNotification(){
    //setting up the notification intent
    final Intent notificationIntent = new Intent(MediaPlayerService.this, PlayListActivity.class);
    notificationIntent.setAction(Intent.ACTION_MAIN);
    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);


    //setting up the intents for the actions available in notification: previous, play, next, close
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

    PendingIntent popenIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);

    // Using RemoteViews to bind custom layouts into Notification
    RemoteViews views = new RemoteViews(getPackageName(),R.layout.status_bar);
    RemoteViews bigViews = new RemoteViews(getPackageName(),R.layout.status_bar_expanded);

    //connect views with pending intents
    views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
    bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
    views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
    bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
    views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
    bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
    views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
    bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
    views.setOnClickPendingIntent(R.id.iv_status_bar_album_art, popenIntent);
    bigViews.setOnClickPendingIntent(R.id.iv_status_bar_album_art, popenIntent);

    if(mediaPlayer != null && mediaPlayer.isPlaying()) {
      views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play_button);
      bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play_button);
    } else {
      views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
      bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_pause);
    }

    //set up the texts in the notification
    String truncatedSongTitle = playListModel.getCurrentSong() == null ? "test123" : playListModel.getCurrentSong().getName().length() > 15 ? 
                                                              playListModel.getCurrentSong().getName().substring(0, 15)+"..." : playListModel.getCurrentSong().getName();

    views.setTextViewText(R.id.status_bar_track_name, truncatedSongTitle);
    bigViews.setTextViewText(R.id.status_bar_track_name, truncatedSongTitle);
    String artist = playListModel.getCurrentSong().getArtist();
    if(artist.isEmpty() || artist.contains("<unknown>")){
      views.setViewVisibility(R.id.status_bar_album_name, View.GONE); 
    } else {
      views.setViewVisibility(R.id.status_bar_album_name, View.VISIBLE); 
    }
    views.setTextViewText(R.id.status_bar_artist_name, artist);
    bigViews.setTextViewText(R.id.status_bar_artist_name, artist);

    //get album image and album (if possible)
    Song song = playListModel.getCurrentSong();
    String albumName = song.getAlbum();
    bigViews.setTextViewText(R.id.status_bar_album_name, albumName);

    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    //Create a notification channel
    CharSequence name = getString(R.string.channel_name);
    String description = getString(R.string.channel_description);
    int importance = NotificationManager.IMPORTANCE_MAX;
    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
    notificationChannel.setDescription(description);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    notificationManager.createNotificationChannel(notificationChannel);

       //build the notification with Builder Pattern 
    Notification.Builder notificationBuilder = new Notification.Builder(this, CHANNEL_ID);
    notificationBuilder.setContentTitle(getString(R.string.app_name));
    notificationBuilder.setContentText(truncatedSongTitle);
    notificationBuilder.setChannelId(CHANNEL_ID);
    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
    notificationBuilder.setCustomContentView(views);
    notificationBuilder.setCustomBigContentView(bigViews);
    notificationBuilder.setSmallIcon(R.drawable.ic_moonstonemusicplayerlogo);
    notificationBuilder.setWhen(System.currentTimeMillis());
    notificationBuilder.setAutoCancel(false);

    statusNotification = notificationBuilder.build();
    statusNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
    statusNotification.contentIntent = pendingIntent;
    
    notificationManager.notify(8888,statusNotification);
    //startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, statusNotification);
  }
}
