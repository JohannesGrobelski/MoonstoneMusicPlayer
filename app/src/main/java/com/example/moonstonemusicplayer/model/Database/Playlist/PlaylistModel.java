package com.example.moonstonemusicplayer.model.Database.Playlist;

import com.example.moonstonemusicplayer.model.PlayListActivity.Audiofile;

import java.util.List;

public class PlaylistModel {

    private String playlistName;
    private List<Audiofile> playlistFiles;

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public List<Audiofile> getPlaylistFiles() {
        return playlistFiles;
    }

    public void setPlaylistFiles(List<Audiofile> playlistFiles) {
        this.playlistFiles = playlistFiles;
    }
}
