package com.example.moonstonemusicplayer.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class DataSource {
    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DataSource.class.getSimpleName();

    //Variablendeklaration
    private DBHelperLocalSongs dbHelperLocalSongs;
    private SQLiteDatabase databaseLocalSongs;

    private String[] columns = {
            DBHelperLocalSongs.COLUMN_ID,
            DBHelperLocalSongs.COLUMN_TITLE,
            DBHelperLocalSongs.COLUMN_ARTIST,
            DBHelperLocalSongs.COLUMN_URI,
            DBHelperLocalSongs.COLUMN_DURATION,
            DBHelperLocalSongs.COLUMN_LAST_POSITION,
            DBHelperLocalSongs.COLUMN_GENRE,
            DBHelperLocalSongs.COLUMN_LYRICS,
            DBHelperLocalSongs.COLUMN_MEANING
    };

    public DataSource(Context context){
        Log.d(LOG_TAG,"Unsere DataSource erzeugt den dbHelper");
        dbHelperLocalSongs = new DBHelperLocalSongs(context);
    }

    private void open_writable(){
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalSongs = dbHelperLocalSongs.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalSongs.getPath());
    }

    private void open_readable(){
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalSongs = dbHelperLocalSongs.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalSongs.getPath());
    }

    private void close_db(){
        Log.d(LOG_TAG, "DB mit hilfe des DBHelpers schließen");
        dbHelperLocalSongs.close();
    }

    private Song cursorToSong(Cursor cursor){
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_TITLE);
        int idArtist = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelperLocalSongs.COLUMN_MEANING);

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

    Song insertSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperLocalSongs.COLUMN_TITLE, inputSong.getTitle());
        values.put(DBHelperLocalSongs.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelperLocalSongs.COLUMN_URI, inputSong.getURI());
        values.put(DBHelperLocalSongs.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelperLocalSongs.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelperLocalSongs.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelperLocalSongs.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelperLocalSongs.COLUMN_MEANING, inputSong.getMeaning());

        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = databaseLocalSongs.insert(DBHelperLocalSongs.TABLE_SONG_LIST, null,values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = databaseLocalSongs.query(DBHelperLocalSongs.TABLE_SONG_LIST,
                columns,
                DBHelperLocalSongs.COLUMN_ID + " = " + insertID,
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
        databaseLocalSongs.delete(DBHelperLocalSongs.TABLE_SONG_LIST, DBHelperLocalSongs.COLUMN_ID+ " = "+Song.getID(),null);
        close_db();
    }

    void updateSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Update-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperLocalSongs.COLUMN_TITLE, inputSong.getTitle());
        values.put(DBHelperLocalSongs.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelperLocalSongs.COLUMN_URI, inputSong.getURI());
        values.put(DBHelperLocalSongs.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelperLocalSongs.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelperLocalSongs.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelperLocalSongs.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelperLocalSongs.COLUMN_MEANING, inputSong.getMeaning());

        open_writable();
        databaseLocalSongs.update(DBHelperLocalSongs.TABLE_SONG_LIST,values, DBHelperLocalSongs.COLUMN_ID+ " = "+inputSong.getID(),null);
        close_db();
    }

    List<Song> getAllSong(){
       List<Song> SongList = new ArrayList<>();
       String query = "SELECT * FROM "+ DBHelperLocalSongs.TABLE_SONG_LIST;

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

    void deleteAllSongs(){
        open_writable();
        String query = "DELETE FROM "+ DBHelperLocalSongs.TABLE_SONG_LIST;
        databaseLocalSongs.execSQL(query);
        close_db();
    }

}
