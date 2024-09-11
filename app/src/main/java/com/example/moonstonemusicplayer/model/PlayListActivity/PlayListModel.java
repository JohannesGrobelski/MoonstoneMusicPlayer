package com.example.moonstonemusicplayer.model.PlayListActivity;

import java.io.File;
import java.util.List;

import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.NONE;
import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.ONESONG;
import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.ALL;
import static com.example.moonstonemusicplayer.model.PlayListActivity.Song.getIdentifier;

import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;

public class PlayListModel {
  private final List<File> playlist;
  private int currentSongIndex = 0;
  boolean shuffleModelOn = false;
  public enum REPEATMODE {
    NONE, ALL, ONESONG
  }

  public REPEATMODE repeatmode = ALL;

  public PlayListModel(List<File> playList) {
    this.playlist = playList;
  }

  public void setCurrentSong(File song) {
    for(int i=0; i<playlist.size(); i++){
      if(song.equals(playlist.get(i)))currentSongIndex = i;
    }
  }

  public Song getCurrentSong(){
    return BrowserManager.getSongFromAudioFile(playlist.get(currentSongIndex));
  }

  public File getCurrentSongFile(){
    return playlist.get(currentSongIndex);
  }

  public String getFileId(){
    return getIdentifier(getCurrentSongFile().getName());
  }

  public void setCurrentSong(int index) {
    this.currentSongIndex = index;
  }


  public int getCurrentSongIndex(){
    return currentSongIndex;
  }

  public void prevSong(){
    if(playlist.size() <= 1 || repeatmode.equals(ONESONG))return;
    if(shuffleModelOn){
      int previousSong = currentSongIndex;
      while(currentSongIndex == previousSong)currentSongIndex = (int) (Math.random()* playlist.size());
    } else {
      if(repeatmode.equals(ALL)){
        currentSongIndex = (--currentSongIndex);
        if(currentSongIndex == -1)currentSongIndex = playlist.size()-1;
      }
    }
  }

  public void nextSong(){
    if(playlist.size() <= 1 || repeatmode.equals(ONESONG))return;
    if(shuffleModelOn){
      int previousSong = currentSongIndex;
      while(currentSongIndex == previousSong)currentSongIndex = (int) (Math.random()* playlist.size());
    } else {
      if(repeatmode.equals(ALL))currentSongIndex = (++currentSongIndex)% playlist.size();
    }
  }


  public boolean toogleShuffleMode(){
    shuffleModelOn = !shuffleModelOn; return shuffleModelOn;
  }

  public REPEATMODE nextRepeatMode(){
    switch (repeatmode){
      case NONE: {repeatmode = ALL; break;}
      case ALL: {repeatmode = ONESONG; break;}
      case ONESONG: {repeatmode = NONE; break;}
    }
    return repeatmode;
  }

  public List<File> getPlaylist() {
    return playlist;
  }
}
