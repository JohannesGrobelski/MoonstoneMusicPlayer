package com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment;

import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.Genre;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class Album {
  private String name;
  private String artistName;
  private List<Song> songList;

  public static Album emptyAlbum = new Album("","",new ArrayList<Song>());


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
    return Song.getDurationString(getDuration());
  }

  public int getDuration() {
    int duration = 0;
    for(Song song: songList){
      duration += song.getDuration_ms();
    }
    return duration;
  }
}
