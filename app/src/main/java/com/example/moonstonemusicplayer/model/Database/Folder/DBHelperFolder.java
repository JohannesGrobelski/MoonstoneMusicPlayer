package com.example.moonstonemusicplayer.model.Database.Folder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

public class DBHelperFolder extends SQLiteOpenHelper {

    /**
     *  Table maps playlistname to song
     *
     *     folder_song_name | path      | song_artist ...
     *     -----------------+-----------------------------
     *     rock             | alpha     |  this and all following
     *     rock             | waf       |  columns are empty for
     *     bach             | basf      |  folders
     *     ....
     */

    //Angabe Klassenname für spätere Log-Ausgaben (vereinfacht das Auffinden in der Konsole)
    private static final boolean DEBUG = true;
    private static final String LOG_TAG = DBHelperFolder.class.getSimpleName();

    ///Variable für den Datenbanknamen
    private static final String DB_NAME = "folder_song_list.db3";

    //Variable für die Datenbank-Version (Änderung bei Upgrade => DB neu anlegen)
    private static final int DB_VERSION = 1;

    //Tabellennamen
    static final String TABLE_FOLDER_SONGLIST = "folder_song_list";

    //Variablen für die Tabellenspalten
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOLDER_NAME = "FolderName";
    public static final String COLUMN_SONG_NAME = "SongName";
    public static final String COLUMN_PATH = "Path";
    public static final String COLUMN_ARTIST = "Artist";
    public static final String COLUMN_DURATION = "Duration";
    public static final String COLUMN_LAST_POSITION = "LastPosition";
    public static final String COLUMN_GENRE = "Genre";
    public static final String COLUMN_LYRICS = "Lyrics";
    public static final String COLUMN_MEANING = "Meaning";

    //Create-Statements
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_FOLDER_SONGLIST +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FOLDER_NAME + " TEXT, " +
                    COLUMN_SONG_NAME + " TEXT, " +
                    COLUMN_PATH + " TEXT NOT NULL, " +
                    COLUMN_ARTIST + " TEXT, "  +
                    COLUMN_DURATION + " INT, "  +
                    COLUMN_LAST_POSITION + " INT, "  +
                    COLUMN_GENRE + " TEXT, "  +
                    COLUMN_LYRICS + " TEXT, "  +
                    COLUMN_MEANING + " TEXT)";

    //Drop-Statement
    private static final String SQL_DROP = "DROP TABLE IF EXISTS "+ TABLE_FOLDER_SONGLIST;


    public DBHelperFolder(@NonNull Context context) {
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
