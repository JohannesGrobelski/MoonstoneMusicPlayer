package com.example.moonstonemusicplayer.controller.PlayListActivity;

import androidx.core.app.NotificationCompat.Builder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class SongNotification {
  public static final String NOTIFICATION_ORDER = "PAUSE";
  public static String CHANNEL_ID = "Moonstone Media Player";

  Context context;
  NotificationManager notificationManager;

  public SongNotification(Context context){
    this.notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
    this.context = context;
  }


  public void buildNotification(Song song){
    Log.d("SongNotification",song.getName());

    // Get the layouts to use in the custom notification
    RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
    //RemoteViews notificationLayoutExpanded = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

    notificationLayout.setTextViewText(R.id.notification_tv_name,song.getName());
    //icon = context.getResources().getIdentifier("ic_action_refresh_"+choosenTheme, "drawable", context.getPackageName());
    notificationLayout.setInt(R.id.notification_btn_play_pause, "setBackgroundResource", R.drawable.ic_pause_24);

    // Apply the layouts to the notification
    Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_music)
        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
        .setCustomContentView(notificationLayout)
        //.setCustomBigContentView(notificationLayoutExpanded)

        .setContentTitle(song.getName())
        .setContentText(song.getArtist())
        .setAutoCancel(true)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setPriority(Notification.PRIORITY_MAX);

    // === Removed some obsoletes
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
    {
      NotificationChannel channel = new NotificationChannel(
          CHANNEL_ID,
          song.getName(),
          NotificationManager.IMPORTANCE_HIGH);
      notificationManager.createNotificationChannel(channel);
      notificationBuilder.setChannelId(CHANNEL_ID);
    }

    setNotificationIntents(notificationLayout,notificationBuilder);

    notificationManager.notify(1, notificationBuilder.build());
  }


  private void setNotificationIntents(RemoteViews notificationLayout, Builder notificationBuilder ){

    Intent intent_pause = new Intent(MediaPlayerService.ACTION_NOTIFICATION_ORDER);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 7, intent_pause, 0);
    context.sendBroadcast(intent_pause);

    notificationLayout.setOnClickPendingIntent(R.id.notification_btn_play_pause,
        PendingIntent.getBroadcast(context, 0, intent_pause, 0));

    notificationBuilder.setContentIntent(pendingIntent);
  }
}
