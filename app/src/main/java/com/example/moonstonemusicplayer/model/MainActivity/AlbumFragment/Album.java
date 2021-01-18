package com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class Album {
  String name;
  String artistName;
  List<Song> songList;

  public Album(String name, String artistName, List<Song> songList) {
    this.name = name;
    this.artistName = artistName;
    this.songList = songList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getArtistName() {
    return artistName;
  }

  public void setArtistName(String artistName) {
    this.artistName = artistName;
  }

  public List<Song> getSongList() {
    return songList;
  }

  public void setSongList(List<Song> songList) {
    this.songList = songList;
  }

  public String getDurationString() {
    int duration = 0;
    for(Song song: songList){
      duration += song.getDuration_ms();
    }
    return Song.getDurationString(duration);
  }
}
