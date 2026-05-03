package com.example.moonstonemusicplayer.model.Database.Playlist;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface PlaylistDao {

    public final static String FAVORITES = "Favorites";
    public final static String RECENTLY_PLAYED = "RecentlyPlayed";
    public final static String RECENTLY_ADDED = "RecentlyAdded";
    public final static String MOSTLY_PLAYED = "MostlyPlayed";
   
    @Query("SELECT DISTINCT PlaylistName FROM PlaylistEntry WHERE PlaylistName != 'RecentlyPlayed' AND PlaylistName != 'MostlyPlayed' AND PlaylistName != 'Favorites'")
    public LiveData<String[]> getAllPlaylistNames();

    @Query("SELECT SongURL FROM PlaylistEntry WHERE PlaylistName = 'Favorites'")
    public LiveData<List<String>> getAllFavoritesSongURLs();

    @Query("SELECT SongURL FROM PlaylistEntry WHERE PlaylistName = 'RecentlyPlayed'")
    public LiveData<List<String>> getAllRecentlyPlayedSongURLs();

    @Query("SELECT SongURL FROM PlaylistEntry WHERE PlaylistName = :playlistName ORDER BY PlaylistIndex ASC")
    public List<String> getAllPlaylistEntries(String playlistName);

    @Query("SELECT EXISTS(SELECT 1 FROM PlaylistEntry WHERE PlaylistName = 'Favorites' AND SongURL = :songURL)")
    public LiveData<Boolean> isInFavorites(String songURL);

    @Query("SELECT COUNT(*) FROM PlaylistEntry WHERE PlaylistName = :playlistName")
    public int getPlaylistCount(String playlistName);

    @Query("SELECT DISTINCT SongURL FROM PlaylistEntry ORDER BY Playcount DESC LIMIT :maxEntries")
    public LiveData<List<String>> getMostlyPlayedEntries(int maxEntries);

    @Query("DELETE FROM PlaylistEntry WHERE PlaylistName = :playlistName")
    public void deletePlaylist(String playlistName);
    
    @Query("DELETE FROM PlaylistEntry WHERE PlaylistName = :playlistName AND SongURL = :songURL")
    public void deleteFromPlaylist(String playlistName, String songURL);

    @Query("UPDATE PlaylistEntry SET Playcount = Playcount + 1 WHERE SongURL = :songURL")
    public void incrementPlaycount(String songURL);

    @Query("UPDATE PlaylistEntry SET PlaylistIndex = :playlistIndex WHERE PlaylistName = :playlistName AND SongURL = :songURL")
    public void updatePlaylistEntryIndex(String playlistName, String songURL, int playlistIndex);

    @Insert
    void insert(PlaylistEntry playlistEntry);

    @Delete
    void delete(PlaylistEntry... playlistEntries);

    @Update
    void update(PlaylistEntry... playlistEntries);
}

