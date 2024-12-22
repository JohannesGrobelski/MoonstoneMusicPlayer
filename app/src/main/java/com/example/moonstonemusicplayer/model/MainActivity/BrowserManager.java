/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiofile;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Singleton
 *
 */
public class BrowserManager {

  public enum Filter {
    AUDIOBOOKS, SONGS
  }

  private static BrowserManager instance;
  private static List<File> audioFiles = new ArrayList<>();

  private static Map<File, Song> audiofileSongMap = new HashMap<>();
  private static Map<File, Audiobook> audioFileAudiobookMap = new HashMap<>();

  private static Map<String, List<Song>> genreListMap = new HashMap<>();

  private static Map<String, List<Song>> artistListMap = new HashMap<>();

  private static Map<String, List<Song>> albumListMap = new HashMap<>();

  private static final String TAG = BrowserManager.class.getSimpleName();
  private final Context context;

  private File rootFolder;


  private BrowserManager(Context baseContext) {
    this.context = baseContext;
    this.rootFolder = Environment.getExternalStorageDirectory();
    audioFiles = getAllAudioFiles(baseContext);
  }

  public static void reloadFilesInstance(Context context){
    BrowserManager instance = getInstance(context);
    instance.reloadFiles(context);
  }

  private void reloadFiles(Context context){
    audioFiles = getAllAudioFiles(context);
  }

  public static BrowserManager getInstance(Context context){
    if(instance == null){
      if(context != null){
        instance = new BrowserManager(context);
      }
    }
    return instance;
  }

  public File getRootFolder() {
    return rootFolder;
  }

  public void setRootFolder(File rootFolder) {
    this.rootFolder = rootFolder;
  }

  public static Map<String, List<Song>> getGenreListMap() {
    return genreListMap;
  }

  public static Map<String, List<Song>> getArtistListMap() {
    return artistListMap;
  }

  public static Map<String, List<Song>> getAlbumListMap() {
    return albumListMap;
  }

  public static List<File> getChildren(File file, Filter filter){
    List<File> songs = new ArrayList<>();
    List<File> children = new ArrayList<>();

    Set<String> childDirPathSet = new HashSet<>();
    if(file != null && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(filter == Filter.AUDIOBOOKS && !audioFileAudiobookMap.containsKey(audioFile)){
          continue;
        }
        if(isDirectChildFile(file, audioFile)){
          songs.add(audioFile);
        }
        if(!audioFile.getAbsolutePath().contains(file.getAbsolutePath())
                || audioFile.getParentFile().equals(file)){
          continue;
        } else {
          //if audio file resides in child directory of file add subdirectory to directories
          String relPath = audioFile.getAbsolutePath().replace(file.getAbsolutePath()+"/", "");
          String pathChildDir = file.getAbsolutePath() + "/" + relPath.substring(0,relPath.indexOf("/"));
          if(!childDirPathSet.contains(pathChildDir)){
            childDirPathSet.add(pathChildDir);
          }
        }
      }
    }
    for(String childDirPath : childDirPathSet){
      children.add(new File(childDirPath));
    }
    children.addAll(songs);
    return children;
  }

  public static List<File> getChildrenMatchingQuery(File file, String query, Filter filter){
    List<File> children = new ArrayList<>();

    if(file != null && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(fileMatchesQuery(query, audioFile)){
          if(filter == Filter.AUDIOBOOKS && !audioFileAudiobookMap.containsKey(audioFile)){
            continue;
          }
          children.add(audioFile);
        }
      }
    }
    return children;
  }

  public static File[] getDirectories(File file, Filter filter){
    Set<String> childDirPathSet = new HashSet<>();
    if(file != null && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(!audioFile.getAbsolutePath().contains(file.getAbsolutePath())
          || audioFile.getParentFile().equals(file)){
          continue;
        } else {
          //if audio file resides in child directory of file add subdirectory to directories
          if(filter == Filter.AUDIOBOOKS && !audioFileAudiobookMap.containsKey(audioFile)){
            continue;
          }
          String relPath = audioFile.getAbsolutePath().replace(file.getAbsolutePath()+"/", "");
          String pathChildDir = file.getAbsolutePath() + "/" + relPath.substring(0,relPath.indexOf("/"));
          if(!childDirPathSet.contains(pathChildDir)){
            childDirPathSet.add(pathChildDir);
          }
        }
      }
    }
    List<File> directories = new ArrayList<>();
    for(String childDirPath : childDirPathSet){
      directories.add(new File(childDirPath));
    }
    return directories.toArray(new File[directories.size()]);
  }

  public static Song[] getChildSongs(File file){
    List<Song> songs = new ArrayList<>();
    if(file != null && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(isDirectChildFile(file, audioFile)){
          songs.add(BrowserManager.getSongFromAudioFile(audioFile));
        }
      }
    }
    return songs.toArray(new Song[songs.size()]);
  }

  public static Audiobook[] getChildAudiobooks(File file){
    List<Audiobook> audiobooks = new ArrayList<>();
    if(file != null && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(isDirectChildFile(file, audioFile) &&
                parseSongFromAudioFile(file).getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS){
          audiobooks.add(BrowserManager.getAudiobookFromAudioFile(audioFile));
        }
      }
    }
    return audiobooks.toArray(new Audiobook[audiobooks.size()]);
  }

  public static File[] getChildFiles(File file, Filter filter){
    List<File> songs = new ArrayList<>();
    for(File audioFile : BrowserManager.audioFiles){
      if(isDirectChildFile(file,audioFile)){
        if(filter == Filter.AUDIOBOOKS && !audioFileAudiobookMap.containsKey(audioFile)){
          continue;
        }
        songs.add(audioFile);
      }
    }
    return songs.toArray(new File[songs.size()]);
  }

  private static boolean isDirectChildFile(File potentParent, File potentChild) {
    File parentDirectory = potentChild.getParentFile();
    return parentDirectory != null && parentDirectory.equals(potentParent);
  }


  /**
   * create song from file by extracting metadata
   * @param file
   * @return
   */
  public static Song getSongFromAudioFile(File file){
    if(audiofileSongMap.containsKey(file)){
      return audiofileSongMap.get(file);
    } else {
      Audiofile audiofile = parseSongFromAudioFile(file);
      Song song = new Song(audiofile.getPath(), audiofile.getName(), audiofile.getArtist(), audiofile.getAlbum(), audiofile.getGenre(), audiofile.getDuration_ms(), audiofile.getLyrics());
      audiofileSongMap.put(file,song);
      return song;
    }
  }

  public static File getFileFromSong(Song song){
    return new File(song.getPath());
  }

  /**
   * create song from file by extracting metadata
   * @param file
   * @return
   */
  public static Audiobook getAudiobookFromAudioFile(File file){
    if(audioFileAudiobookMap.containsKey(file)){
      return audioFileAudiobookMap.get(file);
    } else {
      Audiofile audiofile = parseSongFromAudioFile(file);
      Audiobook audiobook = new Audiobook(audiofile.getPath(), audiofile.getName(), audiofile.getArtist(), audiofile.getAlbum(), audiofile.getGenre(), audiofile.getDuration_ms(), audiofile.getLyrics());
      audioFileAudiobookMap.put(file,audiobook);
      return audiobook;
    }
  }


  private static Audiofile parseSongFromAudioFile(File file){
    try {
      String title = file.getName().substring(0, (file.getName().length() - 4));
      String path = file.getAbsolutePath();//Uri.fromFile(file).toString();
      String genre = "";
      String artist = "";
      String album = "";
      int duration = 0;

      MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      try {
        mmr.setDataSource(Uri.fromFile(file).getPath());
      } catch (Exception e){
        Log.e(TAG, e.toString());
        return null;
      }

      String meta_durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
      String meta_artist =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
      String meta_genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
      String meta_title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
      String meta_album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

      if(meta_title != null && !meta_title.isEmpty() && !meta_title.equals("null")){
        title = meta_title;
      }
      if(meta_album != null && !meta_album.isEmpty() && !meta_album.equals("null")){
        album = meta_album;
      }
      if(meta_genre != null && !meta_genre.isEmpty() && !meta_genre.equals("null")){
        genre = translateGenre(meta_genre);
      }
      if(meta_artist != null && !meta_artist.isEmpty() && !meta_artist.equals("null")){
        artist = meta_artist;
      } else {artist = "unbekannter Künstler";}
      if(meta_durationStr != null && !meta_durationStr.isEmpty() && !meta_durationStr.equals("null") && meta_durationStr.matches("[0-9]*")){
        duration = Integer.parseInt(meta_durationStr);
      }

      return new Audiofile(path,title,artist,album,genre,duration,"");
    } catch (Exception e){
      Log.e(TAG, "getSongFromAudioFile Could not parse to a song: "+file.getName()+"; Exception: "+e);
      return null;
    }
  }

  public static List<Song> getSongListFromFileList(List<File> fileList){
    List<Song> songList = new ArrayList<>();
    for(File file: fileList){
      if(!file.isDirectory()){
        songList.add(getSongFromAudioFile(file));
      }
    }
    return songList;
  }

  /**
   * returns if {"mp3","3gp","m4a","amr","flac","mkv","ogg","wav"} contains end of filename
   * @param filename
   * @return
   */
  private static boolean isSupportedFormat(String filename){
    String[] supportedExtensions = new String[]{
            "mp3","3gp","m4a","amr","flac","mkv","ogg","wav"
    };
    for(String ext: supportedExtensions){
      if(filename.endsWith(ext) || filename.endsWith(ext.toUpperCase()))return true;
    }
    return false;
  }

  /** translate some english genres to german */
  private static String translateGenre(String genre){
    switch(genre.toLowerCase()){
      case "classical": return "Klassik";
      case "other": return "Andere";
      default: return genre;
    }
  }

  private static List<File> getAllAudioFiles(Context context) {
    List<File> audioFiles = new ArrayList<>();

    //Some audio may be explicitly marked as not being music
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    // Define the columns to retrieve from the MediaStore
    String[] projection = {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.DURATION,
    };

    Uri collection;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
    } else {
      collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    // Perform the query using the MediaStore.Audio.Media.EXTERNAL_CONTENT_URI content URI
    Cursor cursor =  context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
    );

    if (cursor != null && cursor.moveToFirst()) {
      // Retrieve the column index for the data column
      int dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
      int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
      int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
      int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
      int genreIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE);
      int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

      // Iterate through the cursor to retrieve the file paths
      while (cursor.moveToNext()) {
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

        audioFiles.add(songFile);
        if(duration_ms >= Audiobook.AUDIOBOOK_CUTOFF_MS){
          Audiobook audiobook = new Audiobook(filePath,name,artist,album,genre,duration_ms,"");
          audioFileAudiobookMap.put(songFile,audiobook);
        } else {
          Song song = new Song(filePath,name,artist,album,genre,duration_ms,"");
          registerSongForArtistMap(song);
          registerSongForAlbumMap(song);
          registerSongForGenreMap(song);
          audiofileSongMap.put(songFile,song);
        }
      }

      //add files from Downloads/YTMusic
      /*
      String selectionYTMusic = MediaStore.Files.FileColumns.DATA + " LIKE ?";
      String[] selectionArgsYTMusic = new String[]{"%/storage/emulated/0/Download/YTMusic%"};

      cursor = context.getContentResolver().query(
              MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
              projection,
              selectionYTMusic,
              selectionArgsYTMusic,
              null
      );

      while (cursor.moveToNext()) {
        String filePath = cursor.getString(dataIndex);
        File songFile = new File(filePath);

        if(songFile.isFile()){
          Song song = (Song) parseSongFromAudioFile(songFile);
          audioFiles.add(songFile);
          audiofileAudiobookMap.put(songFile,song);
          registerSongForArtistMap(song);
          registerSongForAlbumMap(song);
          registerSongForGenreMap(song);
        }
      }
       */

      // Close the cursor
      cursor.close();
    }

    return audioFiles;
  }

  private static void registerSongForAlbumMap(Song song){
    if(song.getAlbum() != null && !song.getAlbum().isEmpty()){
      String albumName = song.getAlbum();
      if(!albumListMap.containsKey(albumName)){
        albumListMap.put(albumName, new ArrayList<>());
      }
      albumListMap.get(albumName).add(song);
    }
  }

  private static void registerSongForArtistMap(Song song){
    if(song.getArtist() != null && !song.getArtist().isEmpty()){
      String artistName = song.getArtist();
      if(!artistListMap.containsKey(artistName)){
        artistListMap.put(artistName, new ArrayList<>());
      }
      artistListMap.get(artistName).add(song);
    }
  }

  private static void registerSongForGenreMap(Song song){
    if(song.getGenre() != null && !song.getGenre().isEmpty()){
      String genreName = song.getGenre();
      if(!genreListMap.containsKey(genreName)){
        genreListMap.put(genreName, new ArrayList<>());
      }
      genreListMap.get(genreName).add(song);
    }
  }

  private static boolean fileMatchesQuery(String query, File audioFile){
    /*
    Song song = getSongFromPath(audioFile.getAbsolutePath());
    return audioFile.getAbsolutePath().toLowerCase().contains(query.toLowerCase())
            || song.getName().toLowerCase().contains(query.toLowerCase())
            || song.getAlbum().toLowerCase().contains(query.toLowerCase());
     */
    return audioFile.getAbsolutePath().toLowerCase().contains(query.toLowerCase());
  }

  public static Song getSongFromPath(String songPath){
    return getSongFromAudioFile(new File(songPath));
  }



}
