package com.example.moonstonemusicplayer.model.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

public class DBHelper extends SQLiteOpenHelper {

    /**
     *  TODO: tabelle playlist
     *
     *     playlistname | song
     *     -------------+--------
     *     rock         | alpha ...
     *     rock         | waf
     *     metal        | bfmv 1
     *     ....
     *
     *
     *
     *     tabelle ordner/song
     *
     *     ordner_path       | name |  typ
     *     ------------------+------+------
     *     0/Music           | Music| folder
     *     0/Music/bla.mp3   | bla  | song
     *     0/Music/blub.mp3  | blub | song
     */

    //Angabe Klassenname für spätere Log-Ausgaben (vereinfacht das Auffinden in der Konsole)
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = DBHelper.class.getSimpleName();

    ///Variable für den Datenbanknamen
    private static final String DB_NAME = "music_db.db3";

    //Variable für die Datenbank-Version (Änderung bei Upgrade => DB neu anlegen)
    private static final int DB_VERSION = 1;

    //Tabellennamen
    static final String TABLE_FOLDER_LIST = "folder_list";
    static final String TABLE_PLAYLIST_LIST = "favorite_list";
    static final String TABLE_FAVORITE_LIST = "favorite_list";
    static final String TABLE_RADIO_LIST = "radio_list";
    static final String TABLE_SONG_LIST = "song_list";

    //Variablen für die Tabellenspalten
    public static final String FOLDER_COLUMN_ID = "_id";
    public static final String FOLDER_COLUMN_PATH= "Path";
    public static final String FOLDER_COLUMN_NAME = "Name";
    public static final String FOLDER_COLUMN_TYPE = "Type";

    public static final String PLAYLIST_COLUMN_ID = "_id";
    public static final String PLAYLIST_COLUMN_SONG_ID = "SongID";

    public static final String FAVORITE_COLUMN_ID = "_id";
    public static final String FAVORITE_COLUMN_SONG_ID = "SongID";

    public static final String RADIO_COLUMN_ID = "_id";
    public static final String RADIO_COLUMN_SONG_ID = "SongID";

    public static final String SONG_COLUMN_ID = "_id";
    public static final String SONG_COLUMN_TITLE = "Title";
    public static final String SONG_COLUMN_ARTIST = "Artist";
    public static final String SONG_COLUMN_URI = "Uri";
    public static final String SONG_COLUMN_DURATION = "Duration";
    public static final String SONG_COLUMN_LAST_POSITION = "LastPosition";
    public static final String SONG_COLUMN_GENRE = "Genre";
    public static final String SONG_COLUMN_LYRICS = "Lyrics";
    public static final String SONG_COLUMN_MEANING = "Meaning";

    //Create-Statements
    private static final String SQL_CREATE_TABLE_FOLDER =
        "CREATE TABLE " + TABLE_FOLDER_LIST +
            "(" + FOLDER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            FOLDER_COLUMN_PATH + " TEXT NOT NULL, " +
            FOLDER_COLUMN_NAME + " TEXT NOT NULL, " +
            FOLDER_COLUMN_TYPE + " TEXT NOT NULL)";

    private static final String SQL_CREATE_TABLE_PLAYLIST =
        "CREATE TABLE " + TABLE_PLAYLIST_LIST +
            "(" + FOLDER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            awf + " TEXT NOT NULL, " +
            FOLDER_COLUMN_NAME + " TEXT NOT NULL, " +
            FOLDER_COLUMN_TYPE + " TEXT NOT NULL)";


    private static final String SQL_CREATE_TABLE_SONG =
            "CREATE TABLE " + TABLE_SONG_LIST +
                    "(" + SONG_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    SONG_COLUMN_TITLE + " TEXT NOT NULL, " +
                    SONG_COLUMN_ARTIST + " TEXT NOT NULL, "  +
                    SONG_COLUMN_URI + " TEXT NOT NULL, "  +
                    SONG_COLUMN_DURATION + " INT NOT NULL, "  +
                    SONG_COLUMN_LAST_POSITION + " INT NOT NULL, "  +
                    SONG_COLUMN_GENRE + " TEXT NOT NULL, "  +
                    SONG_COLUMN_LYRICS + " TEXT NOT NULL, "  +
                    SONG_COLUMN_MEANING + " TEXT NOT NULL)";



    //Drop-Statement
    private static final String SQL_DROP = "DROP TABLE IF EXISTS "+TABLE_SONG_LIST;

    public DBHelper(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
    }

    //wird aufgerufen, wenn noch keine DB angelegt wurde
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
            db.execSQL(SQL_CREATE_TABLE_SONG);
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
