package com.example.moonstonemusicplayer.controller;

import android.Manifest;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.moonstonemusicplayer.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageState;

public class SongManager {
  Context context;
  List<Song> songList = new ArrayList<>();

  public SongManager(Context context){
    this.context= context;
  }

  public void addSong(Song song){
    songList.add(song);
  }

  public Song getSong(int songIndex){
    return songList.get(songIndex);
  }

  public int getSongCount(){
    return songList.size();
  }

  public List<Song> getSongList(){
    return songList;
  }


  public void findAllAudioFiles(String directory){
    if(directory == null){
      if (Environment.MEDIA_MOUNTED.equals(getExternalStorageState())
      && (Environment.MEDIA_MOUNTED_READ_ONLY.equals(getExternalStorageState()))) {
        Toast.makeText(context,"cannot access all files: storage not readable",Toast.LENGTH_LONG).show();
        return;
      }
      directory = Environment.getExternalStorageDirectory().toString(); //sd_card
    }
    try {
      File file = new File(directory );
      if (file.isDirectory()) {
        if(file.getAbsolutePath().endsWith("Android"))return; //throws null pointer exception; cannot enter without root
        Log.d("songmanager dir","   try access: "+file.getAbsolutePath());
        for (File childFile: file.listFiles()) {
          Log.d("songmanager dir",childFile.getAbsolutePath());
          findAllAudioFiles(childFile.getAbsolutePath());
        }
      } else{
        Log.d("songmanager file",file.getAbsolutePath());
        if (file.getName().endsWith(".mp3") || file.getName().endsWith(".MP3")) {
          if(!this.songList.contains(getSongFromAudioFile(file)))this.songList.add(getSongFromAudioFile(file));
        }
      }
    } catch (Exception e){
      Toast.makeText(context,"cannot access all files: "+e.getMessage(),Toast.LENGTH_LONG).show();
    }


    if(directory.equals(Environment.getExternalStorageDirectory().toString())){
      Toast.makeText(context,"songs: "+songList,Toast.LENGTH_LONG).show();
      Log.d("songmanager listsize","songs: "+songList);
    }
  }

  private Song getSongFromAudioFile(File file){
    String title = file.getName().substring(0, (file.getName().length() - 4));
    String author = "";
    String URI = Uri.fromFile(file).toString();

    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    mmr.setDataSource(context,Uri.fromFile(file));
    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    int duration = Integer.parseInt(durationStr);
    return new Song(title,author,URI,duration);
  }


}
