package com.example.libfolder;

import com.example.libfolder.Song;

import java.util.ArrayList;
import java.util.List;

public class Folder {
  private static final boolean DEBUG = false;
  private static final String TAG = Folder.class.getSimpleName();

  Folder parent;
  String name;
  Folder[] children_folders = new Folder[0];
  Song[] children_songs = new Song[0];

  public Folder(String name, Folder parent, Folder[] children_folders, Song[] children_songs) {
    this.parent = parent;
    this.name = name;
    this.children_folders = children_folders;
    this.children_songs = children_songs;
  }

  public static void main(String[] a){
    Song x = new Song("x",null,null,0);
    Song y = new Song("y",null,null,1);
    Song z = new Song("z",null,null,2);

    Folder music = new Folder("Music",null,null,new Song[]{x,y,z});
    Folder zero = new Folder("0",null,new Folder[]{music},null);
    System.out.println(zero.toString());
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
      if(DEBUG)System.out.println("Folder "+name+" has childfolder null");
    }
    if(children_songs != null){
      for(Song child: children_songs){
        if(child != null)input += offset+"  SONG: "+child.getTitle()+"\n";
      }
      if(children_songs.length==0)if(DEBUG)System.out.println("Folder "+name+" has no children songs");
    } else {
      if(DEBUG)System.out.println("Folder "+name+" has childsong null");
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
    System.out.println("setParentBelow: "+this.name);
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
