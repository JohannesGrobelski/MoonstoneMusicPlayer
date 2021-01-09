package com.example.moonstonemusicplayer.model;

import android.content.Context;

import java.util.List;

import static com.example.moonstonemusicplayer.model.MusicPlayer.REPEATMODE.ALL;
import static com.example.moonstonemusicplayer.model.MusicPlayer.REPEATMODE.NONE;
import static com.example.moonstonemusicplayer.model.MusicPlayer.REPEATMODE.ONESONG;

/**
 * models a music player
 */
public class MusicPlayer {
    Context context;

    private int currentSongIndex;

    private DataSource dataSource;
    private List<Song> currentSongList;

    boolean shuffleModelOn = false;
    public enum REPEATMODE {
        NONE, ALL, ONESONG;
    } REPEATMODE repeatmode = NONE;


    public MusicPlayer(Context baseContext) {
        this.context = baseContext;
        dataSource = new DataSource(this.context);
        currentSongList = dataSource.getAllSong();
    }

    /** loads local music and adds it to dataSource*/
    public void loadLocalMusic(){
        deleteAllSongs();//TODO: dont delete db but only local files
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
        if(currentSongList.size() == 1)return;
        if(shuffleModelOn){
            int previousSong = currentSongIndex;
            while(currentSongIndex == previousSong)currentSongIndex = (int) (Math.random()*currentSongList.size());
        } else {
            currentSongIndex = (--currentSongIndex);
            if(currentSongIndex == -1)currentSongIndex = currentSongList.size()-1;
        }
    }

    public void nextSong(){
        if(currentSongList.size() == 1)return;
        if(shuffleModelOn){
            int previousSong = currentSongIndex;
            while(currentSongIndex == previousSong)currentSongIndex = (int) (Math.random()*currentSongList.size());
        } else {
            currentSongIndex = (++currentSongIndex)%currentSongList.size();
        }
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


    public void deleteAllSongs(){
        dataSource.deleteAllSongs();
        currentSongList.clear();
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
