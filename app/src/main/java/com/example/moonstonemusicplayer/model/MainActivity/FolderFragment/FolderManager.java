package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.content.Context;
import android.util.Log;

import com.example.moonstonemusicplayer.controller.PlayListActivity.RefreshTask;
import com.example.moonstonemusicplayer.model.PlayListActivity.LocalSongLoader;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FolderManager {

  private static final String TAG = FolderManager.class.getSimpleName();
  private Context context;
  //private DataSource dataSource;

  private Folder rootFolder;


  public FolderManager(Context baseContext) {
    this.context = baseContext;
    //FolderLoader.loadFromXML(baseContext);
  }

  /** loads local music*/
  public void loadLocalMusicAsFolder(){
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


  public Folder[] getAllFoldersMatchingQuery(String query){
    List<Folder> result = getAllFoldersMatchingQuery(query,rootFolder,new ArrayList<Folder>());
    return result.toArray(new Folder[result.size()]);
  }

  /** recursively descends the root folder searches for child folders whose names match the querystring*/
  private List<Folder> getAllFoldersMatchingQuery(String query, Folder folder, List<Folder> inputList){
    if(folder != null){
      if(folder.getName().toLowerCase().contains(query.toLowerCase())) inputList.add(folder);
      if(folder.getChildren_folders() != null) {
        for (Folder subFolder : folder.getChildren_folders()) {
          getAllFoldersMatchingQuery(query,subFolder,inputList);
        }
      }
    }
    return inputList;
  }

  public Song[] getAllSongsMatchingQuery(String query){
    List<Song> result = getAllSongsMatchingQuery(query,rootFolder,new ArrayList<Song>());
    return result.toArray(new Song[result.size()]);
  }

  /** recursively descends the root folder searches for songs in all subfolders whose names match the querystring*/
  private List<Song> getAllSongsMatchingQuery(String query, Folder folder, List<Song> inputList){
    if(folder != null){
      if(folder.getChildren_songs() != null) {
        for (Song childSong: folder.getChildren_songs()) {
          if(childSong.getName().toLowerCase().contains(query.toLowerCase())){
            inputList.add(childSong);
          }
        }
      }
      if(folder.getChildren_folders() != null) {
        for (Folder subFolder : folder.getChildren_folders()) {
          getAllSongsMatchingQuery(query,subFolder,inputList);
        }
      }
    }
    return inputList;
  }

}
