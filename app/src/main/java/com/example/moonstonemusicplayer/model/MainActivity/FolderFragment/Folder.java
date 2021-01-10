package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

public class Folder {
  String name;
  Folder[] children_folders;
  Song[] children_songs;

  public Folder(String name, Folder[] children_folders, Song[] children_songs) {
    this.name = name;
    this.children_folders = children_folders;
    this.children_songs = children_songs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Folder[] getChildren_folders() {
    return children_folders;
  }

  public void setChildren_folders(Folder[] children_folders) {
    this.children_folders = children_folders;
  }

  public Song[] getChildren_songs() {
    return children_songs;
  }

  public void setChildren_songs(Song[] children_songs) {
    this.children_songs = children_songs;
  }

  public void print(int startingdepth){
    String offset = new String(new char[startingdepth]).replace("\0", " ");
    System.out.println(offset+"DIR: "+name);
    if(children_folders != null){
      for(Folder child: children_folders){
        if(child != null)child.print(startingdepth+2);
      }
    }
    if(children_songs != null){
      for(Song child: children_songs){
        if(child != null)System.out.println(offset+"  SONG: "+child.getTitle());
      }
    }

  }
}
