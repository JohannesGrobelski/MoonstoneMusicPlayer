package com.example.moonstonemusicplayer.model.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.MainActivity.RadioFragment.Radio;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataSourceSingleton {
    private static DataSourceSingleton instance;

    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DataSourceSingleton.class.getSimpleName();
    private static final int minSongDuration = 60000;

    //Variablendeklaration
    private DBHelper DBHelper;
    private static SQLiteDatabase database_music;


    private String[] columnsPlaylist = {
            DBHelper.PLAYLIST_COLUMN_ID,
            DBHelper.PLAYLIST_COLUMN_NAME,
            DBHelper.PLAYLIST_COLUMN_SONG_URI
    };

    private String[] columnsSonglist = {
            DBHelper.SONG_COLUMN_ID,
            DBHelper.SONG_COLUMN_TITLE,
            DBHelper.SONG_COLUMN_ARTIST,
            DBHelper.SONG_COLUMN_URI,
            DBHelper.SONG_COLUMN_DURATION,
            DBHelper.SONG_COLUMN_LAST_POSITION,
            DBHelper.SONG_COLUMN_GENRE,
            DBHelper.SONG_COLUMN_LYRICS,
            DBHelper.SONG_COLUMN_MEANING
    };

    private DataSourceSingleton(Context context){
        Log.d(LOG_TAG,"Unsere DataSource erzeugt den DBHelperLocalSongs");
        DBHelper = new DBHelper(context);
    }

    private void open_writable(){
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_music = DBHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_music.getPath());
    }

    private void open_readable(){
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_music = DBHelper.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_music.getPath());
    }

    private void close_db(){
        Log.d(LOG_TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        DBHelper.close();
    }



    private Song cursorToSong(Cursor cursor){
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelper.SONG_COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelper.SONG_COLUMN_TITLE);
        int idArtist = cursor.getColumnIndex(DBHelper.SONG_COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelper.SONG_COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelper.SONG_COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelper.SONG_COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelper.SONG_COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelper.SONG_COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelper.SONG_COLUMN_MEANING);

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

    private Song getSongByID(String ID){
        Song song = null;
        String query = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+
                " WHERE "+ DBHelper.SONG_COLUMN_ID+" = "+ID+")";

        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_music.rawQuery(query,null);
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

                song = new Song(index,title,artist,uri,duration,lastPosition,genre,lyrics,meaning);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();
        return song;
    }

    List<Song> insertSongList(List<Song> songList){
        //öffnen der DB
        open_writable();
        for(Song inputSong:songList){
            //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
            ContentValues values = new ContentValues();
            values.put(DBHelper.SONG_COLUMN_TITLE, inputSong.getName());
            values.put(DBHelper.SONG_COLUMN_ARTIST, inputSong.getArtist());
            values.put(DBHelper.SONG_COLUMN_URI, inputSong.getURI());
            values.put(DBHelper.SONG_COLUMN_DURATION, inputSong.getDuration_ms());
            values.put(DBHelper.SONG_COLUMN_LAST_POSITION, inputSong.getLastPosition());
            values.put(DBHelper.SONG_COLUMN_GENRE, inputSong.getGenre());
            values.put(DBHelper.SONG_COLUMN_LYRICS, inputSong.getLyrics());
            values.put(DBHelper.SONG_COLUMN_MEANING, inputSong.getMeaning());

            //Song-Objekt in DB einfügen und ID zurückbekommen
            long insertID = database_music.insert(DBHelper.TABLE_SONG_LIST, null,values);

            //Zeiger auf gerade eingefügtes Element
            Cursor cursor = database_music.query(DBHelper.TABLE_SONG_LIST,
                columnsSonglist,
                DBHelper.SONG_COLUMN_ID + " = " + insertID,
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

    List<Song> insertPlaylist(Playlist playlist){
        //öffnen der DB
        open_writable();
        for(Song inputSong: playlist.getPlaylist()){
            //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
            ContentValues values = new ContentValues();
            values.put(DBHelper.PLAYLIST_COLUMN_NAME, playlist.getName());
            values.put(DBHelper.PLAYLIST_COLUMN_SONG_URI, inputSong.getURI());

            //Song-Objekt in DB einfügen und ID zurückbekommen
            long insertID = database_music.insert(DBHelper.TABLE_PLAYLIST_LIST, null,values);

            //Zeiger auf gerade eingefügtes Element
            Cursor cursor = database_music.query(DBHelper.TABLE_PLAYLIST_LIST,
                    columnsSonglist,
                    DBHelper.SONG_COLUMN_ID + " = " + insertID,
                    null,null,null,null);

            //Zeiger auf Anfang bringen
            cursor.moveToFirst();

            //zeiger zerstören
            cursor.close();
        }
        //datenbank schließen und rückgabe des Songobjekts
        close_db();
        return playlist.getPlaylist();
    }

    public void deleteSongToPlaylist(String songUri, String playlistname){
        //öffnen der DB
        open_writable();

        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.PLAYLIST_COLUMN_NAME, playlistname);
        values.put(DBHelper.PLAYLIST_COLUMN_SONG_URI, songUri);



        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_music.delete(DBHelper.TABLE_PLAYLIST_LIST, DBHelper.PLAYLIST_COLUMN_NAME+" = "+playlistname+" AND "+
            DBHelper.SONG_COLUMN_URI+" = "+songUri+")",null);

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    public void insertSongToPlaylist(String songUri, String playlistname){
        //öffnen der DB
        open_writable();

        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.PLAYLIST_COLUMN_NAME, playlistname);
        values.put(DBHelper.PLAYLIST_COLUMN_SONG_URI, songUri);

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_music.insert(DBHelper.TABLE_PLAYLIST_LIST, null,values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = database_music.query(DBHelper.TABLE_PLAYLIST_LIST,
                columnsSonglist,
                DBHelper.SONG_COLUMN_ID + " = " + insertID,
                null,null,null,null);

        //Zeiger auf Anfang bringen
        cursor.moveToFirst();

        //zeiger zerstören
        cursor.close();

        //datenbank schließen und rückgabe des Songobjekts
        close_db();
    }

    void deleteSong(String songUri, String playlistname){
        open_writable();
        database_music.delete(DBHelper.TABLE_PLAYLIST_LIST,
                DBHelper.PLAYLIST_COLUMN_SONG_URI+ " = "+songUri+" AND "+
                DBHelper.PLAYLIST_COLUMN_NAME+" = "+playlistname,null);
        close_db();
    }

    Song insertSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.SONG_COLUMN_TITLE, inputSong.getName());
        values.put(DBHelper.SONG_COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelper.SONG_COLUMN_URI, inputSong.getURI());
        values.put(DBHelper.SONG_COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelper.SONG_COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelper.SONG_COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelper.SONG_COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelper.SONG_COLUMN_MEANING, inputSong.getMeaning());

        //öffnen der DB
        open_writable();

        //Song-Objekt in DB einfügen und ID zurückbekommen
        long insertID = database_music.insert(DBHelper.TABLE_SONG_LIST, null,values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = database_music.query(DBHelper.TABLE_SONG_LIST,
                columnsSonglist,
                DBHelper.SONG_COLUMN_ID + " = " + insertID,
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
        database_music.delete(DBHelper.TABLE_SONG_LIST, DBHelper.SONG_COLUMN_ID+ " = "+Song.getID(),null);
        close_db();
    }

    void deletePlaylist(Playlist playlist){
        open_writable();
        database_music.delete(DBHelper.TABLE_SONG_LIST,
                DBHelper.PLAYLIST_COLUMN_NAME+ " = "+playlist.getName()
                ,null);
        close_db();
    }

    void updateSong(Song inputSong){
        //Anlegen von Wertepaaren zur Übergabe in Update-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelper.SONG_COLUMN_TITLE, inputSong.getName());
        values.put(DBHelper.SONG_COLUMN_ARTIST, inputSong.getArtist());
        values.put(DBHelper.SONG_COLUMN_URI, inputSong.getURI());
        values.put(DBHelper.SONG_COLUMN_DURATION, inputSong.getDuration_ms());
        values.put(DBHelper.SONG_COLUMN_LAST_POSITION, inputSong.getLastPosition());
        values.put(DBHelper.SONG_COLUMN_GENRE, inputSong.getGenre());
        values.put(DBHelper.SONG_COLUMN_LYRICS, inputSong.getLyrics());
        values.put(DBHelper.SONG_COLUMN_MEANING, inputSong.getMeaning());

        open_writable();
        database_music.update(DBHelper.TABLE_SONG_LIST,values, DBHelper.SONG_COLUMN_ID+ " = "+inputSong.getID(),null);
        close_db();
    }

    void updatePlaylist(Playlist playlist){
        deletePlaylist(playlist);
        insertPlaylist(playlist);
    }

    List<Song> getAllSong(int minduration){
       List<Song> SongList = new ArrayList<>();
       String query = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+" WHERE "+ DBHelper.SONG_COLUMN_DURATION+" >= "+minduration;
       return getSongListFromQuery(query);
    }

    public void deleteAllSongs(){
        open_writable();
        String query = "DELETE FROM "+ DBHelper.TABLE_SONG_LIST;
        database_music.execSQL(query);
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
            DBHelper.SONG_COLUMN_TITLE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+ // search column for match containing substring searchterm (% is wildcard)
            DBHelper.SONG_COLUMN_ARTIST+" LIKE \'"+"%"+searchterm+"%"+"\') AND ("+
            DBHelper.SONG_COLUMN_DURATION+" >= "+ minSongDuration +")";

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
            " WHERE "+ DBHelper.SONG_COLUMN_DURATION+" >= 60000"+
            " ORDER BY " + var+" "+mode;
        return getSongListFromQuery(query);
    }

    private List<Song> getSongListFromQuery(String query){
        List<Song> SongList = new ArrayList<>();


        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_music.rawQuery(query,null);
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

    private List<Playlist> getPlaylistsFromQuery(String query){
        List<Playlist> playlists = new ArrayList<>();

        Map<String,List<Song>> playlistMap = new LinkedHashMap<>();

        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = database_music.rawQuery(query,null);
        //Wenn Cursor beim ersten Eintrag steht
        if(cursor.moveToNext()){
            do{
               int index = cursor.getInt(0);
               String name = cursor.getString(1);
               String uri = cursor.getString(2);

               String songQuery = "SELECT * FROM "+ DBHelper.TABLE_SONG_LIST+
                        " WHERE "+ DBHelper.SONG_COLUMN_URI+" = "+uri;

               Song song = getSongListFromQuery(songQuery).get(0);

               List<Song> newSongList = new ArrayList<>();
               if(playlistMap.keySet().contains(name)){
                   newSongList = playlistMap.get(name);
               }
               newSongList.add(song);
               playlistMap.put(name,newSongList);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();

        //create List<Playlist> from playlistmap
        for(String playlistname: playlistMap.keySet()){
            Playlist playlist = new Playlist(playlistname,playlistMap.get(playlistname));
            playlists.add(playlist);
        }

        return playlists;
    }



    public static DataSourceSingleton getInstance(Context context){
        if(instance == null){
            instance = new DataSourceSingleton(context);
        }
        return instance;
    }

    public List<Playlist> getAllPlaylists() {
       return getPlaylistsFromQuery("SELECT * FROM "+DBHelper.TABLE_SONG_LIST);
    }

    //TODO
    public List<Song> getAllFavorites() {
        ArrayList<Song> songList = new ArrayList<>();
        songList.add(new Song("EXAMPLE","EXAMPLEARTIST","EXAMPLEURL",0));
        return songList;
    }

    //TODO
    public List<Radio> getAllRadios() {
        ArrayList<Radio> radioList = new ArrayList<>();
        radioList.add(new Radio("EXAMPLE","EXAMPLEURL"));
        return radioList;
    }
}
