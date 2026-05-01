package com.example.moonstonemusicplayer.model.Database.Playlist;

import androidx.room.*;

import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import android.content.Context;

import java.util.List;
import java.util.LinkedList;
import java.util.stream.Collectors;
import timber.log.Timber;


/** 
 *  Uses {@link PlaylistDao} to access Playlist DB and 
 *  transform {@link} PlaylistEntry} Model to {@link Song}.
 */
public class DBPlaylists {
    private static DBPlaylists instance;
    private static PlaylistDao dao;

    private DBPlaylists(Context context){
        Timber.d("Unsere DataSource erzeugt den DBHelperPlaylists");
    }

    public static DBPlaylists getInstance(Context context){
        if(instance == null){
            instance = new DBPlaylists(context);
            PlaylistDBHelper db = Room.databaseBuilder(context, PlaylistDBHelper.class, "DBPlaylists").build();
            dao = db.playlistEntryDao();
        }
        return instance;
    }

    public String[] getAllPlaylistNames() {
        return dao.getAllPlaylistNames();
    }

    public List<Song> getAllFavorites(Context context){
        return dao.getAllFavoritesSongURLs().stream()
            .map(songURL -> BrowserManager.getSongFromPath(songURL))
            .collect(Collectors.toList());
    }

    public List<Song> getAllRecentlyPlayed(Context context){
        return dao.getAllRecentlyPlayedSongURLs().stream()
            .map(songURL -> BrowserManager.getSongFromPath(songURL))
            .collect(Collectors.toList());
    }

    public void deleteFromPlaylist(Song song, String playlistName){
        dao.deleteFromPlaylist(playlistName, song.getPath());
    }

    public void changePlaylistOrder(String playlistName, List<Song> songList) {
        for(int playlistIndex=0; playlistIndex<songList.size(); playlistIndex++){
            dao.updatePlaylistEntryIndex(playlistName, 
                    songList.get(playlistIndex).getPath(), 
                    playlistIndex);  
        }
    }

    public Song addToPlaylist(Context context, Song inputSong, String playlistname){
        int playlistIndex = dao.getPlaylistCount(playlistname);
        dao.insert(new PlaylistEntry(++playlistIndex, playlistname, inputSong.getPath(), 0));
        return inputSong;
    }

    public void addToFavorites(Context context,Song song){
        int playlistIndex = dao.getPlaylistCount(PlaylistDao.FAVORITES);
        dao.insert(new PlaylistEntry(++playlistIndex, PlaylistDao.FAVORITES, song.getPath(), 0));
    }

    public void removeFromFavorites(Context context, Song song){
        dao.deleteFromPlaylist(PlaylistDao.FAVORITES, song.getPath());
    }

    public boolean isInFavorites(Context context, Song song){
        return dao.isInFavorites(song.getPath());
    }

    public void addToRecentlyPlayed(Context context,Song song){
        dao.deleteFromPlaylist(PlaylistDao.RECENTLY_PLAYED, song.getPath());
        int playlistIndex = dao.getPlaylistCount(PlaylistDao.RECENTLY_PLAYED);
        dao.insert(new PlaylistEntry(++playlistIndex, PlaylistDao.RECENTLY_PLAYED, song.getPath(), 1));
    }

    public void deleteFromFavorites(Song song){
        dao.deleteFromPlaylist(PlaylistDao.FAVORITES, song.getPath());
    }

    public void deletePlaylist(Playlist playlist){
        dao.deletePlaylist(playlist.getName());
    }

    public List<Playlist> getAllPlaylists(Context context) {
        String[] playlistNames = dao.getAllPlaylistNames();
        List<Playlist> playlists = new LinkedList<Playlist>();
        for(String playlistName: playlistNames){
            List<Song> playlistEntries = dao.getAllPlaylistEntries(playlistName).stream()
            .map(songURL -> BrowserManager.getSongFromPath(songURL))
            .collect(Collectors.toList());
            playlists.add(new Playlist(playlistName, playlistEntries));
        }
        return playlists;
    }

    public Song playedSong(Context context, Song song){
        dao.incrementPlaycount(song.getPath());
        return song;
    }

    public List<Song> getMostlyPlayed() {
        return dao.getMostlyPlayedEntries(50).stream()
            .map(songURL -> BrowserManager.getSongFromPath(songURL))
            .collect(Collectors.toList());
    }
}
