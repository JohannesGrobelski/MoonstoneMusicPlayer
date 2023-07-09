package com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

//import com.example.moonstonemusicplayer.model.Database.DBSonglists;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** saves and loads playlists and contains the current (displayed) playlist in playlistfragment*/
public class PlaylistListManager {

  private static final String TAG = PlaylistListManager.class.getSimpleName();
  private final Context context;
  //private DataSource dataSource;

  private Playlist currentPlaylist;
  private List<Playlist> playlists = new ArrayList<>();
  private final List<Playlist> playlists_backup = new ArrayList<>();

  public PlaylistListManager(Context baseContext) {
    this.context = baseContext;
    loadPlaylistsFromDB(baseContext);
  }

  /** Update the playlists.
   *
   * @param context
   */
  public void updateData(Context context){
    playlists_backup.clear();
    playlists.clear();
    loadPlaylistsFromDB(context);
    playlists.addAll(playlists_backup);
  }

  /** loads local music and adds it to dataSource*/
  public void loadPlaylistsFromDB(Context context){
    createRecentlyAddedPlaylist(context);
    createRecentlyPlayedPlaylist(context);
    //List<Playlist> allPlayLists = DBPlaylists.getInstance(context).getAllPlaylists(context);

  }

  public Playlist getPlaylist(String name){
    for(Playlist playList: this.playlists){
      if(playList.name.equals(name))return playList;
    }
    return null;
  }

  public List<Playlist> getAllPlaylists(){
    playlists.clear();
    playlists.addAll(playlists_backup);
    return this.playlists;
  }

  public List<String> getPlaylistNames(){
    List<String> playlistNames = new ArrayList<>();
    for(Playlist playList: this.playlists){
      playlistNames.add(playList.name);
    }
    return playlistNames;
  }

  public Playlist getCurrentPlaylist() {
    return currentPlaylist;
  }

  public void setCurrentPlaylist(Playlist currentPlaylist) {
    this.currentPlaylist = currentPlaylist;
  }

  public List<Playlist> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(List<Playlist> playlists) {
    this.playlists = playlists;
  }

  public Playlist[] getAllPlaylistsMatchingQuery(String query) {
    List<Playlist> result = new ArrayList<>();
    for(Playlist playlist: getAllPlaylists()){
      if(playlist.getName().toLowerCase().contains(query.toLowerCase())){
        result.add(playlist);
      }
    }
    return result.toArray(new Playlist[result.size()]);
  }

  public Song[] getAllSongsMatchingQuery(String query) {
    List<Song> result = new ArrayList<>();
    for(Playlist playlist: getAllPlaylists()){
      for(Song song: playlist.getPlaylist()) {
        if(song.getName().toLowerCase().contains(query.toLowerCase())){
          result.add(song);
        }
      }
    }
    return result.toArray(new Song[result.size()]);
  }



  public void deletePlaylist(Playlist playlist) {
    playlists_backup.remove(playlist);
    playlists.remove(playlist);
  }

  public void deleteFromPlaylist(Song song, String name) {
    for(Playlist playlist: playlists_backup){
      if(playlist.name.equals(name)){
        playlist.getPlaylist().remove(song);
      }
    }
    for(Playlist playlist: playlists){
      if(playlist.name.equals(name)){
        playlist.getPlaylist().remove(song);
      }
    }
  }

  public void sortSongsByGenre() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getGenre().compareTo(o2.getGenre());
        }
      });
    }
  }

  public void sortSongsByDuration() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return (int)(o1.getDuration_ms() - o2.getDuration_ms());
        }
      });
    }
  }

  public void sortSongsByArtist() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getArtist().compareTo(o2.getArtist());
        }
      });
    }
  }

  public void sortSongsByName() {
    if(currentPlaylist != null){
      Collections.sort(currentPlaylist.getPlaylist(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    Collections.sort(playlists, new Comparator<Playlist>() {
      @Override
      public int compare(Playlist o1, Playlist o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
  }

  public void reverse(){
    if(currentPlaylist != null){
      Collections.reverse(currentPlaylist.getPlaylist());
    }
  }

  private void createRecentlyAddedPlaylist(Context context){
    Playlist recentlyAddedPlaylist = new Playlist("recently added", new ArrayList<>());

    List<Song> songList = new ArrayList<>();

    // Define the columns to retrieve from the MediaStore
    String[] projection = {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DURATION,
    };

    // Set the sort order to get the most recently added songs first
    String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

    // Limit the query to retrieve only the last 30 songs
    int limit = 100;

    Cursor cursor = context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
    );

    // Process the cursor to get the song information
    if (cursor != null && cursor.moveToFirst()) {
      do {
        // Retrieve the column index for the data column
        int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int genreIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE);
        int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

        String filePath = cursor.getString(dataIndex);
        String name = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);
        String album = cursor.getString(albumIndex);
        String genre = cursor.getString(genreIndex);
        String durationString = cursor.getString(durationIndex);

        if(filePath == null || filePath.isEmpty()
                || name == null || name.isEmpty()
                || durationString == null || durationString.isEmpty())continue;

        int duration_ms = 0;
        duration_ms = Integer.parseInt(durationString);

        File songFile = new File(filePath);
        Song song = new Song(filePath,name,artist,album,genre,duration_ms,"");
        songList.add(song);
        --limit;
      } while (cursor.moveToNext() && limit > 0);

      cursor.close();
    }
    recentlyAddedPlaylist.playlist.addAll(songList);
    this.playlists_backup.add(recentlyAddedPlaylist);
  }

  private void createRecentlyPlayedPlaylist(Context context){
    Playlist recentlyPlayedPlaylist = new Playlist("recently played", new ArrayList<>());
    List<Song> songListRecentlyPlayed = DBPlaylists.getInstance(context).getAllRecentlyPlayed(context);
    for(int i=songListRecentlyPlayed.size()-1; i>=0; i--){
      recentlyPlayedPlaylist.playlist.add(songListRecentlyPlayed.get(i));
    }
    this.playlists_backup.add(recentlyPlayedPlaylist);
  }

}
