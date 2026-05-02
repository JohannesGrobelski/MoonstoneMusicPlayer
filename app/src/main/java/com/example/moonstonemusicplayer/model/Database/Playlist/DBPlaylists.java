package com.example.moonstonemusicplayer.model.Database.Playlist;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.*;

import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import android.content.Context;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;
import timber.log.Timber;
import java.util.concurrent.Executors;
import androidx.lifecycle.MediatorLiveData;

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

    public LiveData<String[]> getAllPlaylistNames() {
        return dao.getAllPlaylistNames();
    }

    public LiveData<List<Song>> getAllFavorites(Context context){
        return Transformations.map(
                    dao.getAllFavoritesSongURLs(),
                    songURLs -> songURLs.stream()
                        .map(songURL -> BrowserManager.getSongFromPath(songURL))
                        .collect(Collectors.toList())
                );
    }

    public LiveData<List<Song>> getAllRecentlyPlayed(Context context){
        return Transformations.map(
                    dao.getAllRecentlyPlayedSongURLs(),
                    songURLs -> songURLs.stream()
                        .map(songURL -> BrowserManager.getSongFromPath(songURL))
                        .collect(Collectors.toList())
                );
    }

    public void deleteFromPlaylist(Song song, String playlistName){
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deleteFromPlaylist(playlistName, song.getPath());
        });
    }

    public void changePlaylistOrder(String playlistName, List<Song> songList) {
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            for(int playlistIndex=0; playlistIndex<songList.size(); playlistIndex++){
                dao.updatePlaylistEntryIndex(playlistName, 
                        songList.get(playlistIndex).getPath(), 
                        playlistIndex);  
            }
        });
    }

    public void addToPlaylist(Context context, Song inputSong, String playlistname){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            int playlistIndex = dao.getPlaylistCount(playlistname);
            dao.insert(new PlaylistEntry(++playlistIndex, playlistname, inputSong.getPath(), 0));
        });
    }

    public void addToFavorites(Context context,Song song){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            int playlistIndex = dao.getPlaylistCount(PlaylistDao.FAVORITES);
            dao.insert(new PlaylistEntry(++playlistIndex, PlaylistDao.FAVORITES, song.getPath(), 0));
        });
    }

    public void removeFromFavorites(Context context, Song song){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deleteFromPlaylist(PlaylistDao.FAVORITES, song.getPath());
        });
    }

    public void addToRecentlyPlayed(Context context,Song song){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deleteFromPlaylist(PlaylistDao.RECENTLY_PLAYED, song.getPath());
            int playlistIndex = dao.getPlaylistCount(PlaylistDao.RECENTLY_PLAYED);
            dao.insert(new PlaylistEntry(++playlistIndex, PlaylistDao.RECENTLY_PLAYED, song.getPath(), 1));
        });
    }

    public void deleteFromFavorites(Song song){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deleteFromPlaylist(PlaylistDao.FAVORITES, song.getPath());
        });
    }

    public void deletePlaylist(Playlist playlist){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.deletePlaylist(playlist.getName());
        });
    }

    public LiveData<List<Playlist>> getAllPlaylists(Context context) {
        //NOTE: LiveData Objekt das andere LiveData Objekte beobachtet (mit postValue)
        MediatorLiveData<List<Playlist>> result = new MediatorLiveData<>();
        result.addSource(dao.getAllPlaylistNames(), playlistNames -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<Playlist> playlists = Arrays.asList(playlistNames).stream()
                        .map(playlistName -> new Playlist(playlistName,
                                dao.getAllPlaylistEntries(playlistName).stream()
                                    .map(songURL -> BrowserManager.getSongFromPath(songURL))
                                    .collect(Collectors.toList())
                        ))
                        .collect(Collectors.toList());
                    result.postValue(playlists);
                });
        });
        return result;
    }

    public void playedSong(Context context, Song song){
        //NOTE: Room-Datenbankoperationen dürfen nicht auf dem Main Thread laufen - Ausnahme LiveData-Queries
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.incrementPlaycount(song.getPath());
        });
    }

    public LiveData<List<Song>> getMostlyPlayed() {
        return Transformations.map(
                    dao.getMostlyPlayedEntries(50),
                    songURLs -> songURLs.stream()
                        .map(songURL -> BrowserManager.getSongFromPath(songURL))
                        .collect(Collectors.toList())
                    );
    }
}
