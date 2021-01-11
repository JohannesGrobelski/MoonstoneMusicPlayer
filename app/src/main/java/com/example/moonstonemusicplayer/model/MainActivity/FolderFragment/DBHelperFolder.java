package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

/** https://stackoverflow.com/a/6802879
 * Datenbank stellt einen Folder dar (also einen Baum)
 * Hierfür werden zwei Tabellen erzeugt:
 * - FileSystem (stellt jeweils einen Knoten dar, also directory oder song)
 * - TreePath (stellt jeweils eine Kante dar, also eine Eltern-Kind Relation)
 * */
class DBHelperFolder extends SQLiteOpenHelper {

    //Angabe Klassenname für spätere Log-Ausgaben (vereinfacht das Auffinden in der Konsole)
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = DBHelperFolder.class.getSimpleName();

    ///Variable für den Datenbanknamen
    private static final String DB_NAME = "folder_tree.db3";

    //Variable für die Datenbank-Version (Änderung bei Upgrade => DB neu anlegen)
    private static final int DB_VERSION = 1;

    //Tabellennamen
    static final String TABLE_FOLDER = "folder";
    static final String TABLE_TREE_PATH = "tree_path";

    //Variablen für die Tabellenspalten von FileSystem
    //Stellen die Knoten dar
    static final String FILE_COLUMN_ID = "_id";
    static final String FILE_COLUMN_DIR = "Dir";
    static final String SONG_COLUMN_TITLE = "Title";
    static final String SONG_COLUMN_ARTIST = "Artist";
    static final String SONG_COLUMN_URI = "Uri";
    static final String SONG_COLUMN_DURATION = "Duration";
    static final String SONG_COLUMN_LAST_POSITION = "LastPosition";
    static final String SONG_COLUMN_GENRE = "Genre";
    static final String SONG_COLUMN_LYRICS = "Lyrics";
    static final String SONG_COLUMN_MEANING = "Meaning";

    //Variablen für die Tabellenspalten von TreePath
    //Stellen die Kanten dar
    static final String ANCESTOR = "ancestor";
    static final String DESCANDENT = "descendant";

    //Create-Statement für FileSystem
    private static final String SQL_CREATE_FOLDER =
            "CREATE TABLE " + TABLE_FOLDER +
                    "(" + FILE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     FILE_COLUMN_DIR + " TEXT NOT NULL, " +
                    SONG_COLUMN_TITLE + " TEXT NOT NULL, " +
                    SONG_COLUMN_ARTIST + " TEXT NOT NULL, "  +
                    SONG_COLUMN_URI + " TEXT NOT NULL, "  +
                    SONG_COLUMN_DURATION + " INT NOT NULL, "  +
                    SONG_COLUMN_LAST_POSITION + " INT NOT NULL, "  +
                    SONG_COLUMN_GENRE + " TEXT NOT NULL, "  +
                    SONG_COLUMN_LYRICS + " TEXT NOT NULL, "  +
                    SONG_COLUMN_MEANING + " TEXT NOT NULL)";

    //Create-Statement für TreePath
    private static final String SQL_CREATE_TREE_PATH =
        "CREATE TABLE " + TABLE_TREE_PATH +
            "(" + FILE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ANCESTOR + " INTEGER, " +
            DESCANDENT + " INTEGER)";

    //Drop-Statement
    private static final String SQL_DROP_FOLDER = "DROP TABLE IF EXISTS "+TABLE_FOLDER;
    private static final String SQL_DROP_TREE_PATH = "DROP TABLE IF EXISTS "+TABLE_TREE_PATH;

    public DBHelperFolder(@NonNull Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
    }

    //wird aufgerufen, wenn noch keine DB angelegt wurde
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            if(DEBUG)Log.d(LOG_TAG,"DBHelper hat die Datenbank: "+getDatabaseName()+" erzeugt.");
            db.execSQL(SQL_CREATE_FOLDER);
            db.execSQL(SQL_CREATE_TREE_PATH);
        } catch (Exception e){
            if(DEBUG)Log.d(LOG_TAG,"Fehler beim anlegen: "+e.getMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(DEBUG)Log.d(LOG_TAG,"Tabellen mit Versionsnumbmer: "+DB_VERSION+" werden entfernt.");
        db.execSQL(SQL_CREATE_FOLDER);
        db.execSQL(SQL_CREATE_TREE_PATH);
        if(DEBUG)Log.d(LOG_TAG,"Die neuen Tabellen werden hinzugefügt.");
        onCreate(db);
    }
}
