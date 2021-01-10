package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class PlayList {
  String name = "";
  List<Song> playlist;

  public PlayList(String name, List<Song> playlist) {
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
