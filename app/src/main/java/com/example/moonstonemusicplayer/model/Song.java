package com.example.moonstonemusicplayer.model;

import android.util.Log;

import java.io.Serializable;
import java.util.logging.Logger;

public class Song {
  int ID = -1;
  String title;
  String artist = "unknown Artist";
  String URI = "";
  int duration_ms = 0;
  int lastPosition = 0;
  String genre = "";
  String lyrics = "";
  String meaning = "";


  /** "normal" constructor*/
  public Song(String title, String artist, String URI, int duration_ms) {
    this.title = title;
    this.artist = artist;
    this.URI = URI;
    this.duration_ms = duration_ms;
  }

  /** Constructor for DB*/
  public Song(int ID, String title, String artist, String URI, int duration_ms, int lastPosition, String genre, String lyrics, String meaning) {
    this.ID = ID;
    this.title = title;
    this.artist = artist;
    this.URI = URI;
    this.duration_ms = duration_ms;
    this.lastPosition = lastPosition;
    this.genre = genre;
    this.lyrics = lyrics;
    this.meaning = meaning;
  }


  public long getLastPosition() { return lastPosition;}

  public void setLastPosition(int lastPosition) {this.lastPosition = lastPosition;}

  public int getID() {
    return ID;
  }

  public void setID(int ID) {
    this.ID = ID;
  }

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
}
