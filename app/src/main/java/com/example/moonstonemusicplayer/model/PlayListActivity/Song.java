package com.example.moonstonemusicplayer.model.PlayListActivity;

import android.net.Uri;
import android.util.Log;

import java.io.File;

public class Song extends Audiofile {
  String name;
  String path;
  String artist = "unknown Artist";
  String album = "";
  String genre = "";
  int duration_ms = 0;
  String lyrics = "";

  public Song(String path, String name, String artist, String album, String genre, int duration_ms, String lyrics) {
    super(path, name, artist, album, genre, duration_ms, lyrics);
    this.path = path;
    this.name = name;
    this.artist = artist;
    this.album = album;
    this.genre = genre;
    this.duration_ms = duration_ms;
    this.lyrics = lyrics;
  }

}
