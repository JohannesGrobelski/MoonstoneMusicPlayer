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
  private static final boolean DEBUG = false;

  /** find all Audiofiles in externalDirs and create a List<Song> from these files*/
  public static Folder findAllAudioFilesAsFolder(File[] externalFilesDir){
    //System.getenv("SECONDARY_STORAGE");
    String[] fileDirs = new String[externalFilesDir.length];
    for(int i=0; i<fileDirs.length; i++){
      fileDirs[i] = externalFilesDir[i].getAbsolutePath().replace("/Android/media/com.example.moonstonemusicplayer","");
      if(DEBUG)Log.d(TAG,fileDirs[i]+" "+new File(fileDirs[i]).exists());
    }

    //go through sd-cards and
    List<Folder> childrenList = new ArrayList<>();
    int sdcard = 0;
    for(String fileDir: fileDirs){
      if(new File(fileDir).exists()){
        Folder child = findAllAudioFilesAsFolder(fileDir,null);
        if(child == null)continue;
        if(sdcard==0)child.setName("interner Speicher");
        else {
          if(fileDir.length()>2) {
            child.setName("SD-Karte "+sdcard);
          }
          else{child.setName("SD-Karte");}
        }
        if(child != null)childrenList.add(child);
      }
      ++sdcard;
    }
    Folder rootFolder = new Folder("root", null, childrenList.toArray(new Folder[childrenList.size()]),null);

    Log.d(TAG,rootFolder.toString());
    //set parents of folders (recursively)
    rootFolder.setParentsBelow();
    return rootFolder;
  }

  /** recursive */
  private static Folder findAllAudioFilesAsFolder(String directory, Folder parentFolder){
    if(DEBUG)Log.d(TAG,"findAllAudioFilesAsFolder FILE: "+directory);
    try {
      File file = new File(directory );
      if(file.exists()) {
        if(file.isDirectory()) {
          if(file.getAbsolutePath().endsWith("Android")){
            if(DEBUG)Log.d(TAG,"findAllAudioFilesAsFolder isAndroid: "+file.getAbsolutePath());
            return null; //throws null pointer exception; cannot enter without root
          }
          if(file.listFiles() != null){
            List<Folder> children_folder = new ArrayList<>();
            List<Song> children_song = new ArrayList<>();
            for (File childFile: file.listFiles()) { //gehe durch Kinder
              if(DEBUG)Log.d(TAG,"findAllAudioFilesAsFolder TEST: "+childFile.getAbsolutePath());
              Folder child_folder = findAllAudioFilesAsFolder(childFile.getAbsolutePath(), parentFolder);
              if(child_folder != null){//child is a directory
                children_folder.add(child_folder);
              } else {//child is not a dir, might be a song-file?
                if (isSupportedFormat(childFile.getName())) {
                  children_song.add(getSongFromAudioFile(childFile));
                } else { /* ignore */ }
              }
            }
            if(!(children_folder.isEmpty() && children_song.isEmpty())){ //directory is not empty
              if(DEBUG)Log.d(TAG,"findAllAudioFilesAsFolder: save"+file.getName()+" "+children_folder.size()+" "+children_song.size());
              return new Folder(file.getName(),
                  parentFolder,
                  children_folder.toArray(new Folder[children_folder.size()]),
                  children_song.toArray(new Song[children_song.size()])
              );
            } else {
              if(DEBUG)Log.e(TAG,"findAllAudioFilesAsFolder empty Dir: "+file.getAbsolutePath());
            }
          } else {
            if(DEBUG)Log.e(TAG,"findAllAudioFilesAsFolder list files is null: "+file.getName());
            return null;
          }
        } else { //file is not a directory
          if(DEBUG)Log.e(TAG,"findAllAudioFilesAsFolder file is not a dir: "+file.getName());
          return null;
        }
      } else{ //file does not exist
        if(DEBUG)Log.e(TAG,"findAllAudioFilesAsFolder file does not exist: "+file.getName());
        return null;
      }
    } catch (Exception e){
      if(DEBUG)Log.e(TAG,"findAllAudioFilesAsFolder error: "+e.getMessage());
      if(DEBUG)Log.e(TAG, "findAllAudioFilesAsFolder error: "+String.valueOf(e.getCause()));
    }
    return null;
  }

  /** find all Audiofiles in externalDirs and create a List<Song> from these files*/
  public static List<Song> findAllAudioFiles(File[] externalFilesDir){
      List<Song> result = new ArrayList<>();

      //System.getenv("SECONDARY_STORAGE");
      String[] fileDirs = new String[externalFilesDir.length];
      for(int i=0; i<fileDirs.length; i++){
        fileDirs[i] = externalFilesDir[i].getAbsolutePath().replace("/Android/media/com.example.moonstonemusicplayer","");
        if(DEBUG)Log.d("SongManager",fileDirs[i]+" "+new File(fileDirs[i]).exists());
      }
      for(String fileDir: fileDirs){
        if(new File(fileDir).exists())result.addAll(findAllAudioFiles(fileDir,null));
      }
      return result;
  }

  private static List<Song> findAllAudioFiles(String directory, List<Song> localAudioFiles){
    if(DEBUG)Log.d("SongManager","findAllAudioFiles");
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
            findAllAudioFiles(childFile.getAbsolutePath(),localAudioFiles);
          }
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
        "mp3","3gp","mp4","m4a","amr","flac","mkv","ogg","wav"
    };

    for(String ext: supportedExtensions){
      if(filename.endsWith(ext) || filename.endsWith(ext.toUpperCase()))return true;
    }
    return false;
  }
}
