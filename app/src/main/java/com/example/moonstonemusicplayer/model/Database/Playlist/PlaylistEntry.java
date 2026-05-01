package com.example.moonstonemusicplayer.model.Database.Playlist;

import androidx.room.*;

@Entity
public class PlaylistEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "PlaylistIndex")
    public int playlistIndex;

    @ColumnInfo(name = "PlaylistName")
    public String playlistName;

    @ColumnInfo(name = "SongURL")
    public String songURL;

    @ColumnInfo(name = "Playcount")
    public int playcount;

    public PlaylistEntry(int playlistIndex, String playlistName, String songURL, int playcount){
        this.playlistIndex = playlistIndex;
        this.playlistName = playlistName;
        this.songURL = songURL;
        this.playcount = playcount;
    }
}
