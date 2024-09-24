package com.example.moonstonemusicplayer.controller.PlayListActivity.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.PlayListActivity.MediaPlayerService;
import com.example.moonstonemusicplayer.view.PlayListActivity;

import java.util.Objects;

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
public class MediaNotificationManager extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE = 100;
    private static final String CHANNEL_ID = "MoonstoneMediaPlayerServiceChannelID_8941891918918941351";

    private static final String ACTION_PAUSE = "com.example.android.musicplayercodelab.pause";
    private static final String ACTION_PLAY = "com.example.android.musicplayercodelab.play";
    private static final String ACTION_NEXT = "com.example.android.musicplayercodelab.next";
    private static final String ACTION_PREV = "com.example.android.musicplayercodelab.prev";

    private final MediaPlayerService mService;
    private final NotificationManager mNotificationManager;
    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    private boolean mStarted;
    private NotificationChannel notificationChannel;
    private NotificationManager notificationManager;

    public MediaNotificationManager(MediaPlayerService service) {
        mService = service;
        createNotificationChannel(service.getApplicationContext());

        String pkg = mService.getPackageName();
        PendingIntent playIntent =
                PendingIntent.getBroadcast(
                        mService,
                        REQUEST_CODE,
                        new Intent(ACTION_PLAY).setPackage(pkg),
                        PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pauseIntent =
                PendingIntent.getBroadcast(
                        mService,
                        REQUEST_CODE,
                        new Intent(ACTION_PAUSE).setPackage(pkg),
                        PendingIntent.FLAG_IMMUTABLE);
        PendingIntent nextIntent =
                PendingIntent.getBroadcast(
                        mService,
                        REQUEST_CODE,
                        new Intent(ACTION_NEXT).setPackage(pkg),
                        PendingIntent.FLAG_IMMUTABLE);
        PendingIntent prevIntent =
                PendingIntent.getBroadcast(
                        mService,
                        REQUEST_CODE,
                        new Intent(ACTION_PREV).setPackage(pkg),
                        PendingIntent.FLAG_IMMUTABLE);

        mPlayAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_button,
                        "PLAY",
                        playIntent);
        mPauseAction =
                new NotificationCompat.Action(
                        R.drawable.ic_pause,
                        "PAUSE",
                        pauseIntent);
        mNextAction =
                new NotificationCompat.Action(
                        R.drawable.ic_next,
                        "NEXT",
                        nextIntent);
        mPrevAction =
                new NotificationCompat.Action(
                        R.drawable.ic_previous,
                        "PREVIOUS",
                        prevIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_PREV);

        mService.registerReceiver(this, filter);

        mNotificationManager =
                (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();
    }

    private void createNotificationChannel(Context context){
        //Create a notification channel
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;

        this.notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        notificationChannel.setDescription(description);

        notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (Objects.requireNonNull(action)) {
            case ACTION_PAUSE:
                mService.pause();
                break;
            case ACTION_PLAY:
                mService.resume();
                break;
            case ACTION_NEXT:
                mService.nextSong();
                break;
            case ACTION_PREV:
                mService.prevSong();
                break;
        }
    }

    public void update(
            MediaMetadataCompat metadata,
            PlaybackStateCompat state,
            MediaSessionCompat.Token token) {
        if (state == null
                || state.getState() == PlaybackStateCompat.STATE_STOPPED
                || state.getState() == PlaybackStateCompat.STATE_NONE) {
            mService.stopForeground(true);
            try {
                mService.unregisterReceiver(this);
            } catch (IllegalArgumentException ex) {
                // ignore receiver not registered
            }
            mService.stopSelf();
            return;
        }
        if (metadata == null) {
            return;
        }
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        MediaDescriptionCompat description = metadata.getDescription();

        notificationBuilder
                .setStyle(
                        new androidx.media.app.NotificationCompat.MediaStyle()
                                .setMediaSession(token)
                                .setShowActionsInCompactView(0, 1, 2))
                .setColor(
                        mService.getApplication().getResources().getColor(R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_moonstonemusicplayerlogo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                //.setLargeIcon(MusicLibrary.getAlbumBitmap(mService, description.getMediaId()))
                .setOngoing(isPlaying)
                .setWhen(isPlaying ? System.currentTimeMillis() - state.getPosition() : 0)
                .setShowWhen(isPlaying)
                .setUsesChronometer(isPlaying);

        // If skip to next action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            notificationBuilder.addAction(mPrevAction);
        }

        notificationBuilder.addAction(isPlaying ? mPauseAction : mPlayAction);

        // If skip to prev action is enabled
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            notificationBuilder.addAction(mNextAction);
        }

        Notification notification = notificationBuilder.build();

        if (isPlaying && !mStarted) {
            mService.startService(new Intent(mService.getApplicationContext(), MediaPlayerService.class));
            mService.startForeground(NOTIFICATION_ID, notification);
            mStarted = true;
        } else {
            if (!isPlaying) {
                mService.stopForeground(false);
                mStarted = false;
            }
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, PlayListActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}