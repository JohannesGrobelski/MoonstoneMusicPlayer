package com.example.moonstonemusicplayer.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DBPlaylists {
    private static final String FAVORITES_PLAYLIST_NAME = "FAVORITES_MOONSTONEMUSICPLAYER_32325393434133218379432139324316239844321";
    private static final String TAG = DBPlaylists.class.getSimpleName();
    private static DBPlaylists instance;

    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DBPlaylists.class.getSimpleName();
    private static final int minSongDuration = 60000;

    //Variablendeklaration
    private DBHelperPlaylists DBHelperPlaylists;
    private static SQLiteDatabase database_playlists;


    private String[] columns = {
            DBHelperPlaylists.COLUMN_ID,
            DBHelperPlaylists.COLUMN_PLAYLIST_NAME,
            DBHelperPlaylists.COLUMN_SONG_NAME,
            DBHelperPlaylists.COLUMN_ARTIST,
            DBHelperPlaylists.COLUMN_URI,
            DBHelperPlaylists.COLUMN_DURATION,
            DBHelperPlaylists.COLUMN_LAST_POSITION,
            DBHelperPlaylists.COLUMN_GENRE,
            DBHelperPlaylists.COLUMN_LYRICS,
            DBHelperPlaylists.COLUMN_MEANING,
    };


    private DBPlaylists(Context context){
        Log.d(LOG_TAG,"Unsere DataSource erzeugt den DBHelperPlaylists");
        DBHelperPlaylists = new DBHelperPlaylists(context);
    }

    private void open_writable(){
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_playlists = DBHelperPlaylists.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_playlists.getPath());
    }

    private void open_readable(){
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_playlists = DBHelperPlaylists.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_playlists.getPath());
    }

    private void close_db(){
        Log.d(LOG_TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelperPlaylists.close();
    }

    public String[] getAllPlaylistNames() {
        Set<String> allPlaylistNames = new HashSet<>();
        String query = "SELECT "+DBHelperPlaylists.COLUMN_PLAYLIST_NAME+" FROM " + DBHelperPlaylists.TABLE_PLAYLISTS;

        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_playlists.rawQuery(query, null);
        //Wenn Cursor beim ersten Eintrag steht
        if (cursor.moveToNext()) {
            do {
                allPlaylistNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();
        allPlaylistNames.remove(FAVORITES_PLAYLIST_NAME);
        return allPlaylistNames.toArray(new String[allPlaylistNames.size()]);
    }


    public List<Song> getAllFavorites(){
        String query = "SELECT * FROM "+DBHelperPlaylists.TABLE_PLAYLISTS
            +" WHERE "+DBHelperPlaylists.COLUMN_PLAYLIST_NAME+" = \'"+FAVORITES_PLAYLIST_NAME+"\'";
        return getSongListFromQuery(query);
    }



    public void deleteFromPlaylist(Song song, String playlistname){
        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_playlists.delete(DBHelperPlaylists.TABLE_PLAYLISTS,
            DBHelperPlaylists.COLUMN_PLAYLIST_NAME+" = "+playlistname+" AND "+
                       DBHelperPlaylists.COLUMN_URI+" = "+song.getURI()+")",null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    public Song addToPlaylist(Song inputSong, String playlistname){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperPlaylists.COLUMN_PLAYLIST_NAME, playlistname);
        values.put(DBHelperPlaylists.COLUMN_SONG_NAME, inputSong.getName());
        values.put(DBHelperPlaylists.COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelperPlaylists.COLUMN_URI, inputSong.getURI());
        values.put(DBHelperPlaylists.COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelperPlaylists.COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelperPlaylists.COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelperPlaylists.COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelperPlaylists.COLUMN_MEANING, inputSong.getMeaning());

        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_playlists.insert(DBHelperPlaylists.TABLE_PLAYLISTS, null, values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = database_playlists.query(DBHelperPlaylists.TABLE_PLAYLISTS,
            columns,
            DBHelperPlaylists.COLUMN_ID + " = " + insertID,
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

    public void addToFavorites(Song song){
       addToPlaylist(song,FAVORITES_PLAYLIST_NAME);
    }

    public void deleteFromFavorites(Song song){
        deleteFromPlaylist(song,FAVORITES_PLAYLIST_NAME);
    }

    public void deletePlaylist(Playlist playlist){
        open_writable();
        database_playlists.delete(DBHelperPlaylists.TABLE_PLAYLISTS,
                DBHelperPlaylists.COLUMN_PLAYLIST_NAME+ " = "+playlist.getName()
                ,null);
        close_db();
    }

    private List<Song> searchPlaylist(String searchterm){
        String query = "SELECT * FROM "+ DBHelperPlaylists.TABLE_PLAYLISTS+" WHERE ("+
            DBHelperPlaylists.COLUMN_PLAYLIST_NAME+" LIKE \'"+"%"+searchterm+"%)";

        return null;
    }

    private List<Song> getSongListFromQuery(String query) {
        List<Song> SongList = new ArrayList<>();

        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_playlists.rawQuery(query, null);
        //Wenn Cursor beim ersten Eintrag steht
        if (cursor.moveToNext()) {
            do {
                int index = cursor.getInt(0);
                String playlistname = cursor.getString(1);
                String title = cursor.getString(2);
                String artist = cursor.getString(3);
                String uri = cursor.getString(4);
                int duration = cursor.getInt(5);
                int lastPosition = cursor.getInt(6);
                String genre = cursor.getString(7);
                String lyrics = cursor.getString(8);
                String meaning = cursor.getString(9);

                SongList.add(new Song(index, title, artist, uri, duration, lastPosition, genre, lyrics, meaning));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();

        return SongList;
    }

    public List<Playlist> getAllPlaylists() {
        List<Playlist> allPlaylists = new ArrayList<>();
        String[] allPlaylistNames = getAllPlaylistNames();
        Log.d(TAG, Arrays.toString(allPlaylistNames));
        for(String playlistName: allPlaylistNames){
            String query = "SELECT * FROM "+DBHelperPlaylists.TABLE_PLAYLISTS+" WHERE "+
                DBHelperPlaylists.COLUMN_PLAYLIST_NAME+" LIKE \'"+playlistName+"\'";
            List<Song> playlistSongs = getSongListFromQuery(query);
            allPlaylists.add(new Playlist(playlistName,playlistSongs));
        }
        return allPlaylists;
    }


    private Song cursorToSong(Cursor cursor) {
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_SONG_NAME);
        int idArtist = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelperPlaylists.COLUMN_MEANING);

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

    public static DBPlaylists getInstance(Context context){
        if(instance == null){
            instance = new DBPlaylists(context);
        }
        return instance;
    }
}
