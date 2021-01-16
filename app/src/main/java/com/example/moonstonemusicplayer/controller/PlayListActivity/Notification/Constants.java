package com.example.moonstonemusicplayer.controller.PlayListActivity.Notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.moonstonemusicplayer.R;

public class Constants {
  public interface ACTION {
    public static String MAIN_ACTION = "com.example.moonstonemusicplayer.action.main";
    public static String INIT_ACTION = "com.example.moonstonemusicplayer.action.init";
    public static String PREV_ACTION = "com.example.moonstonemusicplayer.action.prev";
    public static String PLAY_ACTION = "com.example.moonstonemusicplayer.action.play";
    public static String NEXT_ACTION = "com.example.moonstonemusicplayer.action.next";
    public static String STARTFOREGROUND_ACTION = "com.example.moonstonemusicplayer.action.startforeground";
    public static String STOPFOREGROUND_ACTION = "com.example.moonstonemusicplayer.action.stopforeground";
  }

  public interface NOTIFICATION_ID {
    public static int FOREGROUND_SERVICE = 101;
  }
  public static Bitmap getDefaultAlbumArt(Context context) {
    Bitmap bm = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    try {
      bm = BitmapFactory.decodeResource(context.getResources(),
          R.drawable.ic_moonstonemusicplayerlogo, options);
    } catch (Error ee) {
    } catch (Exception e) {
    }
    return bm;
  }
}