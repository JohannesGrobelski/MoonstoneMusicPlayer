/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class Playlist {
  String name = "";
  List<Song> playlist;

  public Playlist(String name, List<Song> playlist) {
    this.name = name;
    this.playlist = playlist;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Song> getPlaylist() {
    return playlist;
  }

  public void setPlaylist(List<Song> playlist) {
    this.playlist = playlist;
  }
}
