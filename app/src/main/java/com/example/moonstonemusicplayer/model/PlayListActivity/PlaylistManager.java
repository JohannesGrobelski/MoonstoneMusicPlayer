package com.example.moonstonemusicplayer.model.PlayListActivity;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.example.moonstonemusicplayer.model.Database.DBHelper;
import com.example.moonstonemusicplayer.model.Database.DataSourceSingleton;


/**
 * Holds all Songs available to PlaylistActivity.
 * Is only used by the MainActivity.
 */
public class PlaylistManager {
    private Context context;


    private List<Song> playList; //the songs to be played
    private List<Song> displayedSongList = new ArrayList<>(); //the songs to be displayed by

    public PlaylistManager(Context baseContext, Song[] playlist) {
        this.context = baseContext;

        playList = Arrays.asList(playlist);
        displayedSongList = Arrays.asList(playlist);
    }


    /** loads local music and adds it to DataSourceSingleton.getInstance(context)
    public void loadLocalMusic(){
        deleteAllSongs();//TODO: dont delete db but only local files
        File[] externalFileDirs = context.getExternalMediaDirs(); //getExternalMediaDirs actually does get both internal and external sdcards
        DataSourceSingleton.getInstance(context).insertSongList(LocalSongLoader.findAllAudioFiles(externalFileDirs));
        playList.addAll(DataSourceSingleton.getInstance(context).getAllSong(60000));
        displayedSongList.clear();
        displayedSongList.addAll(playList);
    }*/

    public List<Song> getPlayList(){
        return this.playList;
    }

    public List<Song> getDisplayedSongList(){return this.displayedSongList;}


    public void searchSong(String searchterm){
        displayedSongList.clear();
        intersectPlaylist(DataSourceSingleton.getInstance(context).searchSongs(searchterm));
        displayedSongList.addAll(playList);
    }


    public void sortByTitle(){
        displayedSongList.clear();
        intersectPlaylist(DataSourceSingleton.getInstance(context).sortBy(DBHelper.COLUMN_TITLE,"ASC"));
        displayedSongList.addAll(playList);
    }

    public void sortByArtist(){
        displayedSongList.clear();
        intersectPlaylist(DataSourceSingleton.getInstance(context).sortBy(DBHelper.COLUMN_ARTIST,"ASC"));
        displayedSongList.addAll(playList);
    }

    public void sortByGenre(){
        displayedSongList.clear();
        intersectPlaylist(DataSourceSingleton.getInstance(context).sortBy(DBHelper.COLUMN_GENRE,"ASC"));
        displayedSongList.addAll(playList);
    }

    public void reverseList(){
        Collections.reverse(displayedSongList);
    }

    public void deleteAllSongs(){
        DataSourceSingleton.getInstance(context).deleteAllSongs();
        playList.clear();
        displayedSongList.clear();
    }

    public void intersectPlaylist(List<Song> input){
        input.retainAll(playList);
        playList.clear();
        playList.addAll(input);
    }
}
