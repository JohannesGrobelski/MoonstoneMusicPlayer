package com.example.moonstonemusicplayer.model.MainActivity.GenreFragment;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class Genre {
  String name = "";
  List<Song> songList;

  public Genre(String name, List<Song> songList) {
    this.name = name;
    this.songList = songList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Song> getSongList() {
    return songList;
  }

  public void setSongList(List<Song> songList) {
    this.songList = songList;
  }
}
