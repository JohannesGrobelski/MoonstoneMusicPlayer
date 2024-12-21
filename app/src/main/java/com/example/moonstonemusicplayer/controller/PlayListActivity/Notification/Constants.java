/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity.Notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.moonstonemusicplayer.R;

public class Constants {
  public interface ACTION {
    String MAIN_ACTION = "com.example.moonstonemusicplayer.action.main";
    String INIT_ACTION = "com.example.moonstonemusicplayer.action.init";
    String PREV_ACTION = "com.example.moonstonemusicplayer.action.prev";
    String PLAY_ACTION = "com.example.moonstonemusicplayer.action.play";
    String NEXT_ACTION = "com.example.moonstonemusicplayer.action.next";
    String STARTFOREGROUND_ACTION = "com.example.moonstonemusicplayer.action.startforeground";
    String STOPFOREGROUND_ACTION = "com.example.moonstonemusicplayer.action.stopforeground";
  }

  public interface NOTIFICATION_ID {
    int FOREGROUND_SERVICE = 101;
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
