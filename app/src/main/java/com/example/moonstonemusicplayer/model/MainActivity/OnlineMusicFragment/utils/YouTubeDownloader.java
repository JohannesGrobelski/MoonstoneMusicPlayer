package com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import com.example.moonstonemusicplayer.view.MainActivity;

import java.io.File;

public class YouTubeDownloader extends AsyncTask<String, Void, String> {


    private static final String TAG = YouTubeDownloader.class.getSimpleName();
    private final Activity activity;

    public YouTubeDownloader(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String videoUrl = params[0];
        String title = params[1];

        try {
            File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "YTMusic");
            YoutubeDLRequest request = new YoutubeDLRequest(videoUrl);
            request.addOption("-x"); // Extract audio
            request.addOption("--audio-format", "mp3"); // Convert to MP3
            request.addOption("-o", youtubeDLDir.getAbsolutePath() + "/%(title)s.%(ext)s");

            YoutubeDL.getInstance().execute(request);

            /*
            YoutubeDLRequest request = new YoutubeDLRequest(videoUrl);
            request.addOption("-o", "/sdcard/Download/youtubedl-android/%(title)s.%(ext)s");
            request.addOption("f","bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
            YoutubeDL.getInstance().execute(request)
             */
        } catch (YoutubeDLException e) {
            Log.e(TAG, "failed to initialize youtubedl-android", e);
        } catch (YoutubeDL.CanceledException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(activity, "Video downloaded and converted: " + result, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Error downloading video", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadVideo(String videoUrl, String fileTitle) {
        // Check if WRITE_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ((MainActivity) activity).requestWritePermission();
        } else {
            Toast.makeText(activity,"Downloading song "+fileTitle,Toast.LENGTH_LONG).show();
            execute(videoUrl,fileTitle);
        }
    }


}