package com.example.moonstonemusicplayer.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class DataSourceSingleton {



    private static DataSourceSingleton instance;

    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DataSourceSingleton.class.getSimpleName();
    private static final int minSongDuration = 60000;

    //Variablendeklaration
    private DBHelper DBHelper;
    private static SQLiteDatabase databaseLocalSongs;

    private String[] columnsSonglist = {
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_TITLE,
            DBHelper.COLUMN_ARTIST,
            DBHelper.COLUMN_URI,
            DBHelper.COLUMN_DURATION,
            DBHelper.COLUMN_LAST_POSITION,
            DBHelper.COLUMN_GENRE,
            DBHelper.COLUMN_LYRICS,
            DBHelper.COLUMN_MEANING
    };

    private DataSourceSingleton(Context context){
        Log.d(LOG_TAG,"Unsere DataSource erzeugt den DBHelperLocalSongs");
        DBHelper = new DBHelper(context);
    }

    private void open_writable(){
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalSongs = DBHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalSongs.getPath());
    }

    private void open_readable(){
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalSongs = DBHelper.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalSongs.getPath());
    }

    private void close_db(){
        Log.d(LOG_TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelper.close();
    }

    private Song cursorToSong(Cursor cursor){
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelper.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelper.COLUMN_TITLE);
        int idArtist = cursor.getColumnIndex(DBHelper.COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelper.COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelper.COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelper.COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelper.COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelper.COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelper.COLUMN_MEANING);

        //get values from indezes
        int index = cursor.getInt(idIndex);
        String title = cursor.getString(idTitle);
        String artist = cursor.getString(idArtist);
        String uri = cursor.getString(idURI);
        int duration = cursor.getInt(idDuration);
        int lastPosition = cursor.getInt(idLastPosition);
        String genre = cursor.getString(idGenre);
        String lyrics = cursor.getString(idLyrics);
        String meaning = cursor.getString(idMeaning);

        //create Song from values
        return new Song(index,title,artist,uri,duration,lastPosition,genre,lyrics,meaning);
    }

    List<Song> insertSongList(List<Song> songList){
        //öffnen der DB
        open_writable();
        for(Song inputSong:songList){
            //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_TITLE, inputSong.getTitle());
            values.put(DBHelper.COLUMN_ARTIST, inputSong.getArtist());
            values.put(DBHelper.COLUMN_URI, inputSong.getURI());
            values.put(DBHelper.COLUMN_DURATION, inputSong.getDuration_ms());
            values.put(DBHelper.COLUMN_LAST_POSITION, inputSong.getLastPosition());
            values.put(DBHelper.COLUMN_GENRE, inputSong.getGenre());
            values.put(DBHelper.COLUMN_LYRICS, inputSong.getLyrics());
            values.put(DBHelper.COLUMN_MEANING, inputSong.getMeaning());

            //Song-Objekt in DB einfügen und ID zurückbekommen
            long insertID = databaseLocalSongs.insert(DBHelper.TABLE_SONG_LIST, null,values);

            //Zeiger auf gerade eingefügtes Element
            Cursor cursor = databaseLocalSongs.query(DBHelper.TABLE_SONG_LIST,
                columnsSonglist,
                DBHelper.COLUMN_ID + " = " + insertID,
                null,null,null,null);

            //Zeiger auf Anfang bringen
            cursor.moveToFirst();

            //zeiger zerstören
            cursor.close();
        }
        //datenbank schließen und rückgabe des Songobjekts
        close_db();
        return songList;
    }

    Song insertSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_TITLE, inputSong.getTitle());
        values.put(DBHelper.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelper.COLUMN_URI, inputSong.getURI());
        values.put(DBHelper.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelper.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelper.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelper.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelper.COLUMN_MEANING, inputSong.getMeaning());

        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = databaseLocalSongs.insert(DBHelper.TABLE_SONG_LIST, null,values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = databaseLocalSongs.query(DBHelper.TABLE_SONG_LIST,
                columnsSonglist,
                DBHelper.COLUMN_ID + " = " + insertID,
                null,null,null,null);

        //Zeiger auf Anfang bringen
        cursor.moveToFirst();

        //aktuelles Element auslesen
        Song current = cursorToSong(cursor);

        //zeiger zerstören
        cursor.close();

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
        return current;
    }

    void deleteSong(Song Song){
        open_writable();
        databaseLocalSongs.delete(DBHelper.TABLE_SONG_LIST, DBHelper.COLUMN_ID+ " = "+Song.getID(),null);
        close_db();
    }

    void updateSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Update-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.COLUMN_TITLE, inputSong.getTitle());
        values.put(DBHelper.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelper.COLUMN_URI, inputSong.getURI());
        values.put(DBHelper.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelper.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelper.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelper.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelper.COLUMN_MEANING, inputSong.getMeaning());

        open_writable();
        databaseLocalSongs.update(DBHelper.TABLE_SONG_LIST,values, DBHelper.COLUMN_ID+ " = "+inputSong.getID(),null);
        close_db();
    }

    List<Song> getAllSong(int minduration){
       List<Song> SongList = new ArrayList<>();
       String query = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+" WHERE "+ DBHelper.COLUMN_DURATION+" >= "+minduration;
       return getSongListFromQuery(query);
    }

    public void deleteAllSongs(){
        open_writable();
        String query = "DELETE FROM "+ DBHelper.TABLE_SONG_LIST;
        databaseLocalSongs.execSQL(query);
        close_db();
    }

    public List<Song> searchSongs(String searchterm){
        /*
        String query = "SELECT * FROM "+DBHelperLocalSongs.TABLE_SONG_LIST+" WHERE "+
            "instr("+DBHelperLocalSongs.COLUMN_TITLE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_MEANING+", \'"+searchterm+"\') > 0";
         */
        //TODO: es wird nur nach titeln gesucht
        String query = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+" WHERE ("+
            DBHelper.COLUMN_TITLE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+ // search column for match containing substring searchterm (% is wildcard)
            DBHelper.COLUMN_ARTIST+" LIKE \'"+"%"+searchterm+"%"+"\') AND ("+
            DBHelper.COLUMN_DURATION+" >= "+ minSongDuration +")";

            /* OR "+
            DBHelperLocalSongs.COLUMN_GENRE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperLocalSongs.COLUMN_LYRICS+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperLocalSongs.COLUMN_MEANING+" LIKE \'"+"%"+searchterm+"%"+"\'";
            */
            Log.d("query",query);

        /*    "instr("+DBHelperLocalSongs.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_MEANING+", \'"+searchterm+"\') > 0"; //case-insensitive search*/
        return getSongListFromQuery(query);
    }

    public List<Song> sortBy(String var, String mode){
        String query = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+
            " WHERE "+ DBHelper.COLUMN_DURATION+" >= 60000"+
            " ORDER BY " + var+" "+mode;
        return getSongListFromQuery(query);
    }

    private List<Song> getSongListFromQuery(String query){
        List<Song> SongList = new ArrayList<>();


        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = databaseLocalSongs.rawQuery(query,null);
        //Wenn Cursor beim ersten Eintrag steht
        if(cursor.moveToNext()){
            do{
                int index = cursor.getInt(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String uri = cursor.getString(3);
                int duration = cursor.getInt(4);
                int lastPosition = cursor.getInt(5);
                String genre = cursor.getString(6);
                String lyrics = cursor.getString(7);
                String meaning = cursor.getString(8);

                SongList.add(new Song(index,title,artist,uri,duration,lastPosition,genre,lyrics,meaning));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();

        return SongList;
    }

    public static DataSourceSingleton getInstance(Context context){
        if(instance == null){
            instance = new DataSourceSingleton(context);
        }
        return instance;
    }

}
