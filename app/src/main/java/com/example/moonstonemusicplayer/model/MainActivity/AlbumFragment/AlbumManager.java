package com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Folder.DBFolder;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlbumManager {

  private static final String TAG = AlbumManager.class.getSimpleName();

  private Context context;
  private Album currentAlbum;
  private List<Album> albumList = new ArrayList<>();
  private final List<Album> albumList_backup = new ArrayList<>();

  public AlbumManager(Context baseContext) {
    loadAlbumsFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadAlbumsFromDB(Context context){
    if(context != null){
      this.albumList.clear();
      this.albumList_backup.clear();
      this.albumList.addAll(DBFolder.getInstance(context).getAlbumList());
      this.albumList_backup.addAll(albumList);
    }
  }

  public List<Album> getAlbumList() {
    return albumList;
  }

  public void setAlbumList(List<Album> albumList) {
    this.albumList = albumList;
  }

  /** search for albums with name or artistname matching the query*/
  public Album[] getAllAlbumsMatchingQuery(String query) {
    List<Album> results = new ArrayList<>();
    for(Album Album: getAlbumList()){
      if(Album.getName().toLowerCase().contains(query.toLowerCase())
      || Album.getArtistName().toLowerCase().contains(query.toLowerCase())){
            results.add(Album);
      }
    }
    return results.toArray(new Album[results.size()]);
  }

  public List<Album> getAllAlbums() {
    Log.d("albumList","backup: "+albumList_backup.size());
    albumList.clear();
    albumList.addAll(albumList_backup);
    return albumList;
  }

  public Album getCurrentAlbum() {
    return currentAlbum;
  }

  public void setCurrentAlbum(Album currentAlbum) {
    this.currentAlbum = currentAlbum;
  }

  public void sortSongsByDuration() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (int) (o1.getDuration_ms() - o2.getDuration_ms());
        }
      });
    }

  }

  public void sortSongsByArtist() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getArtist().compareTo(o2.getArtist()));
        }
      });
    }

  }

  public void sortSongsByName() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getName().compareTo(o2.getName()));
        }
      });
    }

  }

  public void sortSongsByGenre() {
    if(currentAlbum != null){
      Collections.sort(currentAlbum.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song  o2) {
          return (o1.getGenre().compareTo(o2.getGenre()));
        }
      });
    }

  }

  public void reverse(){
    if(currentAlbum != null){
      Collections.reverse(albumList);
    }
  }
}
