package com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment;

import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class Artist {
  private String name;
  private List<Album> albumList;

  public Artist(String name, List<Album> albumList) {
    this.name = name;
    this.albumList = albumList;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Album> getAlbumList() {
    return albumList;
  }

  public void setAlbumList(List<Album> albumList) {
    this.albumList = albumList;
  }

  public int getDuration() {
    int duration = 0;
    for(Album album: albumList){
      if(album != null){
        duration += album.getDuration();
      }
    }
    return duration;
  }

  public String getDurationString() {
    return Song.getDurationString(getDuration());
  }
}
