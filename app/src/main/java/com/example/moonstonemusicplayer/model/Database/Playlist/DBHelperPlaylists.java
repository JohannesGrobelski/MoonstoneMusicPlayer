package com.example.moonstonemusicplayer.model.Database.Playlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

public class DBHelperPlaylists extends SQLiteOpenHelper {

    /**
     *  Table maps playlistname to song
     *
     *     playlistname | song_name | song_artist ...
     *     -------------+-----------
     *     rock         | alpha     |  ...
     *     rock         | waf       |
     *     bach         | basf      |
     *     ....
     */

    //Angabe Klassenname für spätere Log-Ausgaben (vereinfacht das Auffinden in der Konsole)
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = DBHelperPlaylists.class.getSimpleName();

    ///Variable für den Datenbanknamen
    public static final String DB_NAME = "playlists.db3";

    //Variable für die Datenbank-Version (Änderung bei Upgrade => DB neu anlegen)
    private static final int DB_VERSION = 1;

    //Tabellennamen
    static final String TABLE_PLAYLISTS = "playlists";

    //Variablen für die Tabellenspalten
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PLAYLIST_NAME = "PlaylistName";
    public static final String COLUMN_SONG_PATH = "SongURL";

    //Create-Statements
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_PLAYLISTS +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PLAYLIST_NAME + " TEXT NOT NULL, " +
                    COLUMN_SONG_PATH + " TEXT NOT NULL)";



    //Drop-Statement
    private static final String SQL_DROP = "DROP TABLE IF EXISTS "+TABLE_PLAYLISTS;


    public DBHelperPlaylists(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG,"DBHelper erzeugt die Datenbank "+getDatabaseName());

    }

    //wird aufgerufen, wenn noch keine DB angelegt wurde
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
            db.execSQL(SQL_CREATE_TABLE);
        } catch (Exception e){
            if(DEBUG)Log.d(LOG_TAG,"Fehler beim anlegen: "+e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(DEBUG)Log.d(LOG_TAG,"Tabelle mit Versionsnumbmer: "+DB_VERSION+" wird entfernt.");
        db.execSQL(SQL_DROP);
        if(DEBUG)Log.d(LOG_TAG,"Die neue Tabelle wird hinzugefügt.");
        onCreate(db);
    }
}
