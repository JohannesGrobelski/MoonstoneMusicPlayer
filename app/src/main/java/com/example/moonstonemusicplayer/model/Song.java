package com.example.moonstonemusicplayer.model;

public class Song {
  String title;
  String artist;
  String genre = "";
  long duration_ms = 0;
  String URI = "";
  String lyrics = "";
  String meaning = "";

  public Song(String title, String artist, String URI, long duration_ms) {
    this.title = title;
    this.artist = artist;
    this.URI = URI;
    this.duration_ms = duration_ms;
  }

  boolean isFavourite = false;



  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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
    int duration_seconds = -1;
    duration_seconds = (int) duration_ms / 1000;
    int hours = duration_seconds / 3600;
    int minutes = (duration_seconds - (hours * 3600)) / 60;
    int seconds = (duration_seconds - (minutes * 60)) % 60;
    if(duration_seconds < 3600) return minutes+":"+seconds;
    else return hours+":"+minutes+":"+seconds;
  }

  public void setDuration_ms(long duration_ms) {
    this.duration_ms = duration_ms;
  }

  public String getURI() {
    return URI;
  }

  public void setURI(String URI) {
    this.URI = URI;
  }

  public String getLyrics() {
    return lyrics;
  }

  public void setLyrics(String lyrics) {
    this.lyrics = lyrics;
  }

  public String getMeaning() {
    return meaning;
  }

  public void setMeaning(String meaning) {
    this.meaning = meaning;
  }

  public boolean isFavourite() {
    return isFavourite;
  }

  public void setFavourite(boolean favourite) {
    isFavourite = favourite;
  }



}
