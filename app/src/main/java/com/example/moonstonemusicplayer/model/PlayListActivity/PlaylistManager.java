/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.PlayListActivity;

import android.content.Context;

import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//import com.example.moonstonemusicplayer.model.Database.DBSonglists;


/**
 * Holds all Songs available to PlaylistActivity.
 * Is only used by the MainActivity.
 */
public class PlaylistManager {
    private final Context context;


    private List<File> playList; //the songs to be played
    private List<File> displayedSongList = new ArrayList<>(); //the songs to be displayed by

    public PlaylistManager(Context baseContext, File[] playlist) {
        this.context = baseContext;

        playList = new ArrayList<>(Arrays.asList(playlist));
        displayedSongList = new ArrayList<>(Arrays.asList(playlist));
    }


    /** loads local music and adds it to DataSourceSingleton.getInstance(context)
    public void loadLocalMusic(){
        deleteAllSongs();//TODO: dont delete db but only local files
        File[] externalFileDirs = context.getExternalMediaDirs(); //getExternalMediaDirs actually does get both internal and external sdcards
        DataSourceSingleton.getInstance(context).insertSongList(LocalSongLoader.findAllAudioFilesInDir(externalFileDirs));
        playList.addAll(DataSourceSingleton.getInstance(context).getAllSong(60000));
        displayedSongList.clear();
        displayedSongList.addAll(playList);
    }*/

    public List<File> getPlayList(){
        return this.playList;
    }

    public List<File> getDisplayedSongList(){return this.displayedSongList;}


    public void searchSong(String searchterm){
        this.displayedSongList.clear();
        for(File song: playList) {
            if (song.getName().toLowerCase().contains(searchterm.toLowerCase()))
                displayedSongList.add(song);
        }
        /*
        intersectPlaylist(DataSourceSingleton.getInstance(context).searchSongs(searchterm));
         */
        //displayedSongList.addAll(playList);
    }


    /*
    public void sortByTitle(){
        Collections.sort(displayedSongList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return (o1.getName().compareTo(o2.getName()));
            }
        });
    }

    public void sortByArtist(){
        Collections.sort(displayedSongList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return (o1.getArtist().compareTo(o2.getArtist()));
            }
        });
    }

    public void sortByGenre(){
        Collections.sort(displayedSongList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return (o1.getGenre().compareTo(o2.getGenre()));
            }
        });
    }

     public void sortByDuration() {
        Collections.sort(displayedSongList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return (int) (o1.getDuration_ms() - o2.getDuration_ms());
            }
        });
    }

    */

    public void reverseList(){
        Collections.reverse(displayedSongList);
    }

    public void deleteAllSongs(){
        //DBSonglists.getInstance(context).deleteAllSongs();
        playList.clear();
        displayedSongList.clear();
    }

    public void updatePlaylist(Playlist updatedPlaylist) {
        this.displayedSongList = updatedPlaylist.getPlaylist().stream().map(BrowserManager::getFileFromSong).collect(Collectors.toList());
        this.playList = updatedPlaylist.getPlaylist().stream().map(BrowserManager::getFileFromSong).collect(Collectors.toList());
    }


    /*
    * public void intersectPlaylist(List<Song> input){
        input.retainAll(playList);
        playList.clear();
        playList.addAll(input);
    }
    * */

}
