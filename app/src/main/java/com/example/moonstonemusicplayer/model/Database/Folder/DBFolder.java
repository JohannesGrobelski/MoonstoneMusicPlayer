package com.example.moonstonemusicplayer.model.Database.Folder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBFolder {
    //favorites is just another playlist
    private static final String FAVORITES_PLAYLIST_NAME = "FAVORITES_MOONSTONEMUSICPLAYER_32325393434133218384916498164861498515687949184994971679";

    private static final String TAG = DBFolder.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static DBFolder instance;

    //Variablendeklaration
    private DBHelperFolder DBHelperFolder;
    private static SQLiteDatabase database_folder_song_list;


    private String[] COLUMNS = {
            DBHelperFolder.COLUMN_ID,
            DBHelperFolder.COLUMN_FOLDER_NAME,
            DBHelperFolder.COLUMN_SONG_NAME,
            DBHelperFolder.COLUMN_PATH,
            DBHelperFolder.COLUMN_ARTIST,
            DBHelperFolder.COLUMN_DURATION,
            DBHelperFolder.COLUMN_LAST_POSITION,
            DBHelperFolder.COLUMN_GENRE,
            DBHelperFolder.COLUMN_LYRICS,
            DBHelperFolder.COLUMN_MEANING,
    };


    private DBFolder(Context context){
        Log.d(TAG,"Unsere DataSource erzeugt den DBHelperFolder");
        DBHelperFolder = new DBHelperFolder(context);
    }

    private void open_writable(){
        Log.d(TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_folder_song_list = DBHelperFolder.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_folder_song_list.getPath());
    }

    private void open_readable(){
        Log.d(TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_folder_song_list = DBHelperFolder.getReadableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_folder_song_list.getPath());
    }

    private void close_db(){
        Log.d(TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelperFolder.close();
    }


    public Folder getRootFolder(){
        if(DEBUG)Log.d(TAG,"load Favorites");
        String query = "SELECT * FROM "+DBHelperFolder.TABLE_FOLDER_SONGLIST;
        Folder rootFolder = null;
        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_folder_song_list.rawQuery(query, null);

        if(cursor.getCount() > 0){//table exists
            //init rootFolder
            cursor.moveToFirst();
            String folderName = cursor.getString(1);
            String path = cursor.getString(3);
            rootFolder = new Folder(folderName,path,null,null,null);
            cursor.moveToNext();

            rootFolder = insertChildrenToRootFolder(rootFolder,cursor);
        }

        cursor.close();
        close_db();

        return rootFolder;
    }

    private Folder insertChildrenToRootFolder(Folder parent, Cursor cursor) {
        String path = "";
        do {
            //read line
            int index = cursor.getInt(0);
            String folderName = cursor.getString(1);
            String songName = cursor.getString(2);
            path = cursor.getString(3);
            String artist = cursor.getString(4);
            int duration = cursor.getInt(5);
            int lastPosition = cursor.getInt(6);
            String genre = cursor.getString(7);
            String lyrics = cursor.getString(8);
            String meaning = cursor.getString(9);

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
                Song song = new Song(index, songName, artist,"file:///"+path, duration, lastPosition, genre, lyrics, meaning);
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
        valuesNewSong.put(DBHelperFolder.COLUMN_FOLDER_NAME,  "");
        valuesNewSong.put(DBHelperFolder.COLUMN_SONG_NAME, song.getName());
        valuesNewSong.put(DBHelperFolder.COLUMN_PATH,  song.getURI());
        valuesNewSong.put(DBHelperFolder.COLUMN_ARTIST, song.getArtist());
        valuesNewSong.put(DBHelperFolder.COLUMN_DURATION, song.getDuration_ms());
        valuesNewSong.put(DBHelperFolder.COLUMN_LAST_POSITION, song.getLastPosition());
        valuesNewSong.put(DBHelperFolder.COLUMN_GENRE, song.getGenre());
        valuesNewSong.put(DBHelperFolder.COLUMN_LYRICS, song.getLyrics());
        valuesNewSong.put(DBHelperFolder.COLUMN_MEANING, song.getMeaning());

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.update(DBHelperFolder.TABLE_FOLDER_SONGLIST,
            valuesNewSong,
            DBHelperFolder.COLUMN_PATH+" = \'"+song.getURI()+"\'",
            null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }


    public void deleteSong(Song song){
        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.delete(DBHelperFolder.TABLE_FOLDER_SONGLIST,
            DBHelperFolder.COLUMN_PATH+" = \'"+song.getURI(),null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    public Song getSongFromURL(String url){
        //öffnen der DB
        open_readable();

        String query = "SELECT * FROM "+DBHelperFolder.TABLE_FOLDER_SONGLIST
            +" WHERE "+DBHelperFolder.COLUMN_PATH+" = \'"+url+"\'";

        Cursor cursor = database_folder_song_list.rawQuery(query, null);
        Song song = null;
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            //read line
            int index = cursor.getInt(0);
            String songName = cursor.getString(2);
            String uri = cursor.getString(3);
            String artist = cursor.getString(4);
            int duration = cursor.getInt(5);
            int lastPosition = cursor.getInt(6);
            String genre = cursor.getString(7);
            String lyrics = cursor.getString(8);
            String meaning = cursor.getString(9);
            song = new Song(index,songName,artist,uri,duration,lastPosition,genre,lyrics,meaning);
        }


        cursor.close();

        //datenbank schließen und rückgabe des Songobjekts
        close_db();


        return song;
    }



    public void deleteTable(){
        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        database_folder_song_list.delete(DBHelperFolder.TABLE_FOLDER_SONGLIST,
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
            Log.d(TAG,"addToFolderSongList Folder: "+((Folder) folderSong).getName());
            values.put(DBHelperFolder.COLUMN_SONG_NAME,  "");
            values.put(DBHelperFolder.COLUMN_FOLDER_NAME,  ((Folder) folderSong).getName());
            values.put(DBHelperFolder.COLUMN_PATH,  ((Folder) folderSong).getPath());

            database_folder_song_list.insert(DBHelperFolder.TABLE_FOLDER_SONGLIST, null, values);

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
            Log.d(TAG,"addToFolderSongList Song: "+((Song) folderSong).getName());
            values.put(DBHelperFolder.COLUMN_FOLDER_NAME,  "");
            values.put(DBHelperFolder.COLUMN_SONG_NAME, ((Song) folderSong).getName());
            values.put(DBHelperFolder.COLUMN_PATH,  ((Song) folderSong).getURI());
            values.put(DBHelperFolder.COLUMN_ARTIST, ((Song) folderSong).getArtist());
            values.put(DBHelperFolder.COLUMN_DURATION, ((Song) folderSong).getDuration_ms());
            values.put(DBHelperFolder.COLUMN_LAST_POSITION, ((Song) folderSong).getLastPosition());
            values.put(DBHelperFolder.COLUMN_GENRE, ((Song) folderSong).getGenre());
            values.put(DBHelperFolder.COLUMN_LYRICS, ((Song) folderSong).getLyrics());
            values.put(DBHelperFolder.COLUMN_MEANING, ((Song) folderSong).getMeaning());

            database_folder_song_list.insert(DBHelperFolder.TABLE_FOLDER_SONGLIST, null, values);
        } else {
            Log.d(TAG,"addToFolderSongList error: not a song|folder");

        }
    }


    public static DBFolder getInstance(Context context){
        if(instance == null){
            instance = new DBFolder(context);
        }
        return instance;
    }


}
