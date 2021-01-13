package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.LocalSongLoader;

import java.io.File;

public class FolderManager {

  private static final String TAG = FolderManager.class.getSimpleName();
  private Context context;
  //private DataSource dataSource;

  private Folder rootFolder;


  public FolderManager(Context baseContext) {
    this.context = baseContext;
    //FolderLoader.loadFromXML(baseContext);
  }

  /** loads local music and adds it to dataSource*/
  public void loadLocalMusicAsFolder(Context context){
    //deleteAllSongs();//TODO: dont delete db but only local files
    if(context != null){
      File[] externalFileDirs = context.getExternalMediaDirs(); //getExternalMediaDirs actually does get both internal and external sdcards
      this.rootFolder = LocalSongLoader.findAllAudioFilesAsFolder(externalFileDirs);
      FolderLoader.saveIntoXML(this.rootFolder,context.getFilesDir().getAbsolutePath());
    }
    //dataSource.insertSongList(LocalSongLoader.findAllAudioFiles(externalFileDirs));
    //playList.addAll(dataSource.getAllSong(60000));
  }

  public void loadSavedMusicAsFolder(Context context){
    //deleteAllSongs();//TODO: dont delete db but only local files
    if(context != null){
      this.rootFolder = FolderLoader.loadFromXML(context);
    } else {
      Log.e(TAG,"loadSavedMusicAsFolder: context null");
    }
    //dataSource.insertSongList(LocalSongLoader.findAllAudioFiles(externalFileDirs));
    //playList.addAll(dataSource.getAllSong(60000));
  }


  public Folder getRootFolder() {
    return rootFolder;
  }

  public void setRootFolder(Folder rootFolder) {
    this.rootFolder = rootFolder;
  }


}
