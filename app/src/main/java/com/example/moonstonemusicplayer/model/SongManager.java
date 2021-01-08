package com.example.moonstonemusicplayer.model;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.moonstonemusicplayer.utility.ExternalStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static android.os.Environment.getDataDirectory;
import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static android.os.Environment.getStorageDirectory;

/**
 * datasource: contains a list of all songs avaiable
 */
public class SongManager {
  private static final boolean DEBUG = true;

  public static List<Song> findAllAudioFiles(String directory, List<Song> localAudioFiles){
    if(DEBUG)Log.d("SongManager","findAllAudioFiles");

    if(localAudioFiles == null){
      localAudioFiles = new ArrayList<>();
    }
    if(directory == null){
      directory = getExternalStorageDirectory().getAbsolutePath();
         //System.getenv("SECONDARY_STORAGE");

      Map<String, File> externalLocations = ExternalStorage.getAllStorageLocations();
      File sdCard = externalLocations.get(ExternalStorage.SD_CARD);
      File externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD);

      directory = sdCard.getAbsolutePath();


      /*
      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
        if(DEBUG)Log.d("SongManager","MEDIA_MOUNTED");
        localAudioFiles.add(new Song(getExternalStorageDirectory().getAbsolutePath(),Environment.MEDIA_MOUNTED,"",-1));
        return localAudioFiles;
      } else {
        directory = getExternalStorageDirectory().toString(); //sd_card
      }


      */
    }
    File file;
    try {
      file = new File(directory );
      if (file.exists() && file.isDirectory()) {
        if(file.getAbsolutePath().endsWith("Android"))return localAudioFiles; //throws null pointer exception; cannot enter without root
        if(file.listFiles() == null){
          localAudioFiles.add(new Song("listFiles null for: "+file.getName(),file.getAbsolutePath(),"",-1));
          localAudioFiles.add(new Song("children for: "+file.getName(), Arrays.toString(file.list()),"",-1));

        }
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
      if(DEBUG)Log.e("songmanager",e.getMessage());
      if(DEBUG)Log.e("songmanager", String.valueOf(e.getCause()));
      localAudioFiles.add(new Song("error",e.getMessage()+": "+String.valueOf(e.getCause()),"",-1));
      localAudioFiles.add(new Song("storage info: "+getExternalStorageDirectory().getAbsolutePath(),getExternalStorageState(),"",-1));

      return localAudioFiles;
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
