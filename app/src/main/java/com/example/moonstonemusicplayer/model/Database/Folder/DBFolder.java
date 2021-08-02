package com.example.moonstonemusicplayer.model.Database.Folder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.Artist;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.Genre;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.escapeString;

public class DBFolder {
    //favorites is just another playlist
    private static final String TAG = DBFolder.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static DBFolder instance;


    //Variablendeklaration
    private final DBHelperFolder DBHelperFolder;
    private static SQLiteDatabase database_folder_song_list;


    private final String[] COLUMNS = {
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ID,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_FOLDER_NAME,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_SONG_NAME,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ARTIST,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_DURATION,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_GENRE,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_LYRICS,
    };


    private DBFolder(Context context){
        if(DEBUG)Log.d(TAG,"Unsere DataSource erzeugt den DBHelperFolder");
        DBHelperFolder = new DBHelperFolder(context);
    }

    private void open_writable(){
        if(DEBUG)Log.d(TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_folder_song_list = DBHelperFolder.getWritableDatabase();
        if(DEBUG)Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_folder_song_list.getPath());
    }

    private void open_readable(){
        if(DEBUG)Log.d(TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_folder_song_list = DBHelperFolder.getReadableDatabase();
        if(DEBUG)Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_folder_song_list.getPath());
    }

    private void close_db(){
        if(DEBUG)Log.d(TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelperFolder.close();
    }



    public Folder getRootFolder(){
        if(DEBUG)Log.d(TAG,"load Favorites");
        open_readable();
        String query = "SELECT * FROM "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST;
        Folder rootFolder = null;
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_folder_song_list.rawQuery(query, null);

        if(cursor.getCount() > 0){//table exists
            //init rootFolder
            cursor.moveToFirst();
            String folderName = cursor.getString(1);
            String path = cursor.getString(3);
            rootFolder = new Folder(folderName,path,null,null,null);

            if(cursor.getCount() > 1){
                cursor.moveToNext();
                rootFolder = insertChildrenToRootFolder(rootFolder,cursor);
            }
        }
        cursor.close();
        close_db();
        return rootFolder;
    }



    public List<Artist> getArtistList(){
        List<Artist> resultArtistList = new ArrayList<>();
        List<Album> albumList = getAlbumList();

        Artist currentArtist = null;
        String currentArtistName = "";
        for(Album album: albumList){
            if(album != null){
                if(!album.getArtistName().equals(currentArtistName)){
                    currentArtistName = album.getArtistName();
                    if(currentArtist != null){
                        resultArtistList.add(currentArtist);
                    }
                    currentArtist = new Artist(album.getArtistName(),new ArrayList<Album>());
                }
                if(currentArtist!=null)currentArtist.getAlbumList().add(album);
            }
        }
        if(resultArtistList.contains(currentArtist) && currentArtist != null)resultArtistList.add(currentArtist);
        return resultArtistList;
    }


    /**
     * @return
     */
    public List<Album> getAlbumList(){
        List<Album> resultAlbumList = new ArrayList<>();

        open_readable();
        String query = "SELECT * FROM "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST
            +" WHERE "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM +" IS NOT NULL"
            +" ORDER BY "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM +" DESC";

        Cursor cursor = database_folder_song_list.rawQuery(query, null);

        Album currentAlbum = Album.emptyAlbum;
        String albumName = "";

        if(cursor.getCount() > 0){//table exists
            //init rootFolder
            cursor.moveToFirst();

            do {
                //read line
                String songName = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String album = cursor.getString(5);
                String genre = cursor.getString(6);
                int duration = cursor.getInt(7);
                String lyrics = cursor.getString(8);

                //new album
                if(!album.equals(albumName)){
                    albumName = album;
                    if(currentAlbum != null){
                        resultAlbumList.add(currentAlbum);
                    }
                    currentAlbum = new Album(album,artist,new ArrayList<Song>());
                }

                //add song to current album
                currentAlbum.getSongList().add(new Song(path, songName, artist,album, genre,duration , lyrics));

            } while(cursor.moveToNext());
        }
        //add the last album
        if(!resultAlbumList.contains(currentAlbum) && currentAlbum != null)resultAlbumList.add(currentAlbum);

        cursor.close();
        close_db();
        return resultAlbumList;
    }

    public List<Genre> getGenreList() {
        List<Genre> resultGenreList = new ArrayList<>();

        open_readable();
        String query = "SELECT * FROM "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST
            +" WHERE "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_GENRE +" IS NOT NULL"
            +" ORDER BY "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_GENRE +" DESC";

        Cursor cursor = database_folder_song_list.rawQuery(query, null);

        Genre currentGenre = null;
        String genreName = "";

        if(cursor.getCount() > 0){//table exists
            //init rootFolder
            cursor.moveToFirst();

            do {
                //read line
                String songName = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String album = cursor.getString(5);
                String genre = cursor.getString(6);
                int duration = cursor.getInt(7);
                String lyrics = cursor.getString(8);

                //new album
                if(!genre.equals(genreName)){
                    genreName = genre;
                    if(currentGenre != null){
                        resultGenreList.add(currentGenre);
                    }
                    currentGenre = new Genre(genreName,new ArrayList<Song>());
                }

                //add song to current album
                if(currentGenre != null)currentGenre.getSongList().add(new Song(path, songName, artist,album, genre,duration , lyrics));

            } while(cursor.moveToNext());
        }
        //add the last album
        if(!resultGenreList.contains(currentGenre) && currentGenre != null)resultGenreList.add(currentGenre);

        cursor.close();
        close_db();
        return resultGenreList;
    }

    private Folder insertChildrenToRootFolder(Folder parent, Cursor cursor) {
        String path = "";
        if(cursor.getColumnCount() != 9 || cursor.getCount() < 2)return parent;
        do {
            //read line
            int index = cursor.getInt(0);
            String folderName = cursor.getString(1);
            String songName = cursor.getString(2);
            path = cursor.getString(3);
            String artist = cursor.getString(4);
            String album = cursor.getString(5);
            String genre = cursor.getString(6);
            int duration = cursor.getInt(7);
            String lyrics = cursor.getString(8);

            //"fix" path by deleting url scheme
            path = path.replace("file://","");

            if(!new File(path).exists()){
                if(DEBUG)Log.d(TAG,"file does not exist: "+path);
                continue;
            }

            //condition: "object is a direct child of parent" not matched
            while(!path.contains(parent.getPath()
                   ) && parent.getParent() != null) {//not the root)
                        parent = parent.getParent();
            }

            if (!folderName.isEmpty()) {
                List<Folder> children = new ArrayList<>();
                if (parent.getChildren_folders() != null) {
                    children = new ArrayList<>(Arrays.asList(parent.getChildren_folders()));
                }
                Folder folder = new Folder(folderName, path, parent, null, null);
                children.add(folder);
                parent.setChildren_folders(children.toArray(new Folder[children.size()]));

                //because folderSongList was inserted through depth-first traversal => go down (to child folder)
                parent = folder;
            } else if (!songName.isEmpty()) {
                List<Song> children = new ArrayList<>();
                if (parent.getChildren_songs() != null) {
                    children = new ArrayList<>(Arrays.asList(parent.getChildren_songs()));
                }
                Song song = new Song(path, songName, artist,album, genre,duration , lyrics);
                children.add(song);
                parent.setChildren_songs(children.toArray(new Song[children.size()]));
            } else {
                //error
            }

        } while (cursor.moveToNext()); //direct children


        //go to original parent
        while(parent.getParent() != null){
            parent = parent.getParent();
        }

        return parent;
    }


    public void updateSong(Song song){
        //öffnen der DB
        open_writable();

        ContentValues valuesNewSong = new ContentValues();
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_FOLDER_NAME,  "");
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_SONG_NAME, song.getName());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH,  song.getPath());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ARTIST, song.getArtist());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM, song.getAlbum());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_GENRE, song.getGenre());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_DURATION, song.getDuration_ms());
        valuesNewSong.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_LYRICS, song.getLyrics());

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.update(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST,
            valuesNewSong,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH + " = '" +song.getPath()+ "'",
            null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }


    public void deleteSong(Song song){
        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.delete(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST,
            com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH + " = '" +song.getPath(),null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    public Song getSongFromPath(String path){
        //öffnen der DB
        open_readable();

        String query = "SELECT * FROM "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST
            +" WHERE "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH + " = '" + escapeString(path)+ "'";

        Cursor cursor = database_folder_song_list.rawQuery(query, null);
        Song song = null;
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            //read line
            int index = cursor.getInt(0);
            String songName = cursor.getString(2);
            String songPath = cursor.getString(3);
            String artist = cursor.getString(4);
            String album = cursor.getString(5);
            String genre = cursor.getString(6);
            int duration = cursor.getInt(7);
            String lyrics = cursor.getString(8);
            song = new Song(songPath,songName,artist,album,genre,duration,lyrics);
        }


        cursor.close();

        //datenbank schließen und rückgabe des Songobjekts
        close_db();

        return song;
    }





    private List<Song> getAllSongsFromAlbum(String albumName){
        List<Song> allSongs = new ArrayList<>();
        //öffnen der DB
        open_readable();

        String query = "SELECT * FROM "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST +
            " WHERE "+ com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM + " = '" +escapeString(albumName)+ "'";

        Cursor cursor = database_folder_song_list.rawQuery(query, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do {
                //read line
                int index = cursor.getInt(0);
                String songName = cursor.getString(2);
                String songPath = cursor.getString(3);
                String artist = cursor.getString(4);
                String album = cursor.getString(5);
                String genre = cursor.getString(6);
                int duration = cursor.getInt(7);
                String lyrics = cursor.getString(8);
                allSongs.add(new Song(songPath,songName,artist,album,genre,duration,lyrics));
            } while (cursor.moveToNext());
        }
        cursor.close();

        //datenbank schließen und rückgabe des Songobjekts
        close_db();

        return allSongs;
    }






    public void deleteTable(){
        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.delete(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST,
            null,null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }



    /**
     * opens db, calls addToFolderSongListRecursive and closes db
     * @param folderSong
     */
    public void addToFolderSongList(Object folderSong){
        //öffnen der DB
        open_writable();

        addToFolderSongListRecursive(folderSong);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    /**
     * adds Song to table or recursively adds Folder and all its children to table
     * @param folderSong
     */
    private void addToFolderSongListRecursive(Object folderSong){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();

        if(folderSong instanceof Folder){
            if(DEBUG)Log.d(TAG,"addToFolderSongList Folder: "+((Folder) folderSong).getName());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_SONG_NAME,  "");
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_FOLDER_NAME,  ((Folder) folderSong).getName());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH,  ((Folder) folderSong).getPath());

            database_folder_song_list.insert(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST, null, values);

            if(((Folder) folderSong).getChildren_folders() != null){
                for(Folder childFolder: ((Folder) folderSong).getChildren_folders()){
                    addToFolderSongListRecursive(childFolder);
                }
            }
            if(((Folder) folderSong).getChildren_songs() != null){
                for(Song childSong: ((Folder) folderSong).getChildren_songs()){
                    addToFolderSongListRecursive(childSong);
                }
            }
        } else if(folderSong instanceof Song){
            if(DEBUG)Log.d(TAG,"addToFolderSongList Song: "+((Song) folderSong).getName());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_FOLDER_NAME,  "");
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_SONG_NAME, ((Song) folderSong).getName());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_PATH,  ((Song) folderSong).getPath());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ARTIST, ((Song) folderSong).getArtist());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_ALBUM, ((Song) folderSong).getAlbum());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_GENRE, ((Song) folderSong).getGenre());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_DURATION, ((Song) folderSong).getDuration_ms());
            values.put(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.COLUMN_LYRICS, ((Song) folderSong).getLyrics());

            database_folder_song_list.insert(com.example.moonstonemusicplayer.model.Database.Folder.DBHelperFolder.TABLE_FOLDER_SONGLIST, null, values);
        } else {
            if(DEBUG)Log.d(TAG,"addToFolderSongList error: not a song|folder");

        }
    }


    public static DBFolder getInstance(Context context){
        if(instance == null){
            instance = new DBFolder(context);
        }
        return instance;
    }



}
