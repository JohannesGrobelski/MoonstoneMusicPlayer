package com.example.moonstonemusicplayer.model.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

class DBHelperLocalSongs extends SQLiteOpenHelper {

    //Angabe Klassenname für spätere Log-Ausgaben (vereinfacht das Auffinden in der Konsole)
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = DBHelperLocalSongs.class.getSimpleName();

    ///Variable für den Datenbanknamen
    private static final String DB_NAME = "song_list.db3";

    //Variable für die Datenbank-Version (Änderung bei Upgrade => DB neu anlegen)
    private static final int DB_VERSION = 1;

    //Tabellennamen
    static final String TABLE_SONG_LIST = "song_list";

    //Variablen für die Tabellenspalten
    static final String COLUMN_ID = "_id";
    static final String COLUMN_TITLE = "Title";
    static final String COLUMN_ARTIST = "Artist";
    static final String COLUMN_URI = "Uri";
    static final String COLUMN_DURATION = "Duration";
    static final String COLUMN_LAST_POSITION = "LastPosition";
    static final String COLUMN_GENRE = "Genre";
    static final String COLUMN_LYRICS = "Lyrics";
    static final String COLUMN_MEANING = "Meaning";

    //Create-Statement
    private static final String SQL_CREATE =
            "CREATE TABLE " + TABLE_SONG_LIST +
                    "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_ARTIST + " TEXT NOT NULL, "  +
                    COLUMN_URI + " TEXT NOT NULL, "  +
                    COLUMN_DURATION + " INT NOT NULL, "  +
                    COLUMN_LAST_POSITION + " INT NOT NULL, "  +
                    COLUMN_GENRE + " TEXT NOT NULL, "  +
                    COLUMN_LYRICS + " TEXT NOT NULL, "  +
                    COLUMN_MEANING + " TEXT NOT NULL)";

    //Drop-Statement
    private static final String SQL_DROP = "DROP TABLE IF EXISTS "+TABLE_SONG_LIST;

    public DBHelperLocalSongs(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
    }

    //wird aufgerufen, wenn noch keine DB angelegt wurde
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
            db.execSQL(SQL_CREATE);
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
