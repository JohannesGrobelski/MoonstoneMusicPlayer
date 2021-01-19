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
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.Notification.Constants;
import com.example.moonstonemusicplayer.model.Database.Folder.DBFolder;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;

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
  public static final String FOLDERSONGINDEXEXTRA = "FOLDERSONGINDEXEXTRA";

  public static final String ACTION_NOTIFICATION_ORDER ="NOTIFICATION_ORDER";


  public static final String STARTING_INDEX = "STARTING_INDEX";
  private static final String TAG = MediaPlayerService.class.getSimpleName();
  private static final boolean DEBUG = true;

  private final IBinder iBinder = new LocalBinder();;
  private MediaPlayer mediaPlayer;
  private boolean isMediaPlayerPrepared = false;
  private PlayListModel playListModel = null;

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
      //weise Mediendatei der Datenquelle zu
      mediaPlayer.setDataSource(playListModel.getCurrentSong().getUri());
    } catch (IOException e) {
      e.printStackTrace();
      stopSelf();
    }
    //bereitet MediaPlayer für Wiedergabe vor
    mediaPlayer.prepareAsync();
    resumePosition = 0;

    if(((LocalBinder) iBinder) != null){
      ((LocalBinder) iBinder).boundServiceListener.selectedSong(playListModel.getCurrentSong().getUri());
    }
  }

  //public interface
  public void resume() {
    if(DEBUG)Log.d(TAG,"resume: "+resumePosition);
    showNotification();
    //setze Wiedergabe fort
    requestAudioFocus();
    if(mediaPlayer == null)initMediaPlayer();
    else {
      if(!isMediaPlayerPrepared){//state stop
        mediaPlayer.prepareAsync();
      } else {
        mediaPlayer.seekTo(resumePosition);
        mediaPlayer.setVolume(1.0f,1.0f);
        if(!mediaPlayer.isPlaying())mediaPlayer.start();
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
    if(mediaPlayer != null && !mediaPlayer.isPlaying()){
      mediaPlayer.start();
    }
  }

  private void stopMedia(){
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      {mediaPlayer.stop(); isMediaPlayerPrepared=false;}
    }
  }

  //Listener interface
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(TAG,"onStartCommand: binder: "+String.valueOf(iBinder==null));
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
    Log.d(TAG,"onBind: binder: "+String.valueOf(iBinder==null));
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
    if(resumePosition != 0)mediaPlayer.seekTo(resumePosition);
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
        resume();
        break;
      case AudioManager.AUDIOFOCUS_LOSS:
        //verlieren Audiofokus verloren und auf unbestimmte Zeit verloren
        if(mediaPlayer.isPlaying()){mediaPlayer.stop(); isMediaPlayerPrepared=false;}
        resumePosition = mediaPlayer.getCurrentPosition();
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        //verlieren Audiofokus für kurze,unbestimmte Zeit verloren (z.B. YouTube Wiedergabe gestartet)
        if(mediaPlayer.isPlaying())mediaPlayer.pause();
        resumePosition = mediaPlayer.getCurrentPosition();
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        //verlieren Audiofokus für kurze Zeit (z.B. Klingelton)
        mediaPlayer.setVolume(0.1f,0.1f);
        break;
    }
    if(((LocalBinder) iBinder) != null)((LocalBinder) iBinder).boundServiceListener.onAudioFocusChange(focusState);
  }

  private boolean requestAudioFocus(){
    if(audioManager!=null){
      audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
      int result = audioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
      return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    } else return true;
  }

  private boolean removeAudioFocus(){
    if(audioManager!=null)return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    else return true;
  }

  public Song getCurrentSong() {return playListModel.getCurrentSong();}

  public void seekTo(int i) {
    if(DEBUG)Log.d(TAG,"seekTo: "+i);
    mediaPlayer.seekTo(i);
    resumePosition = i;
  }

  public void playSong(Song song) {
    playListModel.setCurrentSong(song);
    //Toast.makeText(this,"clicked: "+playListModel.getCurrentSong().getName(),Toast.LENGTH_LONG).show();
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
        playSong(playListModel.getCurrentSong());
        break;
      }
      case ALL: {
        nextSong();
        break; 
      }
    }
  }

  public void setPlayList(List<Song> playList) {
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

  public void showNotification(){
    // Logic to turn on the screen
    PowerManager powerManager = (PowerManager) this.getSystemService(POWER_SERVICE);
    if (!powerManager.isInteractive()){ // if screen is not already on, turn it on (get wake_lock for 10 seconds)
      PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MoonstoneMediaPlayer:MediaPlayerService");
      wl.acquire(10000);
      PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MoonstoneMediaPlayer:MediaPlayerService");
      wl_cpu.acquire(10000);
    }


    // Using RemoteViews to bind custom layouts into Notification
    RemoteViews views = new RemoteViews(getPackageName(),R.layout.status_bar);
    RemoteViews bigViews = new RemoteViews(getPackageName(),R.layout.status_bar_expanded);

    //setting up the notification intent
    final Intent notificationIntent = new Intent(MediaPlayerService.this, PlayListActivity.class);
    notificationIntent.setAction(Intent.ACTION_MAIN);
    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
        notificationIntent, 0);


    //setting up the intents for the actions available in notification: previous, play, next, close
    Intent previousIntent = new Intent(this, MediaPlayerService.class);
    previousIntent.setAction(Constants.ACTION.PREV_ACTION);
    PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
        previousIntent, 0);

    Intent playIntent = new Intent(this, MediaPlayerService.class);
    playIntent.setAction(Constants.ACTION.PLAY_ACTION);
    PendingIntent pplayIntent = PendingIntent.getService(this, 0,
        playIntent, 0);

    Intent nextIntent = new Intent(this, MediaPlayerService.class);
    nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
    PendingIntent pnextIntent = PendingIntent.getService(this, 0,
        nextIntent, 0);

    Intent closeIntent = new Intent(this, MediaPlayerService.class);
    closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
    PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
        closeIntent, 0);



    PendingIntent popenIntent = PendingIntent.getActivity(this, 0,
        notificationIntent, 0);

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
    views.setTextViewText(R.id.status_bar_track_name, playListModel.getCurrentSong().getName());
    bigViews.setTextViewText(R.id.status_bar_track_name, playListModel.getCurrentSong().getName());
    String artist = playListModel.getCurrentSong().getArtist();
    if(artist.isEmpty())artist = "unknown artist";
    views.setTextViewText(R.id.status_bar_artist_name, artist);
    bigViews.setTextViewText(R.id.status_bar_artist_name, artist);

    //get album image and album (if possible)
    Bitmap songImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_moonstonemusicplayerlogo);
    String albumName = "unknown album";

    try {
      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      mmr.setDataSource(playListModel.getCurrentSong().getUri());
      String meta_albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
      if(meta_albumName != null && !meta_albumName.isEmpty() && !meta_albumName.equals("null")){
        albumName = meta_albumName;
      }
    } catch (Exception e) {
      if(e.getMessage() != null) Log.e(TAG,e.getMessage());
    }
    try {
      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      mmr.setDataSource(playListModel.getCurrentSong().getUri());
      byte[] albumArtBytes = mmr.getEmbeddedPicture();
      songImage = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.length);
    } catch (Exception e) {
      if(e.getMessage() != null) Log.e(TAG,e.getMessage());
    }

    //views.setImageViewBitmap(R.id.iv_status_bar_album_art, songImage);
    //bigViews.setImageViewBitmap(R.id.iv_status_bar_album_art, songImage);
    views.setTextViewText(R.id.status_bar_album_name, albumName);
    bigViews.setTextViewText(R.id.status_bar_album_name, albumName);

    Notification.Builder notificationBuilder = new Notification.Builder(this);
    notificationManager = getSystemService(NotificationManager.class);

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Create the NotificationChannel, but only on API 26+ because
      // the NotificationChannel class is new and not in the support library
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        notificationChannel.setDescription(description);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(notificationChannel);
        notificationBuilder.setChannelId(CHANNEL_ID);
      }
    } else {
      // If earlier version channel ID is not used
      // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
    }

    notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

    statusNotification = notificationBuilder.build();
    statusNotification.contentView = views;
    statusNotification.bigContentView = bigViews;
    statusNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
    statusNotification.icon = R.drawable.ic_moonstonemusicplayerlogo;
    statusNotification.contentIntent = pendingIntent;
    
    notificationManager.notify(8888,statusNotification);
    //startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, statusNotification);

  }

}
