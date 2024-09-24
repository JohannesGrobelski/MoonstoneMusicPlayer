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
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.Notification.Constants;
import com.example.moonstonemusicplayer.controller.PlayListActivity.Notification.MediaNotificationManager;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.model.PlaytimePersistence;

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
  private MediaSessionCompat mediaSession;
  private MediaNotificationManager mediaNotificationManager;

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
          updatePlaybackState(mediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED);
        }
      }
    }
  }

  public void pause() {
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      mediaPlayer.pause();
      resumePosition = mediaPlayer.getCurrentPosition();
      updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
      mediaNotificationManager.update(getMediaDataFromCurrentSong(), mediaSession.getController().getPlaybackState(), mediaSession.getSessionToken());
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
      this.mediaNotificationManager = new MediaNotificationManager(this);
      mediaPlayer.start();
      updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
      mediaNotificationManager.update(getMediaDataFromCurrentSong(), mediaSession.getController().getPlaybackState(), mediaSession.getSessionToken());
    }
  }

  private void stopMedia(){
    if(mediaPlayer != null && mediaPlayer.isPlaying()){
      mediaPlayer.stop(); isMediaPlayerPrepared=false;
      updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
      mediaNotificationManager.update(getMediaDataFromCurrentSong(), mediaSession.getController().getPlaybackState(), mediaSession.getSessionToken());
    }
    if(playListModel.getCurrentSong() != null && playListModel.getCurrentSong().getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS){
      PlaytimePersistence.savePlaytime(this, mediaPlayerCurrentDataSourceUri, mediaPlayer.getCurrentPosition() / 1000);
    }
  }

  private void updatePlaybackState(int state) {
    long position = mediaPlayer.getCurrentPosition(); // Get current position after seeking

    PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setState(state, position, 1.0f)
            .setBufferedPosition(position); // Optional: to show buffered position

    // Set the actions based on the current state
    if (state == PlaybackStateCompat.STATE_PLAYING) {
      stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE |
              PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
              PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
              PlaybackStateCompat.ACTION_SEEK_TO);
    } else if (state == PlaybackStateCompat.STATE_PAUSED) {
      stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY |
              PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
              PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
              PlaybackStateCompat.ACTION_SEEK_TO);
    } else {
      stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY);
    }

    mediaSession.setPlaybackState(stateBuilder.build());
  }

  private MediaMetadataCompat getMediaDataFromCurrentSong(){
    return new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playListModel.getCurrentSong().getName())
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playListModel.getCurrentSong().getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playListModel.getCurrentSong().getAlbum())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, playListModel.getCurrentSong().getDuration_ms())
            .build();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
    if(intent.hasExtra(STARTING_INDEX)){
      startIndex = intent.getIntExtra(STARTING_INDEX,0);
    }
    String intentAction = intent.getAction();
    if(intentAction != null){
      // Update the notification to reflect play/pause changes
        switch (intentAction) {
            case Constants.ACTION.PREV_ACTION:
                Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Clicked Previous");
                prevSong();
                break;
            case Constants.ACTION.PLAY_ACTION: //toogles play,resume
                Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Clicked Play");
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    pause();
                    ((LocalBinder) iBinder).boundServiceListener.pauseSong();
                } else {
                    resume();
                    ((LocalBinder) iBinder).boundServiceListener.resumeSong();
                }
                break;
            case Constants.ACTION.NEXT_ACTION:
                Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Clicked Next");
                nextSong();
                break;
            case Constants.ACTION.STOPFOREGROUND_ACTION:
                Log.i(TAG, "Received Stop Foreground Intent");
                Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
                stopForeground(true);
                stopSelf();
                stopMedia();
                ((LocalBinder) iBinder).boundServiceListener.stopSong();
                break;
        }
    }
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    //create mediasession
    this.mediaSession = new MediaSessionCompat(this, "MediaPlayerService");
    this.mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);     // Optionally set the flags for handling media buttons
    this.mediaSession.setActive(true); // Set the session as active

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
  }

  public void nextSong() {
    playListModel.nextSong();
    initMediaPlayer();
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING); // Update to playing state if playing
    mediaNotificationManager.update(getMediaDataFromCurrentSong(), mediaSession.getController().getPlaybackState(), mediaSession.getSessionToken());
  }

  public void prevSong() {
    playListModel.prevSong();
    initMediaPlayer();
    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING); // Update to playing state if playing
    mediaNotificationManager.update(getMediaDataFromCurrentSong(), mediaSession.getController().getPlaybackState(), mediaSession.getSessionToken());
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

}
