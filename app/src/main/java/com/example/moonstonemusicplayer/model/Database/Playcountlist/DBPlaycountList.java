package com.example.moonstonemusicplayer.model.Database.Playcountlist;

import static com.example.moonstonemusicplayer.model.Database.Playcountlist.DBHelperPlaycountList.COLUMN_PLAYCOUNT;
import static com.example.moonstonemusicplayer.model.Database.Playcountlist.DBHelperPlaycountList.COLUMN_SONG_PATH;
import static com.example.moonstonemusicplayer.model.Database.Playcountlist.DBHelperPlaycountList.TABLE_PLAYCOUNTLIST;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBHelperPlaylists.COLUMN_ID;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.escapeString;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.Database.Playlist.DBHelperPlaylists;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** DB to track how often a song was played by user.
 * I thought about using DBPlaylist with a special playlist, but decided otherwise because this
 * would have been a "hacky" solution. With this implementation its cleaner.
 */
public class DBPlaycountList {
    private static final String TAG = DBPlaycountList.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int SONG_NOT_PLAYED_PLAYCOUNT = 0;
    private static DBPlaycountList instance;

    private final DBHelperPlaycountList dBHelperPlaycountList;
    private static SQLiteDatabase database_playcountlist;

    private final String[] COLUMNS = {
            COLUMN_ID,
            DBHelperPlaycountList.COLUMN_SONG_PATH,
            DBHelperPlaycountList.COLUMN_PLAYCOUNT
    };

    public static DBPlaycountList getInstance(Context context){
        if(instance == null){
            instance = new DBPlaycountList(context);
        }
        return instance;
    }


    /**
     * Increments play count for a given song and updates the play count database.
     *
     * @param context The application context
     * @param song The song to be marked as played
     * @return The updated song with its current play count information
     */
    public Song playedSong(Context context, Song song){
        try {
            if(DEBUG)Log.d(TAG,"add "+song.getName()+" to playlist "+song);

            //check if song is already in playlist
            String query = "SELECT * FROM "+ TABLE_PLAYCOUNTLIST +" WHERE "+
                    com.example.moonstonemusicplayer.model.Database.Playlist.DBHelperPlaylists.COLUMN_SONG_PATH + " LIKE '" + escapeString(song.getPath())+ "'";

            int playcount = getSongplayCountFromQuery(context, query);
            open_writable();

            //Anlegen eines neuen Eintrags
            ContentValues values = new ContentValues();
            values.put(COLUMN_PLAYCOUNT, ++playcount);
            values.put(COLUMN_SONG_PATH, song.getPath());

            //Song-Objekt in DB einfügen und ID zurückbekommen
            long insertID;
            if(playcount == 1){
                insertID = database_playcountlist.insert(TABLE_PLAYCOUNTLIST, null, values);
            } else {
                String whereClause = COLUMN_SONG_PATH + " = ?";
                String[] whereArgs = new String[]{song.getPath()};
                insertID = database_playcountlist.update(TABLE_PLAYCOUNTLIST, values, whereClause, whereArgs);
            }
            Log.d(TAG,"add to playlist: "+song.getName()+" "+insertID);

            //Zeiger auf gerade eingefügtes Element
            Cursor cursor = database_playcountlist.query(TABLE_PLAYCOUNTLIST,
                    COLUMNS,
                    COLUMN_ID + " = " + insertID,
                    null, null, null, null);

            //Zeiger auf Anfang bringen
            cursor.moveToFirst();

            //current Element auslesen
            Song current = cursorToSong(context,cursor);

            //zeiger zerstören
            cursor.close();

            //datenbank schließen und rückgabe des Songobjekts
            close_db();
            return current;
        } catch (Exception e){
            Log.e(TAG,e.toString());
            return null;
        }
    }

    /**
     * Retrieves a list of songs ordered by their play count in descending order.
     *
     * @return List of songs sorted from most frequently played to least frequently played.
     *         Returns an empty list if no songs have been played or if there's an error.
     */
    public List<Song> getMostlyPlayed() {
        try {
            open_readable();
            String query = "SELECT * FROM " + TABLE_PLAYCOUNTLIST +
                    " ORDER BY " + COLUMN_PLAYCOUNT + " DESC";

            Cursor cursor = database_playcountlist.rawQuery(query, null);
            List<Song> mostPlayedSongs = new ArrayList<>();

            while (cursor.moveToNext()) {
                try {
                    int songPathColumnIndex = cursor.getColumnIndex(COLUMN_SONG_PATH);
                    String songPath = cursor.getString(songPathColumnIndex);
                    if(songPath == null)continue;
                    Song song = BrowserManager.getSongFromPath(songPath);
                    if (song != null) {
                        mostPlayedSongs.add(song);
                    }
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
            }

            cursor.close();
            close_db();

            return mostPlayedSongs;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return List.of();
        }
    }

    private DBPlaycountList(Context context){
        Log.d(TAG,"Unsere DataSource erzeugt den DBHelperPlaycountList");
        dBHelperPlaycountList = new DBHelperPlaycountList(context);
    }

    private void open_writable(){
        Log.d(TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        database_playcountlist = dBHelperPlaycountList.getWritableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_playcountlist.getPath());
    }

    private void open_readable(){
        Log.d(TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        database_playcountlist = dBHelperPlaycountList.getReadableDatabase();
        Log.d(TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ database_playcountlist.getPath());
    }

    private void close_db(){
        Log.d(TAG, "DB mit hilfe des DBHelperLocalSongss schließen");
        dBHelperPlaycountList.close();
    }

    private Integer getSongplayCountFromQuery(Context context, String query) {
        open_readable();
        Cursor cursor = database_playcountlist.rawQuery(query, null);
        int playcount = SONG_NOT_PLAYED_PLAYCOUNT;
        if (cursor.moveToNext()) {
            playcount = cursor.getInt(1);
        }
        cursor.close();
        close_db();

        return playcount;
    }

    private Song cursorToSong(Context context, Cursor cursor) {
        //get Indexes
        int idPath = cursor.getColumnIndex(com.example.moonstonemusicplayer.model.Database.Playlist.DBHelperPlaylists.COLUMN_SONG_PATH);

        //create Song from values
        return BrowserManager.getSongFromPath(cursor.getString(idPath));
    }
}
