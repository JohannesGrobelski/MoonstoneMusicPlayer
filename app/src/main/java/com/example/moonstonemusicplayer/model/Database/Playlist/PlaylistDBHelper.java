package com.example.moonstonemusicplayer.model.Database.Playlist;
import androidx.room.*;

@Database(entities = {PlaylistEntry.class}, version = 1)
public abstract class PlaylistDBHelper extends RoomDatabase {
    public abstract PlaylistDao playlistEntryDao();
}
