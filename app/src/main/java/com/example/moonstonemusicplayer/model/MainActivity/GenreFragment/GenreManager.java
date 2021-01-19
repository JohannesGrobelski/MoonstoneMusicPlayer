package com.example.moonstonemusicplayer.model.MainActivity.GenreFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Folder.DBFolder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//import com.example.moonstonemusicplayer.model.Database.DBSonglists;

/** saves and loads genres and contains the current (displayed) genre in genrefragment*/
public class GenreManager {

  private static final String TAG = GenreManager.class.getSimpleName();
  private Context context;
  //private DataSource dataSource;

  private Genre currentGenre;
  private List<Genre> genreList = new ArrayList<>();
  private List<Genre> genreList_backup = new ArrayList<>();

  public GenreManager(Context baseContext) {
    this.context = baseContext;
    loadGenresFromDB(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadGenresFromDB(Context context){
    if(context != null){
      genreList.clear();
      genreList_backup.clear();
      Log.d(TAG,"loadGenresFromDB");
      this.genreList.addAll(DBFolder.getInstance(context).getGenreList());
      this.genreList_backup.addAll(genreList);
    }
  }

  public Genre getGenre(String name){
    for(Genre genre: this.genreList){
      if(genre.name.equals(name))return genre;
    }
    return null;
  }

  public List<Genre> getAllGenres(){
    genreList.clear();
    genreList.addAll(genreList_backup);
    return this.genreList;
  }

  public List<String> getGenreNames(){
    List<String> genreNames = new ArrayList<>();
    for(Genre genre: this.genreList){
      genreNames.add(genre.name);
    }
    return genreNames;
  }

  public Genre getCurrentGenre() {
    return currentGenre;
  }

  public void setCurrentGenre(Genre currentGenre) {
    this.currentGenre = currentGenre;
  }

  public List<Genre> getGenreList() {
    return genreList;
  }

  public void setGenreList(List<Genre> genreList) {
    this.genreList = genreList;
  }

  public Genre[] getAllGenresMatchingQuery(String query) {
    List<Genre> result = new ArrayList<>();
    for(Genre genre: getAllGenres()){
      if(genre.getName().toLowerCase().contains(query.toLowerCase())){
        result.add(genre);
      }
    }
    return result.toArray(new Genre[result.size()]);
  }

  public Song[] getAllSongsMatchingQuery(String query) {
    List<Song> result = new ArrayList<>();
    for(Genre genre: getAllGenres()){
      for(Song song: genre.getSongList()) {
        if(song.getName().toLowerCase().contains(query.toLowerCase())){
          result.add(song);
        }
      }
    }
    return result.toArray(new Song[result.size()]);
  }



  public void deleteGenre(Genre genre) {
    genreList_backup.remove(genre);
    genreList.remove(genre);
  }

  public void deleteFromGenre(Song song, String name) {
    for(Genre genre: genreList_backup){
      if(genre.name.equals(name)){
        genre.getSongList().remove(song);
      }
    }
    for(Genre genre: genreList){
      if(genre.name.equals(name)){
        genre.getSongList().remove(song);
      }
    }
  }

  public void sortSongsByGenre() {
    if(currentGenre != null){
      Collections.sort(currentGenre.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getGenre().compareTo(o2.getGenre());
        }
      });
    }
  }

  public void sortSongsByDuration() {
    if(currentGenre != null){
      Collections.sort(currentGenre.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return (int)(o1.getDuration_ms() - o2.getDuration_ms());
        }
      });
    }
  }

  public void sortSongsByArtist() {
    if(currentGenre != null){
      Collections.sort(currentGenre.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getArtist().compareTo(o2.getArtist());
        }
      });
    }
  }

  public void sortSongsByName() {
    if(currentGenre != null){
      Collections.sort(currentGenre.getSongList(), new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    Collections.sort(genreList, new Comparator<Genre>() {
      @Override
      public int compare(Genre o1, Genre o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
  }

  public void reverse(){
    if(currentGenre != null){
      Collections.reverse(currentGenre.getSongList());
    }
  }
}
