package com.example.moonstonemusicplayer.model.PlayListActivity;

public class Audiobook extends Audiofile{
  public static final int AUDIOBOOK_CUTOFF_MS = 15 * 60 * 1000; //every audiofile that is over 15 minutes long is suggested to be an audiobook

  String name;
  String path;
  String artist = "unknown Artist";
  String album = "";
  String genre = "";
  int duration_ms = 0;
  String lyrics = "";

  public Audiobook(String path, String name, String artist, String album, String genre, int duration_ms, String lyrics) {
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
