package com.example.moonstonemusicplayer.model;

public class Playable {

  String name;
  String URI;

  public Playable(String name, String URI) {
    this.name = name;
    this.URI = URI;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getURI() {
    return URI;
  }

  public void setURI(String URI) {
    this.URI = URI;
  }
}
