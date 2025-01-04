/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.model.MainActivity;

import android.app.RecoverableSecurityException;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiofile;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.ColorSettingsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Singleton
 *
 */
public class BrowserManager {

  public enum Filter {
    AUDIOBOOKS, SONGS
  }

  public interface AfterFileDeletion{
    void refreshAfterSongDeletion();
  }

  private static BrowserManager instance;
  private static List<File> audioFiles = new ArrayList<>();

  private static Map<File, Song> audiofileSongMap = new HashMap<>();
  private static Map<File, Audiobook> audioFileAudiobookMap = new HashMap<>();
  private static Map<String, Bitmap> thumbnailVault = new HashMap<>();

  private static Map<String, List<Song>> genreListMap = new HashMap<>();

  private static Map<String, List<Song>> artistListMap = new HashMap<>();

  private static Map<String, List<Song>> albumListMap = new HashMap<>();

  private static final String TAG = BrowserManager.class.getSimpleName();
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();
  private File rootFolder;

  private BrowserManager(Context baseContext) {
      this.rootFolder = Environment.getExternalStorageDirectory();
    audioFiles = getAllAudioFiles(baseContext);
    grabThumbnails(audioFiles);
  }

  public static void reloadFilesInstance(Context context){
    BrowserManager instance = getInstance(context);
    instance.reloadFiles(context);
  }

  private void reloadFiles(Context context){
    audioFiles = getAllAudioFiles(context);
    grabThumbnails(audioFiles);
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

  public static List<File> getChildren(File file, Filter filter) {
    List<File> results = new ArrayList<>();
    Set<String> childDirPathSet = new HashSet<>();

    // If we're at root level, we need to handle it differently
    boolean isRootDirectory = file.getAbsolutePath().equals("/storage/emulated/0");

    for(File audioFile : BrowserManager.audioFiles) {
      if(filter == Filter.AUDIOBOOKS && !audioFileAudiobookMap.containsKey(audioFile)) {
        continue;
      }

      if (isRootDirectory) {
        // For root directory, check if the file is in a direct subdirectory of /storage/emulated/0
        String relativePath = audioFile.getAbsolutePath().substring(file.getAbsolutePath().length());
        if (relativePath.startsWith("/")) {
          relativePath = relativePath.substring(1);
        }

        // Get the first directory in the path
        int firstSlash = relativePath.indexOf('/');
        if (firstSlash != -1) {
          String topLevelDir = relativePath.substring(0, firstSlash);
          String fullPath = file.getAbsolutePath() + "/" + topLevelDir;
          if (!childDirPathSet.contains(fullPath)) {
            childDirPathSet.add(fullPath);
          }
        } else {
          // This is a file directly in the root
          results.add(audioFile);
        }
      } else {
        // Normal directory handling
        if(isDirectChildFile(file, audioFile)) {
          results.add(audioFile);
        } else if(audioFile.getAbsolutePath().contains(file.getAbsolutePath())
                && !audioFile.getParentFile().equals(file)) {
          String relPath = audioFile.getAbsolutePath().replace(file.getAbsolutePath()+"/", "");
          String pathChildDir = file.getAbsolutePath() + "/" + relPath.substring(0, relPath.indexOf("/"));
          if(!childDirPathSet.contains(pathChildDir)) {
            childDirPathSet.add(pathChildDir);
          }
        }
      }
    }

    // Add all discovered directories to the results
    for(String childDirPath : childDirPathSet) {
      results.add(new File(childDirPath));
    }

    return results;
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

  public static Song[] getChildAudiobooks(File file){
    List<Song> audiobooks = new ArrayList<>();
    if(file != null && file.exists() && file.listFiles() != null){
      for(File audioFile : BrowserManager.audioFiles){
        if(isDirectChildFile(file, audioFile)){
          Audiofile audiofileObject = parseSongFromAudioFile(audioFile);
          if(audiofileObject != null && audiofileObject.getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS){
            audiobooks.add(BrowserManager.getSongFromAudioFile(audioFile));
          }
        }
      }
    }
    return audiobooks.toArray(new Song[audiobooks.size()]);
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
    if(file == null || !file.exists())return null;
    try {
      if(audiofileSongMap.containsKey(file)){
        return audiofileSongMap.get(file);
      } else {
        Audiofile audiofile = parseSongFromAudioFile(file);
        Song song = new Song(audiofile.getPath(), audiofile.getName(), audiofile.getArtist(), audiofile.getAlbum(), audiofile.getGenre(), audiofile.getDuration_ms(), audiofile.getLyrics());
        audiofileSongMap.put(file,song);
        return song;
      }
    } catch (Exception e){
      Log.e(TAG, e.toString());
      return null;
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
    if(file == null || !file.exists())return null;
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

  public static void deleteFile(AfterFileDeletion afterFileDeletion, Context context, ActivityResultLauncher<IntentSenderRequest> deletetionIntentSenderLauncher, String filePath) {
    Uri dataUri = getContentUriFromFilePath(context, filePath);
      executor.execute(() -> {
        try {
          context.getContentResolver().delete(dataUri, null, null);
          afterFileDeletion.refreshAfterSongDeletion();
        } catch (SecurityException e) {
          IntentSender intentSender = null;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
              intentSender = MediaStore.createDeleteRequest(
                      context.getContentResolver(),
                      Collections.singletonList(dataUri)
              ).getIntentSender();
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          } else {
            if (e instanceof RecoverableSecurityException) {
              RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) e;
              intentSender = recoverableSecurityException.getUserAction()
                      .getActionIntent()
                      .getIntentSender();
            }
          }
          if (intentSender != null) {
            deletetionIntentSenderLauncher.launch(new IntentSenderRequest.Builder(intentSender).build());
          }
        }
      });
  }

  private static Uri getContentUriFromFilePath(Context context, String filePath) {
    Uri contentUri = null;
    Cursor cursor = null;

    try {
      String[] projection = {MediaStore.MediaColumns._ID};
      String selection = MediaStore.MediaColumns.DATA + "=?";
      String[] selectionArgs = new String[]{filePath};

      cursor = context.getContentResolver().query(
              MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,  // Use appropriate MediaStore for your file type
              projection,
              selection,
              selectionArgs,
              null
      );

      if (cursor != null && cursor.moveToFirst()) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
        contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return contentUri;
  }

  /** This methods grabs the thumbnails for all songs retrieved with the help
   * of ThumbnailUtils. As this may take a long time (ca. 50-100 thumbnails per seconds)
   * this is done in parallel by as many runtime processors as possible as background task.
   *
   * @param songFiles
   */
  public static void grabThumbnails(List<File> songFiles){
    AsyncTask.execute(() -> {
      loadThumbnails(songFiles);
    });
  }

  private static void loadThumbnails(List<File> songFiles){
    // Use number of available processors to determine thread pool size
    int processors = Runtime.getRuntime().availableProcessors();
    // Create a thread pool with size based on available processors
    ExecutorService executor = Executors.newFixedThreadPool(processors);

      for (File songFile : songFiles) {
        if(thumbnailVault.containsKey(songFile.getPath()))continue;
        executor.submit(() -> {
          try {
            retrieveBitmapFromAudioFile(songFile.getPath());
            Log.i("ThumbnailProcessor", "processed "+thumbnailVault.size()+" of "+songFiles.size());
          } catch (Exception e) {
            // Log error but continue processing other files
            Log.e("ThumbnailProcessor", "Error processing " + songFile.getPath(), e);
          }
        });
      }

      // Shutdown the executor and wait for all tasks to complete
      executor.shutdown();
      try {
        // Wait up to 5 minutes for all tasks to complete
        executor.awaitTermination(5, TimeUnit.MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        Log.e("ThumbnailProcessor", "Thumbnail processing interrupted", e);
      }
  }

  private static Bitmap retrieveBitmapFromAudioFile(String filePath) {
    try {
      Bitmap bitmap = ThumbnailUtils.createAudioThumbnail(
              filePath,
              MediaStore.Images.Thumbnails.MINI_KIND
      );
      thumbnailVault.put(filePath, bitmap);
      return bitmap;
    } catch (Exception e) {
      Log.e("ThumbnailResolver", "Error loading thumbnail", e);
      return null;
    }
  }

  private static List<File> getAllAudioFiles(Context context) {
    List<File> audioFiles = Collections.synchronizedList(new ArrayList<>());

    // Get all storage paths including SD card
    List<String> storagePaths = getStoragePaths(context);

    // First try MediaStore for indexed files
    getAudioFilesFromMediaStore(context, audioFiles);

    // Access shared preferences
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    boolean enableSDCardMusic = sharedPreferences.getBoolean("EnableSDCardMusic", false);

    if(enableSDCardMusic){
      // Create thread pool based on available processors
      int processors = Runtime.getRuntime().availableProcessors();
      ExecutorService executor = Executors.newFixedThreadPool(processors);
      CountDownLatch latch = new CountDownLatch(storagePaths.size());

      // Process each storage path in parallel
      for (String storagePath : storagePaths) {
        executor.submit(() -> {
          try {
            scanDirectoryRecursively(new File(storagePath), audioFiles);
          } finally {
            latch.countDown();
          }
        });
      }

      try {
        // Wait for all scanning to complete with a timeout
        if (!latch.await(5, TimeUnit.MINUTES)) {
          Log.w(TAG, "Scanning timeout reached");
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        Log.e(TAG, "Scanning interrupted", e);
      } finally {
        executor.shutdown();
      }
    }

    return audioFiles;
  }

  private static List<String> getStoragePaths(Context context) {
    List<String> paths = new ArrayList<>();

    // Add primary external storage
    paths.add(Environment.getExternalStorageDirectory().getAbsolutePath());

    // Get secondary storage (SD card) paths
    File[] externalFilesDirs = context.getExternalFilesDirs(null);
    if (externalFilesDirs != null) {
      for (File file : externalFilesDirs) {
        if (file != null) {
          String path = file.getAbsolutePath();
          // Remove Android-specific paths to get root storage path
          int index = path.indexOf("/Android/data");
          if (index > 0) {
            path = path.substring(0, index);
            if (!paths.contains(path)) {
              paths.add(path);
            }
          }
        }
      }
    }

    // Additional common SD card paths
    String[] commonPaths = {
            "/storage/sdcard1",
            "/storage/extSdCard",
            "/storage/external_SD",
            "/storage/ext_sd"
    };

    for (String path : commonPaths) {
      File file = new File(path);
      if (file.exists() && file.canRead() && !paths.contains(path)) {
        paths.add(path);
      }
    }

    return paths;
  }

  private static void getAudioFilesFromMediaStore(Context context, List<File> audioFiles) {
    if(!hasStorageMediaPermissions(context)){
      Toast.makeText(context, "No Storage Permissions!", Toast.LENGTH_SHORT).show();
    }

    // Define the columns to retrieve from the MediaStore
    String[] projection = {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
    };

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // SDK 30 or above
      projection = new String[]{
              MediaStore.Audio.Media.DATA,
              MediaStore.Audio.Media.TITLE,
              MediaStore.Audio.Media.ARTIST,
              MediaStore.Audio.Media.ALBUM,
              MediaStore.Audio.Media.GENRE,
              MediaStore.Audio.Media.DURATION,
      };
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
      int genreIndex = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ? cursor.getColumnIndex(MediaStore.Audio.Media.GENRE) : -1;
      int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

      // Iterate through the cursor to retrieve the file paths
      while (cursor.moveToNext()) {
        String filePath = cursor.getString(dataIndex);
        String name = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);
        String album = cursor.getString(albumIndex);
        String genre = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) ? cursor.getString(genreIndex) : "";
        String durationString = cursor.getString(durationIndex);

        if(filePath == null || filePath.isEmpty()
        || name == null || name.isEmpty()
        || durationString == null || durationString.isEmpty())continue;

        int duration_ms = 0;
        duration_ms = Integer.parseInt(durationString);

        File songFile = new File(filePath);
        if(!songFile.exists())continue;

        audioFiles.add(songFile);
        if(duration_ms >= Audiobook.AUDIOBOOK_CUTOFF_MS){
          Audiobook audiobook = new Audiobook(filePath,name,artist,album,genre,duration_ms,"");
          audioFileAudiobookMap.put(songFile,audiobook);
        }
        Song song = new Song(filePath,name,artist,album,genre,duration_ms,"");
        registerSongForArtistMap(song);
        registerSongForAlbumMap(song);
        registerSongForGenreMap(song);
        audiofileSongMap.put(songFile,song);

      }

      // Close the cursor
      cursor.close();
    }
  }

  private static void scanDirectoryRecursively(File directory, List<File> audioFiles) {
    if (directory == null || !directory.exists() || !directory.canRead()) {
      return;
    }

    File[] files = directory.listFiles();
    if (files == null) return;

    // Create a queue for BFS directory traversal
    Queue<File> directoriesToScan = new LinkedList<>();
    List<File> filesToProcess = new ArrayList<>();

    // Add root directory
    directoriesToScan.offer(directory);

    // Collect all files and directories first
    while (!directoriesToScan.isEmpty()) {
      File currentDir = directoriesToScan.poll();
      File[] currentFiles = currentDir.listFiles();

      if (currentFiles == null) continue;

      for (File file : currentFiles) {
        if (file.isDirectory() && file.canRead()) {
          directoriesToScan.offer(file);
        } else if (isSupportedAudioFile(file)) {
          filesToProcess.add(file);
        }
      }
    }

    // Process files in parallel using a local thread pool
    int processors = Runtime.getRuntime().availableProcessors();
    ExecutorService executor = Executors.newFixedThreadPool(processors);
    CountDownLatch latch = new CountDownLatch(filesToProcess.size());

    for (File file : filesToProcess) {
      executor.submit(() -> {
        try {
          if (!audioFiles.contains(file)) {
            processAudioFileDirectly(file, audioFiles);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    try {
      // Wait for all file processing to complete with a timeout
      if (!latch.await(2, TimeUnit.MINUTES)) {
        Log.w(TAG, "File processing timeout reached for directory: " + directory.getPath());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      Log.e(TAG, "File processing interrupted for directory: " + directory.getPath(), e);
    } finally {
      executor.shutdown();
    }
  }

  private static boolean isSupportedAudioFile(File file) {
    String name = file.getName().toLowerCase();
    return name.endsWith(".mp3") || name.endsWith(".wav") ||
            name.endsWith(".ogg") || name.endsWith(".m4a") ||
            name.endsWith(".aac") || name.endsWith(".flac");
  }

  private static void processAudioFileDirectly(File file, List<File> audioFiles) {
    synchronized (audioFiles) {
      try {
        Song song = BrowserManager.getSongFromAudioFile(file);
        if(song != null && song.getName() != null){
          audioFiles.add(file);

          if (song.getDuration_ms() >= Audiobook.AUDIOBOOK_CUTOFF_MS) {
            Audiobook audiobook = new Audiobook(song.getPath(), song.getName(),
                    song.getArtist(), song.getAlbum(), song.getGenre(), song.getDuration_ms(), "");
            audioFileAudiobookMap.put(file, audiobook);
          } else {
            registerSongForArtistMap(song);
            registerSongForAlbumMap(song);
            registerSongForGenreMap(song);
            audiofileSongMap.put(file, song);
          }
        } else {
          Log.d(TAG, "unreadable audiofile: "+file.getAbsolutePath());
        }
      } catch (Exception e) {
        Log.e(TAG, "Error processing file: " + file.getAbsolutePath(), e);
      }
    }
  }

  // Make registration methods thread-safe
  private static synchronized void registerSongForAlbumMap(Song song) {
    if (song.getAlbum() != null && !song.getAlbum().isEmpty()) {
      String albumName = song.getAlbum();
      if (!albumListMap.containsKey(albumName)) {
        albumListMap.put(albumName, Collections.synchronizedList(new ArrayList<>()));
      }
      albumListMap.get(albumName).add(song);
    }
  }

  private static synchronized void registerSongForArtistMap(Song song) {
    if (song.getArtist() != null && !song.getArtist().isEmpty()) {
      String artistName = song.getArtist();
      if (!artistListMap.containsKey(artistName)) {
        artistListMap.put(artistName, Collections.synchronizedList(new ArrayList<>()));
      }
      artistListMap.get(artistName).add(song);
    }
  }

  private static synchronized void registerSongForGenreMap(Song song) {
    if (song.getGenre() != null && !song.getGenre().isEmpty()) {
      String genreName = song.getGenre();
      if (!genreListMap.containsKey(genreName)) {
        genreListMap.put(genreName, Collections.synchronizedList(new ArrayList<>()));
      }
      genreListMap.get(genreName).add(song);
    }
  }

  public static void getThumbnailForFile(final String filePath, final ImageView view) {
    Drawable fallbackDrawable = DrawableUtils.getTintedDrawable(
            view.getContext(),
            R.drawable.ic_music,
            ColorSettingsFragment.getPrimaryColor(view.getContext())
    );

    // Set a loading indicator (optional, can be a placeholder image)
    Glide.with(view.getContext())
            .load(
              DrawableUtils.getTintedDrawable(
                      view.getContext(),
                      R.drawable.ic_music,
                      ColorSettingsFragment.getPrimaryColor(view.getContext())
              )
            ) // Placeholder or loading image
            .into(view);

    // Check if the thumbnail is in the cache
    if (thumbnailVault.containsKey(filePath)) {
      Bitmap cachedImage = thumbnailVault.get(filePath);
      if (cachedImage != null) {
        // If found in cache, load it into the ImageView asynchronously
        Glide.with(view.getContext())
                .load(cachedImage)
                .into(view);
      } else {
        // If the thumbnail is not available (null), set a fallback
        Glide.with(view.getContext())
                .load(fallbackDrawable)
                .into(view);
      }
    } else {
      // If not in cache, fetch the thumbnail in a background thread and then load it
      new Thread(() -> {
        // Retrieve the thumbnail in the background
        Bitmap bitmap = retrieveBitmapFromAudioFile(filePath);

        // Once the thumbnail is retrieved, update the UI on the main thread
        if (bitmap != null) {
          thumbnailVault.put(filePath, bitmap);

          // Update the ImageView on the UI thread
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              Glide.with(view.getContext())
                      .load(bitmap)
                      .into(view);
            }
          });
        } else {
          // If thumbnail is not available, fallback to default image
          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              Glide.with(view.getContext())
                      .load(fallbackDrawable)
                      .into(view);
            }
          });
        }
      }).start();
    }
  }

  private static boolean fileMatchesQuery(String query, File audioFile){
    return audioFile.getAbsolutePath().toLowerCase().contains(query.toLowerCase());
  }

  public static Song getSongFromPath(String songPath){
    return getSongFromAudioFile(new File(songPath));
  }

  private static boolean hasStorageMediaPermissions(Context context){
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ?
      (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED)
      : (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
  }
}
