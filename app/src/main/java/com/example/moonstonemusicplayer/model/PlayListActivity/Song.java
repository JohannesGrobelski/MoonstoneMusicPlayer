package com.example.moonstonemusicplayer.model.PlayListActivity;

import com.example.moonstonemusicplayer.model.Playable;

public class Song extends Playable {
  String artist = "unknown Artist";
  String album = "";
  String genre = "";
  int duration_ms = 0;
  String lyrics = "";


  /** "normal" constructor*/
  public Song(String name, String artist, String URI, int duration_ms) {
    super(name,URI);
    this.artist = artist;
    this.duration_ms = duration_ms;
  }


  public Song(String URI, String name, String artist, String album, String genre, int duration_ms, String lyrics) {
    super(name, URI);
    this.artist = artist;
    this.album = album;
    this.genre = genre;
    this.duration_ms = duration_ms;
    this.lyrics = lyrics;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public long getDuration_ms() {
    return duration_ms;
  }

  public static String getDurationString(Integer duration_ms){
    int duration_seconds = duration_ms / 1000;
    int hours = duration_seconds / 3600;
    int minutes = (duration_seconds - (hours * 3600)) / 60;
    int seconds = (duration_seconds - (minutes * 60)) % 60;

    String secondsString = String.valueOf(seconds);
    if(seconds < 10) secondsString = "0"+seconds;

    String minutesString = String.valueOf(minutes);
    if(minutes < 10) minutesString = "0"+minutesString;

    String hoursString = String.valueOf(hours);
    if(hours < 10) hoursString = "0"+hoursString;

    if(duration_seconds < 3600) return minutesString+":"+secondsString;
    else return hoursString+":"+minutesString+":"+secondsString;
  }

  public String getDurationString(){
    return Song.getDurationString(duration_ms);
  }

  public void setDuration_ms(int duration_ms) {
    this.duration_ms = duration_ms;
  }


  public String getLyrics() {
    return lyrics;
  }

  public void setLyrics(String lyrics) {
    this.lyrics = lyrics;
  }

  public String getAlbum() {
    return album;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  @Override
  public String toString() {
    return "Song{" +
        "artist='" + artist + '\'' +
        ", album='" + album + '\'' +
        ", genre='" + genre + '\'' +
        ", duration_ms=" + duration_ms +
        ", lyrics='" + lyrics + '\'' +
        "} " + super.toString();
  }
}
