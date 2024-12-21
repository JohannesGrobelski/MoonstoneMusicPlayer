/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model;

import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

/** This utility is a service to set or fetch the next song to play.
 *
 */
public class NextSongToPlayUtility {

    private static Song nextSongToPlay;
   
    /** This will 
     *
     */
   public static void setSongToPlayNext(Song nextSong){
        nextSongToPlay = nextSong; 
   }


   /** This either 
    *
    */
   public static Song getSongToPlayNext(){
        if(nextSongToPlay == null)return nextSongToPlay;
        Song returnSong = null;
        try {
            returnSong = (Song) nextSongToPlay.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        } finally {
            nextSongToPlay = null;
        }
        return returnSong;
   }

}
