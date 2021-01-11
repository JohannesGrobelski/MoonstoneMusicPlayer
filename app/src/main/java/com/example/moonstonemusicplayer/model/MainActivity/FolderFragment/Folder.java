package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Folder {
  private static final boolean DEBUG = false;
  private static final String TAG = Folder.class.getSimpleName();

  Folder parent;
  String name;
  Folder[] children_folders = null;
  Song[] children_songs = null;

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

  public String toString(){return "\n"+toStringStep(0,"");}

  private String toStringStep(int starting_depth, String input){
    if(DEBUG)System.out.println("toString: "+input);
    String offset = new String(new char[starting_depth]).replace("\0", " ");
    input = offset+"DIR: "+name+"\n";
    if(children_folders != null){
      for(Folder child: children_folders){
        if(child != null){
          if(DEBUG)System.out.println("Folder "+name+" has child folder "+child.name);
          input += child.toStringStep(starting_depth+2,input);
        } else {
          if(DEBUG)System.out.println("Folder "+name+" has one null children folder");
        }
      }
      if(children_folders.length==0)if(DEBUG)System.out.println("Folder "+name+" has no children folders");
    } else {
      if(DEBUG)System.out.println("Folder "+name+" has children_folders = null");
    }
    if(children_songs != null){
      for(Song child: children_songs){
        if(child != null)input += offset+"  SONG: "+child.getTitle()+"\n";
      }
      if(children_songs.length==0)if(DEBUG)System.out.println("Folder "+name+" has no children songs");
    } else {
      if(DEBUG)System.out.println("Folder "+name+" has children_songs = null");
    }
    return input;
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
    if(children_folders != null){
      //remove all null children
      for(Folder child: children_folders){
        child.setParent(this);
        child.setParentsBelow();
      }
    }
  }

}
