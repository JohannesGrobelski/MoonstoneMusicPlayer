package com.example.moonstonemusicplayer.model.MainActivity;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to load List<Song> from sd-card(s).
 */
public class LocalSongLoader {
  private static final String TAG = LocalSongLoader.class.getSimpleName();
  private static final boolean DEBUG = true;

  /** find all Audiofiles in externalDirs and create a List<Song> from these files*/
  public static Folder findAllAudioFilesAsFolderInDir(File[] externalFilesDir){
    String[] fileDirs = new String[externalFilesDir.length];
    for(int i=0; i<fileDirs.length; i++){
      fileDirs[i] = externalFilesDir[i].getAbsolutePath().replace("/Android/media/com.example.moonstonemusicplayer","");
      if(DEBUG){
        Log.d(TAG,fileDirs[i]+" "+new File(fileDirs[i]).exists());
      }
    }

    //go through sd-cards and
    List<Folder> childrenList = new ArrayList<>();
    int sdcard = 0;
    for(String fileDir: fileDirs){
      if(new File(fileDir).exists()){
        Folder child = findAllAudioFilesAsFolderInDir(fileDir,null);
        if(child == null)continue;
        else {
          child.setPath(fileDir);
          if(sdcard==0)child.setName("interner Speicher");
          else {
            if(fileDir.length()>1) {
              child.setName("SD-Karte "+sdcard);
            }
            else{child.setName("SD-Karte");}
          }
          childrenList.add(child);
        }
      }
      ++sdcard;
    }
    Folder rootFolder = new Folder("root", "rootpath",null, childrenList.toArray(new Folder[childrenList.size()]),null);
    rootFolder.setParentsBelow();
    return rootFolder;
  }

  /** recursive */
  private static Folder findAllAudioFilesAsFolderInDir(String directory, Folder parentFolder){
    try {
      File file = new File(directory );
      if(file.exists()) {
        if(file.isDirectory()) {
          if(file.getAbsolutePath().endsWith("Android")){
            return null; //throws null pointer exception; cannot enter without root
          }
          List<Folder> child_folders = new ArrayList<>();
          List<Song> child_songs = new ArrayList<>();
          //iterate over children and get audio files and child directories
          try {
            File[] childFiles = file.listFiles();
            if(childFiles != null){
              for (File childFile: childFiles) { //gehe durch Kinder
                if(childFile == null)continue;
                if(childFile.isDirectory()){
                  //if childFile is dir -> turn it into a folder object
                  Folder child_folder = findAllAudioFilesAsFolderInDir(childFile.getAbsolutePath(), parentFolder);
                  if(child_folder != null){
                    child_folders.add(child_folder);
                  }
                } else {
                  //if childFile is not dir -> check if it is a song file
                  if(childFile.isFile()){
                    if (isSupportedFormat(childFile.getName())) {
                      Song song = getSongFromAudioFile(childFile);
                      if(song != null){
                        child_songs.add(song);
                      }
                    }
                  }
                }
              }
            } else {
              Log.e(TAG,"findAllAudioFilesAsFolderInDir: CHILDFILES NULL for DIR"+file.getName());
            }
            if(!(child_folders.isEmpty() && child_songs.isEmpty())){ //directory is not empty
              Log.d(TAG,"Create folder "+file.getName());
              return new Folder(file.getName(),
                      file.getAbsolutePath(),
                      parentFolder,
                      child_folders.toArray(new Folder[child_folders.size()]),
                      child_songs.toArray(new Song[child_songs.size()])
              );
            }
          } catch (Exception e){
            Log.e(TAG,"findAllAudioFilesAsFolderInDir Exception: "+e);
          }
        } else { //file is not a directory
          if(DEBUG){
            //Log.e(TAG, "findAllAudioFilesAsFolderInDir not a dir: "+file.getName());
          }
          return null;
        }
      } else{ //file does not exist
        if(DEBUG){
          //Log.e(TAG, "findAllAudioFilesAsFolderInDir does not exist: "+file.getName());
        }
        return null;
      }
    } catch (Exception e){
      if(DEBUG){
        //Log.e(TAG, "findAllAudioFilesAsFolderInDir error: "+ e.getCause());
      }
      return null;
    }
    return null;
  }

  private static List<Song> findAllAudioFilesInDir(String directory, List<Song> localAudioFiles){
    if(localAudioFiles == null){
      localAudioFiles = new ArrayList<>();
    }
    if(directory == null)return localAudioFiles;
    File file;
    try {
      file = new File(directory );
      if (file.exists() && file.isDirectory()) {
        if(file.getAbsolutePath().endsWith("Android"))return localAudioFiles; //throws null pointer exception; cannot enter without root
        if(file.listFiles() != null){
          for (File childFile: file.listFiles()) {
            findAllAudioFilesInDir(childFile.getAbsolutePath(),localAudioFiles);
          }
        }
      } else{
        if (isSupportedFormat(file.getName())) {
          if(!localAudioFiles.contains(getSongFromAudioFile(file))){
            localAudioFiles.add(getSongFromAudioFile(file));
          }
        }
      }
    } catch (Exception e){
      return localAudioFiles;
    }
    if(directory.equals(Environment.getExternalStorageDirectory().toString())){
      if(DEBUG)Log.d("songmanager listsize","songs: "+ localAudioFiles.size());
    }
    return localAudioFiles;
  }

  /**
   * create song from file by extracting metadata
   * @param file
   * @return
   */
  private static Song getSongFromAudioFile(File file){
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

      return new Song(path,title,artist,album,genre,duration,"");
    } catch (Exception e){
      Log.e(TAG, "getSongFromAudioFile Could not parse to a song: "+file.getName()+"; Exception: "+e);
      return null;
    }
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
}
