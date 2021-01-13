package com.example.moonstonemusicplayer.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

public class DBSonglists {
    private static final String FAVORITES_PLAYLIST_NAME = "FAVORITES_MOONSTONEMUSICPLAYER_32325393434133218379432139324316239844321";
    private static DBSonglists instance;

    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DBSonglists.class.getSimpleName();
    private static final int minSongDuration = 60000;

    //Variablendeklaration
    private DBHelperSonglist DBHelperSonglist;
    private static SQLiteDatabase database_music;

    private String[] columnsSonglist = {
        DBHelperSonglist.COLUMN_ID,
        DBHelperSonglist.COLUMN_TITLE,
        DBHelperSonglist.COLUMN_ARTIST,
        DBHelperSonglist.COLUMN_URI,
        DBHelperSonglist.COLUMN_DURATION,
        DBHelperSonglist.COLUMN_LAST_POSITION,
        DBHelperSonglist.COLUMN_GENRE,
        DBHelperSonglist.COLUMN_LYRICS,
        DBHelperSonglist.COLUMN_MEANING
    };

    private DBSonglists(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt den DBHelperLocalSongs");
        DBHelperSonglist = new DBHelperSonglist(context);
    }

    private void open_writable() {
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_music = DBHelperSonglist.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: " + database_music.getPath());
    }

    private void open_readable() {
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_music = DBHelperSonglist.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: " + database_music.getPath());
    }

    private void close_db() {
        Log.d(LOG_TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelperSonglist.close();
    }


    private Song cursorToSong(Cursor cursor) {
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelperSonglist.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelperSonglist.COLUMN_TITLE);
        int idArtist = cursor.getColumnIndex(DBHelperSonglist.COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelperSonglist.COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelperSonglist.COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelperSonglist.COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelperSonglist.COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelperSonglist.COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelperSonglist.COLUMN_MEANING);

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
        return new Song(index, title, artist, uri, duration, lastPosition, genre, lyrics, meaning);
    }

    private Song getSongByID(String ID) {
        Song song = null;
        String query = "SELECT * FROM " + DBHelperSonglist.TABLE_SONG_LIST +
            " WHERE " + DBHelperSonglist.COLUMN_ID + " = " + ID + ")";

        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_music.rawQuery(query, null);
        //Wenn Cursor beim ersten Eintrag steht
        if (cursor.moveToNext()) {
            do {
                int index = cursor.getInt(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String uri = cursor.getString(3);
                int duration = cursor.getInt(4);
                int lastPosition = cursor.getInt(5);
                String genre = cursor.getString(6);
                String lyrics = cursor.getString(7);
                String meaning = cursor.getString(8);

                song = new Song(index, title, artist, uri, duration, lastPosition, genre, lyrics, meaning);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();
        return song;
    }

    List<Song> insertSongList(List<Song> songList) {
        //öffnen der DB
        open_writable();
        for (Song inputSong : songList) {
            //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
            ContentValues values = new ContentValues();
            values.put(DBHelperSonglist.COLUMN_TITLE, inputSong.getName());
            values.put(DBHelperSonglist.COLUMN_ARTIST, inputSong.getArtist());
            values.put(DBHelperSonglist.COLUMN_URI, inputSong.getURI());
            values.put(DBHelperSonglist.COLUMN_DURATION, inputSong.getDuration_ms());
            values.put(DBHelperSonglist.COLUMN_LAST_POSITION, inputSong.getLastPosition());
            values.put(DBHelperSonglist.COLUMN_GENRE, inputSong.getGenre());
            values.put(DBHelperSonglist.COLUMN_LYRICS, inputSong.getLyrics());
            values.put(DBHelperSonglist.COLUMN_MEANING, inputSong.getMeaning());

            //Song-Objekt in DB einfügen und ID zurückbekommen
            long insertID = database_music.insert(DBHelperSonglist.TABLE_SONG_LIST, null, values);

            //Zeiger auf gerade eingefügtes Element
            Cursor cursor = database_music.query(DBHelperSonglist.TABLE_SONG_LIST,
                columnsSonglist,
                DBHelperSonglist.COLUMN_ID + " = " + insertID,
                null, null, null, null);

            //Zeiger auf Anfang bringen
            cursor.moveToFirst();

            //zeiger zerstören
            cursor.close();
        }
        //datenbank schließen und rückgabe des Songobjekts
        close_db();
        return songList;
    }


    Song insertSong(Song inputSong) {
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperSonglist.COLUMN_TITLE, inputSong.getName());
        values.put(DBHelperSonglist.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelperSonglist.COLUMN_URI, inputSong.getURI());
        values.put(DBHelperSonglist.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelperSonglist.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelperSonglist.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelperSonglist.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelperSonglist.COLUMN_MEANING, inputSong.getMeaning());

        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_music.insert(DBHelperSonglist.TABLE_SONG_LIST, null, values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = database_music.query(DBHelperSonglist.TABLE_SONG_LIST,
            columnsSonglist,
            DBHelperSonglist.COLUMN_ID + " = " + insertID,
            null, null, null, null);

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

    void deleteSong(Song Song) {
        open_writable();
        database_music.delete(DBHelperSonglist.TABLE_SONG_LIST, DBHelperSonglist.COLUMN_ID + " = " + Song.getID(), null);
        close_db();
    }

    void updateSong(Song inputSong) {
        //Anlegen von Wertepaaren zur Übergabe in Update-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperSonglist.COLUMN_TITLE, inputSong.getName());
        values.put(DBHelperSonglist.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelperSonglist.COLUMN_URI, inputSong.getURI());
        values.put(DBHelperSonglist.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelperSonglist.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelperSonglist.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelperSonglist.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelperSonglist.COLUMN_MEANING, inputSong.getMeaning());

        open_writable();
        database_music.update(DBHelperSonglist.TABLE_SONG_LIST, values, DBHelperSonglist.COLUMN_ID + " = " + inputSong.getID(), null);
        close_db();
    }

    List<Song> getAllSong(int minduration) {
        List<Song> SongList = new ArrayList<>();
        String query = "SELECT * FROM " + DBHelperSonglist.TABLE_SONG_LIST + " WHERE " + DBHelperSonglist.COLUMN_DURATION + " >= " + minduration;
        return getSongListFromQuery(query);
    }

    public void deleteAllSongs() {
        open_writable();
        String query = "DELETE FROM " + DBHelperSonglist.TABLE_SONG_LIST;
        database_music.execSQL(query);
        close_db();
    }

    public List<Song> searchSongs(String searchterm) {
        /*
        String query = "SELECT * FROM "+DBHelperLocalSongs.TABLE_SONG_LIST+" WHERE "+
            "instr("+DBHelperLocalSongs.COLUMN_TITLE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_MEANING+", \'"+searchterm+"\') > 0";
         */
        //TODO: es wird nur nach titeln gesucht
        String query = "SELECT * FROM " + DBHelperSonglist.TABLE_SONG_LIST + " WHERE (" +
            DBHelperSonglist.COLUMN_TITLE + " LIKE \'" + "%" + searchterm + "%" + "\' OR " + // search column for match containing substring searchterm (% is wildcard)
            DBHelperSonglist.COLUMN_ARTIST + " LIKE \'" + "%" + searchterm + "%" + "\') AND (" +
            DBHelperSonglist.COLUMN_DURATION + " >= " + minSongDuration + ")";

            /* OR "+
            DBHelperLocalSongs.COLUMN_GENRE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperLocalSongs.COLUMN_LYRICS+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperLocalSongs.COLUMN_MEANING+" LIKE \'"+"%"+searchterm+"%"+"\'";
            */
        Log.d("query", query);

        /*    "instr("+DBHelperLocalSongs.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperLocalSongs.COLUMN_MEANING+", \'"+searchterm+"\') > 0"; //case-insensitive search*/
        return getSongListFromQuery(query);
    }

    public List<Song> sortBy(String var, String mode) {
        String query = "SELECT * FROM " + DBHelperSonglist.TABLE_SONG_LIST +
            " WHERE " + DBHelperSonglist.COLUMN_DURATION + " >= 60000" +
            " ORDER BY " + var + " " + mode;
        return getSongListFromQuery(query);
    }

    private List<Song> getSongListFromQuery(String query) {
        List<Song> SongList = new ArrayList<>();


        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_music.rawQuery(query, null);
        //Wenn Cursor beim ersten Eintrag steht
        if (cursor.moveToNext()) {
            do {
                int index = cursor.getInt(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String uri = cursor.getString(3);
                int duration = cursor.getInt(4);
                int lastPosition = cursor.getInt(5);
                String genre = cursor.getString(6);
                String lyrics = cursor.getString(7);
                String meaning = cursor.getString(8);

                SongList.add(new Song(index, title, artist, uri, duration, lastPosition, genre, lyrics, meaning));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();

        return SongList;
    }

    public static DBSonglists getInstance(Context context) {
        if (instance == null) {
            instance = new DBSonglists(context);
        }
        return instance;
    }
}
