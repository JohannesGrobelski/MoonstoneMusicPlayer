package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.content.Context;

import com.example.moonstonemusicplayer.model.PlayListActivity.LocalSongLoader;

import java.io.File;

public class FolderManager {

  private Context context;
  //private DataSource dataSource;

  private Folder rootFolder;


  public FolderManager(Context baseContext) {
    this.context = baseContext;
  }

  /** loads local music and adds it to dataSource*/
  public void loadLocalMusicAsFolder(){
    //deleteAllSongs();//TODO: dont delete db but only local files
    File[] externalFileDirs = context.getExternalMediaDirs(); //getExternalMediaDirs actually does get both internal and external sdcards
    this.rootFolder = LocalSongLoader.findAllAudioFilesAsFolder(externalFileDirs);

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
