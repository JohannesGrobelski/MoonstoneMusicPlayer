package com.example.moonstonemusicplayer.model.MainActivity.FolderFragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.ArrayList;
import java.util.List;

class DataSource {
    /*
    private static long ID = 0;

    //Angabe Klassenname für spätere LogAusgaben
    private static final String LOG_TAG = DataSource.class.getSimpleName();
    private static final int minDuration = 60000;

    //Variablendeklaration
    private DBHelperFolder DBHelperFolder;
    private SQLiteDatabase databaseLocalFolders;

    private String[] columns_filesytem = {
            DBHelperFolder.FILE_COLUMN_ID,
            DBHelperFolder.FILE_COLUMN_DIR,
            DBHelperFolder.SONG_COLUMN_TITLE,
            DBHelperFolder.SONG_COLUMN_ARTIST,
            DBHelperFolder.SONG_COLUMN_URI,
            DBHelperFolder.SONG_COLUMN_DURATION,
            DBHelperFolder.SONG_COLUMN_LAST_POSITION,
            DBHelperFolder.SONG_COLUMN_GENRE,
            DBHelperFolder.SONG_COLUMN_LYRICS,
            DBHelperFolder.SONG_COLUMN_MEANING
    };

    private String[] columns_treepath = {
        DBHelperFolder.ANCESTOR,
        DBHelperFolder.DESCANDENT
    };

    public DataSource(Context context){
        Log.d(LOG_TAG,"Unsere DataSource erzeugt den DBHelperFolder");
        DBHelperFolder = new DBHelperFolder(context);
    }

    private void open_writable(){
        Log.d(LOG_TAG, "Eine schreibende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalFolders = DBHelperFolder.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalFolders.getPath());
    }

    private void open_readable(){
        Log.d(LOG_TAG, "Eine lesende Referenz auf die DB wird jetzt angefragt.");
        databaseLocalFolders = DBHelperFolder.getReadableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten, Pfad zur Datenbank: "+ databaseLocalFolders.getPath());
    }

    private void close_db(){
        Log.d(LOG_TAG, "DB mit hilfe des DBHelperFolders schließen");
        DBHelperFolder.close();
    }

    private Folder cursorToFolder(Cursor cursor){
        //get Indexes
        int idIndex = cursor.getColumnIndex(DBHelperFolder.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DBHelperFolder.COLUMN_TITLE);
        int idArtist = cursor.getColumnIndex(DBHelperFolder.COLUMN_ARTIST);
        int idURI = cursor.getColumnIndex(DBHelperFolder.COLUMN_URI);
        int idDuration = cursor.getColumnIndex(DBHelperFolder.COLUMN_DURATION);
        int idLastPosition = cursor.getColumnIndex(DBHelperFolder.COLUMN_LAST_POSITION);
        int idGenre = cursor.getColumnIndex(DBHelperFolder.COLUMN_GENRE);
        int idLyrics = cursor.getColumnIndex(DBHelperFolder.COLUMN_LYRICS);
        int idMeaning = cursor.getColumnIndex(DBHelperFolder.COLUMN_MEANING);

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

        //create Folder from values
        return new Folder(index,title,artist,uri,duration,lastPosition,genre,lyrics,meaning);
    }

    // wird hier rekursiv - breath first aufgerufen um auch alle Kinder einzufügen
    Folder insertRootFolder(Folder folder){
        long IDFolder = ++ID;

        //öffnen der DB
        open_writable();

        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        //Füge den Folder ein
        ContentValues values = new ContentValues();
        values.put(DBHelperFolder.FILE_COLUMN_ID, ++ID);
        values.put(DBHelperFolder.FILE_COLUMN_DIR, folder.getName());
        long insertID = databaseLocalFolders.insert(DBHelperFolder.TABLE_FOLDER, null,values);

        values = new ContentValues();
        values.put(DBHelperFolder.AC, ++ID);
        values.put(DBHelperFolder.FILE_COLUMN_DIR, folder.getName());
        long insertID = databaseLocalFolders.insert(DBHelperFolder.TABLE_FOLDER, null,values);


        //put Connection into Table tree_path
        ContentValues values = new ContentValues();
        values.put(DBHelperFolder.FILE_COLUMN_ID, ++ID);


        //Füge seine Kind-Folder ein
        for(Folder children_folder: folder.getChildren_folders()){

        }

        //Füge seine Kind-Songs ein
        for(Song children_song: folder.getChildren_songs()){
            //put Song into Table folder
            ContentValues values = new ContentValues();
            values.put(DBHelperFolder.FILE_COLUMN_ID, ++ID);
            values.put(DBHelperFolder.SONG_COLUMN_TITLE, children_song.getTitle());
            values.put(DBHelperFolder.SONG_COLUMN_TITLE, children_song.getTitle());
            values.put(DBHelperFolder.SONG_COLUMN_ARTIST, children_song.getArtist());
            values.put(DBHelperFolder.SONG_COLUMN_URI, children_song.getURI());
            values.put(DBHelperFolder.SONG_COLUMN_DURATION, children_song.getDuration_ms());
            values.put(DBHelperFolder.SONG_COLUMN_LAST_POSITION, children_song.getLastPosition());
            values.put(DBHelperFolder.SONG_COLUMN_GENRE, children_song.getGenre());
            values.put(DBHelperFolder.SONG_COLUMN_LYRICS, children_song.getLyrics());
            values.put(DBHelperFolder.SONG_COLUMN_MEANING, children_song.getMeaning());
            long insertID = databaseLocalFolders.insert(DBHelperFolder.TABLE_FOLDER, null,values);

            //put Connection into Table tree_path
            ContentValues values = new ContentValues();
            values.put(DBHelperFolder.FILE_COLUMN_ID, ++ID);
        }



        //Folder-Objekt in DB einfügen und ID zurückbekommen




        //datenbank schließen und rückgabe des Folderobjekts
        close_db();
        return folder;
    }

    Folder insertFolder(Folder inputFolder){
        //Anlegen von Wertepaaren zur Übergabe in Insert-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperFolder.COLUMN_TITLE, inputFolder.getTitle());
        values.put(DBHelperFolder.COLUMN_ARTIST, inputFolder.getArtist());
        values.put(DBHelperFolder.COLUMN_URI, inputFolder.getURI());
        values.put(DBHelperFolder.COLUMN_DURATION, inputFolder.getDuration_ms());
        values.put(DBHelperFolder.COLUMN_LAST_POSITION, inputFolder.getLastPosition());
        values.put(DBHelperFolder.COLUMN_GENRE, inputFolder.getGenre());
        values.put(DBHelperFolder.COLUMN_LYRICS, inputFolder.getLyrics());
        values.put(DBHelperFolder.COLUMN_MEANING, inputFolder.getMeaning());

        //öffnen der DB
        open_writable();

        //Folder-Objekt in DB einfügen und ID zurückbekommen
        long insertID = databaseLocalFolders.insert(DBHelperFolder.TABLE_Folder_LIST, null,values);

        //Zeiger auf gerade eingefügtes Element
        Cursor cursor = databaseLocalFolders.query(DBHelperFolder.TABLE_Folder_LIST,
                columns,
                DBHelperFolder.COLUMN_ID + " = " + insertID,
                null,null,null,null);

        //Zeiger auf Anfang bringen
        cursor.moveToFirst();

        //aktuelles Element auslesen
        Folder current = cursorToFolder(cursor);

        //zeiger zerstören
        cursor.close();

        //datenbank schließen und rückgabe des Folderobjekts
        close_db();
        return current;
    }

    void deleteFolder(Folder Folder){
        open_writable();
        databaseLocalFolders.delete(DBHelperFolder.TABLE_Folder_LIST, DBHelperFolder.COLUMN_ID+ " = "+Folder.getID(),null);
        close_db();
    }

    void updateFolder(Folder inputFolder){
        //Anlegen von Wertepaaren zur Übergabe in Update-Methode
        ContentValues values = new ContentValues();
        values.put(DBHelperFolder.COLUMN_TITLE, inputFolder.getTitle());
        values.put(DBHelperFolder.COLUMN_ARTIST, inputFolder.getArtist());
        values.put(DBHelperFolder.COLUMN_URI, inputFolder.getURI());
        values.put(DBHelperFolder.COLUMN_DURATION, inputFolder.getDuration_ms());
        values.put(DBHelperFolder.COLUMN_LAST_POSITION, inputFolder.getLastPosition());
        values.put(DBHelperFolder.COLUMN_GENRE, inputFolder.getGenre());
        values.put(DBHelperFolder.COLUMN_LYRICS, inputFolder.getLyrics());
        values.put(DBHelperFolder.COLUMN_MEANING, inputFolder.getMeaning());

        open_writable();
        databaseLocalFolders.update(DBHelperFolder.TABLE_Folder_LIST,values, DBHelperFolder.COLUMN_ID+ " = "+inputFolder.getID(),null);
        close_db();
    }

    List<Folder> getAllFolder(int minduration){
       List<Folder> FolderList = new ArrayList<>();
       String query = "SELECT * FROM "+ DBHelperFolder.TABLE_Folder_LIST+" WHERE "+DBHelperFolder.COLUMN_DURATION+" >= "+minduration;
       return getFolderListFromQuery(query);
    }

    void deleteAllFolders(){
        open_writable();
        String query = "DELETE FROM "+ DBHelperFolder.TABLE_Folder_LIST;
        databaseLocalFolders.execSQL(query);
        close_db();
    }

    List<Folder> searchFolders(String searchterm){

        String query = "SELECT * FROM "+DBHelperFolder.TABLE_Folder_LIST+" WHERE "+
            "instr("+DBHelperFolder.COLUMN_TITLE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_MEANING+", \'"+searchterm+"\') > 0";

        //TODO: es wird nur nach titeln gesucht
        String query = "SELECT * FROM "+DBHelperFolder.TABLE_Folder_LIST+" WHERE ("+
            DBHelperFolder.COLUMN_TITLE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+ // search column for match containing substring searchterm (% is wildcard)
            DBHelperFolder.COLUMN_ARTIST+" LIKE \'"+"%"+searchterm+"%"+"\') AND ("+
            DBHelperFolder.COLUMN_DURATION+" >= "+minDuration+")";

             OR "+
            DBHelperFolder.COLUMN_GENRE+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperFolder.COLUMN_LYRICS+" LIKE \'"+"%"+searchterm+"%"+"\' OR "+
            DBHelperFolder.COLUMN_MEANING+" LIKE \'"+"%"+searchterm+"%"+"\'";

            Log.d("query",query);

            "instr("+DBHelperFolder.COLUMN_ARTIST+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_GENRE+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_LYRICS+", \'"+searchterm+"\') > 0 OR "+
            "instr("+DBHelperFolder.COLUMN_MEANING+", \'"+searchterm+"\') > 0"; //case-insensitive search
        return getFolderListFromQuery(query);
    }

    List<Folder> sortBy(String var, String mode){
        String query = "SELECT * FROM "+DBHelperFolder.TABLE_Folder_LIST+
            " WHERE "+DBHelperFolder.COLUMN_DURATION+" >= 60000"+
            " ORDER BY " + var+" "+mode;
        return getFolderListFromQuery(query);
    }

    private List<Folder> getFolderListFromQuery(String query){
        List<Folder> FolderList = new ArrayList<>();


        open_readable();
        //Zeiger auf die Einträge der Tabelle
        Cursor cursor = databaseLocalFolders.rawQuery(query,null);
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

                FolderList.add(new Folder(index,title,artist,uri,duration,lastPosition,genre,lyrics,meaning));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close_db();

        return FolderList;
    }



        */
}
