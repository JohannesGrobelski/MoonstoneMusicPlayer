package com.example.moonstonemusicplayer.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * models a music player
 */
public class MusicPlayer {
    Context context;

    private int currentSongIndex;

    private DataSource dataSource;
    private List<Song> currentSongList;

    boolean shuffleModelOn = false;
    enum REPEATMODE {
        ONESONG, NONE ,ALL
    } REPEATMODE repeatmode = REPEATMODE.ALL;


    public MusicPlayer(Context baseContext) {
        this.context = baseContext;
        dataSource = new DataSource(this.context);
        currentSongList = dataSource.getAllSong();
    }

    /** loads local music and adds it to dataSource*/
    public void loadLocalMusic(){
        dataSource.deleteAllSongs(); //TODO: dont delete db but only local files
        currentSongList.clear();
        currentSongList.addAll(SongManager.findAllAudioFiles(null,null));
        for(Song song:currentSongList)dataSource.insertSong(song);
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

    public void searchSong(String searchterm){
        currentSongList.clear();
        currentSongList.addAll(dataSource.searchSongs(searchterm));
    }

    public void getAllSongs(){
        currentSongList.clear();
        currentSongList = dataSource.getAllSong();
    }

    public void toogleShuffleMode(){
        shuffleModelOn = !shuffleModelOn;
    }
}
