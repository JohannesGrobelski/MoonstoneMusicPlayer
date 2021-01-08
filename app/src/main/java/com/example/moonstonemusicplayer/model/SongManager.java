package com.example.moonstonemusicplayer.model;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageState;

/**
 * datasource: contains a list of all songs avaiable
 */
public class SongManager {
  private static final boolean DEBUG = false;

  public static List<Song> findAllAudioFiles(String directory, List<Song> localAudioFiles){
    if(localAudioFiles == null){
      localAudioFiles = new ArrayList<>();
    }
    if(directory == null){
      if (Environment.MEDIA_MOUNTED.equals(getExternalStorageState())
      && (Environment.MEDIA_MOUNTED_READ_ONLY.equals(getExternalStorageState()))) {
        return localAudioFiles;
      }
      directory = Environment.getExternalStorageDirectory().toString(); //sd_card
    }
    try {
      File file = new File(directory );
      if (file.isDirectory()) {
        if(file.getAbsolutePath().endsWith("Android"))return localAudioFiles; //throws null pointer exception; cannot enter without root
        for (File childFile: file.listFiles()) {
          findAllAudioFiles(childFile.getAbsolutePath(),localAudioFiles);
        }
      } else{
        if(DEBUG)Log.d("SongManager","file found: "+file.getAbsolutePath());
        if (isSupportedFormat(file.getName())) {
          if(DEBUG)Log.d("SongManager","audiofile found: "+file.getAbsolutePath());
          if(!localAudioFiles.contains(getSongFromAudioFile(file))){
            if(DEBUG)Log.d("SongManager","audiofile added: "+file.getAbsolutePath());
            localAudioFiles.add(getSongFromAudioFile(file));
          }
        }
      }
    } catch (Exception e){
      if(DEBUG)Log.d("songmanager","cannot access all files");

    }
    if(directory.equals(Environment.getExternalStorageDirectory().toString())){
      if(DEBUG)Log.d("songmanager listsize","songs: "+ localAudioFiles.size());
    }
    return localAudioFiles;
  }

  private static Song getSongFromAudioFile(File file){
    String title = file.getName().substring(0, (file.getName().length() - 4));
    String author = "";
    String URI = Uri.fromFile(file).toString();

    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
      mmr.setDataSource(Uri.fromFile(file).getPath());
    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    int duration = Integer.parseInt(durationStr);
    return new Song(title,author,URI,duration);
  }

  private static boolean isSupportedFormat(String filename){
    String[] supportedExtensions = new String[]{
        "mp3","3gp","mp4","m4a","3gp","amr","flac","mp3","mkv","ogg","wav"
    };

    for(String ext: supportedExtensions){
      if(filename.endsWith(ext) || filename.endsWith(ext.toUpperCase()))return true;
    }
    return false;
  }
}
