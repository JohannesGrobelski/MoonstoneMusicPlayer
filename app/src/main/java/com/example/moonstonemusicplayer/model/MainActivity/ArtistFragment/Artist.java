package com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment;

import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class Artist {
  private String name;
  private List<Song> songList;

  public Artist(String name, List<Song> albumList) {
    this.name = name;
    this.songList = albumList;
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

  public int getDuration() {
    int duration = 0;
    for(Song album: songList){
      if(album != null){
        duration += album.getDuration_ms();
      }
    }
    return duration;
  }

  public String getDurationString() {
    return Song.getDurationString(getDuration());
  }
}
