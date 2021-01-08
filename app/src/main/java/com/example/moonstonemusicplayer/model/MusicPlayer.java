package com.example.moonstonemusicplayer.model;

import android.content.Context;
import android.widget.ListView;

import com.example.moonstonemusicplayer.controller.SongListAdapter;
import com.example.moonstonemusicplayer.controller.SongManager;

public class MusicPlayer {
    Context context;

    SongManager songManager;
    private int currentSongIndex;



    public MusicPlayer(Context baseContext) {
        this.context = baseContext;
    }

    public Song getCurrentSong(){
        return songManager.getSong(currentSongIndex);
    }

    public void bindSongListAdapterToSongListView(ListView lv_songlist){
        ///TODO: move to songmanager
        songListAdapter = new SongListAdapter(context,songManager.getSongList(),currentSongIndex);
        lv_songlist.setAdapter(songListAdapter);
    }

    public void setCurrentSongIndex(int index){
        this.currentSongIndex = index;
    }

    private void prevSong(){
        currentSongIndex = (--currentSongIndex);
        if(currentSongIndex == -1)currentSongIndex = musicPlayer.getSongCount()-1;
        playAudio();
    }

    private void nextSong(){
        currentSongIndex = (++currentSongIndex)%musicPlayer.getSongCount();
        playAudio();
    }
}
