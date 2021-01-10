package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class Folder {
  Folder parent;
  String name;
  Folder[] children_folders;
  Song[] children_songs;

  public Folder(String name, Folder parent, Folder[] children_folders, Song[] children_songs) {
    this.parent = parent;
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

  public Folder getParent() {
    return parent;
  }

  public void setParent(Folder parent) {
    this.parent = parent;
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

  public String[] getAllChildrenAsStrings(){
    List<String> childrenString = new ArrayList<>();
    if(children_folders != null){
      for(Folder child: children_folders){
         if(child != null)childrenString.add("DIR: "+child.name);
      }
    }
    if(children_songs != null){
      for(Song child: children_songs){
        if(child != null)childrenString.add("SONG: "+child.getTitle());
      }
    }
    return childrenString.toArray(new String[childrenString.size()]);
  }

  public void setParentsBelow(){
    Log.d("Folder","setParentBelow: "+this.name);
    if(children_folders != null){
      for(Folder child: children_folders){
        if(child != null){
          child.setParent(this);
          child.setParentsBelow();
        }
      }
    }
  }
}
