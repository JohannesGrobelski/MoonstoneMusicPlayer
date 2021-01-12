package com.example.moonstonemusicplayer.model.PlayListActivity;

import java.util.List;

import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.NONE;
import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.ONESONG;
import static com.example.moonstonemusicplayer.model.PlayListActivity.PlayListModel.REPEATMODE.ALL;


public class PlayListModel {
  private List<Song> playlist;
  private int currentSongIndex = 0;
  boolean shuffleModelOn = false;
  public enum REPEATMODE {
    NONE, ALL, ONESONG;
  };
  public REPEATMODE repeatmode = NONE;

  public PlayListModel(List<Song> playList) {
    this.playlist = playList;
  }

  public void setCurrentSong(Song song) {
    for(int i=0; i<playlist.size(); i++){
      if(song.equals(playlist.get(i)))currentSongIndex = i;
    }
  }


  public Song getCurrentSong(){
    return playlist.get(currentSongIndex);
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

}
