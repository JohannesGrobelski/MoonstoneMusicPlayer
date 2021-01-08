package com.example.moonstonemusicplayer.model;

import android.content.Context;
import android.widget.ListView;

import com.example.moonstonemusicplayer.controller.SongListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * models a music player
 */
public class MusicPlayer {
    Context context;

    SongManager songManager;
    private int currentSongIndex;

    private List<Song> currentSongList = new ArrayList<>();


    public MusicPlayer(Context baseContext) {
        this.context = baseContext;
        songManager = new SongManager();
    }

    public void loadLocalMusic(){
        songManager.findAllAudioFiles(null);
        currentSongList.addAll(songManager.allLocaleSongs);
    }

    public Song getCurrentSong(){
        return currentSongList.get(currentSongIndex);
    }


    public int getCurrentSongIndex(){
        return this.currentSongIndex;
    }

    public void setCurrentSongIndex(int index){
        this.currentSongIndex = index;
    }

    public void prevSong(){
        currentSongIndex = (--currentSongIndex);
        if(currentSongIndex == -1)currentSongIndex = currentSongList.size()-1;
    }

    public void nextSong(){
        currentSongIndex = (++currentSongIndex)%currentSongList.size();
    }

    public List<Song> getCurrentSongList(){
        return currentSongList;
    }


}
