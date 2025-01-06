package com.example.moonstonemusicplayer.model;

import android.content.Context;

import com.example.moonstonemusicplayer.model.Database.PlaylistUtil;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.SettingsActivity.SettingsModel;
import com.example.moonstonemusicplayer.model.Database.Playlist.PlaylistModel;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.api.services.drive.model.File;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GoogleDriveManager {
    private static final String TAG = GoogleDriveManager.class.getSimpleName();
    private static GoogleDriveManager instance;
    private final Drive driveService;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();
    private static final String SETTINGS_FILE = "moonstone_settings.json";
    private static final String PLAYLISTS_FILE = "moonstone_playlists.json";
    private OnDataLoadedListener dataLoadedListener;
    private GoogleSignInAccount account;


    // Interface for callbacks
    public interface OnDataLoadedListener {
        void onSettingsLoaded(SettingsModel settings);
        void onPlaylistsLoaded(List<PlaylistModel> playlists);
        void onError(String error);
    }

    private GoogleDriveManager(Context context, GoogleSignInAccount account) {
        this.account = account;

        // Initialize Google Drive credentials
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                Collections.singleton(DriveScopes.DRIVE_APPDATA)
        );
        credential.setSelectedAccount(account.getAccount());

        // Build Drive service
        driveService = new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Moonstone Music Player")
                .build();
    }

    public static GoogleDriveManager getInstance(Context context, GoogleSignInAccount account) {
        if (instance == null) {
            instance = new GoogleDriveManager(context, account);
        }
        return instance;
    }

    // Set listener for data loading callbacks
    public void setOnDataLoadedListener(OnDataLoadedListener listener) {
        this.dataLoadedListener = listener;
    }

    // Save settings
    public Task<Void> saveSettings(SettingsModel settings) {
        return Tasks.call(executor, () -> {
            String jsonContent = gson.toJson(settings);
            String fileId = findFile(SETTINGS_FILE);

            if (fileId != null) {
                // Update existing file
                updateFile(fileId, jsonContent);
            } else {
                // Create new file
                createFile(SETTINGS_FILE, jsonContent);
            }
            return null;
        });
    }

    // Load settings
    public void loadSettings() {
        Tasks.call(executor, () -> {
            String fileId = findFile(SETTINGS_FILE);
            if (fileId != null) {
                String content = readFile(fileId);
                SettingsModel settings = gson.fromJson(content, SettingsModel.class);
                if (dataLoadedListener != null) {
                    dataLoadedListener.onSettingsLoaded(settings);
                }
            } else {
                // Return default settings
                SettingsModel defaultSettings = new SettingsModel();
                defaultSettings.setColorPrimary("#FF2196F3");
                defaultSettings.setColorPrimaryDark("#FF1976D2");
                defaultSettings.setColorAccent("#FF03DAC5");
                if (dataLoadedListener != null) {
                    dataLoadedListener.onSettingsLoaded(defaultSettings);
                }
            }
            return null;
        });
    }

    // Save playlists
    public Task<Void> savePlaylists(Context context) {
        return Tasks.call(executor, () -> {
            //
            List<Playlist> playlists = PlaylistUtil.getAllPlaylists(context);


            //save playlists
            String jsonContent = gson.toJson(playlists);
            String fileId = findFile(PLAYLISTS_FILE);

            if (fileId != null) {
                updateFile(fileId, jsonContent);
            } else {
                createFile(PLAYLISTS_FILE, jsonContent);
            }
            return null;
        });
    }

    // Load playlists
    public void loadPlaylists() {
        Tasks.call(executor, () -> {
            String fileId = findFile(PLAYLISTS_FILE);
            if (fileId != null) {
                String content = readFile(fileId);
                Type playlistListType = new TypeToken<ArrayList<PlaylistModel>>(){}.getType();
                List<PlaylistModel> playlists = gson.fromJson(content, playlistListType);
                if (dataLoadedListener != null) {
                    dataLoadedListener.onPlaylistsLoaded(playlists);
                }
            } else {
                if (dataLoadedListener != null) {
                    dataLoadedListener.onPlaylistsLoaded(new ArrayList<>());
                }
            }
            return null;
        });
    }

    // Helper methods for file operations
    private String findFile(String filename) throws IOException {
        FileList result = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '" + filename + "'")
                .setFields("files(id)")
                .execute();
        List<File> files = result.getFiles();
        return files != null && !files.isEmpty() ? files.get(0).getId() : null;
    }

    private void createFile(String filename, String content) throws IOException {
        File fileMetadata = new com.google.api.services.drive.model.File()
                .setName(filename)
                .setParents(Collections.singletonList("appDataFolder"));

        ByteArrayContent byteContent = ByteArrayContent.fromString("application/json", content);
        driveService.files().create(fileMetadata, byteContent)
                .setFields("id")
                .execute();
    }

    private void updateFile(String fileId, String content) throws IOException {
        ByteArrayContent byteContent = ByteArrayContent.fromString("application/json", content);
        driveService.files().update(fileId, null, byteContent).execute();
    }

    private String readFile(String fileId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        driveService.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream);
        return outputStream.toString("UTF-8");
    }


}