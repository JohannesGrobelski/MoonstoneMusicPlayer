package com.example.moonstonemusicplayer.model.PlayListActivity;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

public class Song extends Audiofile implements Cloneable{
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

  public String getSongIdentifier(){
    return getIdentifier(this.name);
  }

  public static String getIdentifier(String fileName){

    // Remove the file extension
    int lastIndexOfDot = fileName.lastIndexOf('.');
    if (lastIndexOfDot > 0) {
      fileName = fileName.substring(0, lastIndexOfDot);
    }

    // Replace spaces with underscores or hyphens (as per your preference)
    fileName = fileName.replaceAll("\\s+", "_");

    // Remove any non-alphanumeric characters except underscores or hyphens
    fileName = fileName.replaceAll("[^a-zA-Z0-9_-]", "");

    // Optionally, you could also convert the filename to lowercase for consistency
    fileName = fileName.toLowerCase();

    return fileName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    // Returning a clone of the current object
    return super.clone();
  }

}
