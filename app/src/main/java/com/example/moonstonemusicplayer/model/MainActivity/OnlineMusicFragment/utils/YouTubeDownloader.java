package com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class YouTubeDownloader extends AsyncTask<String, Void, String> {
    private final Activity activity;

    public YouTubeDownloader(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        String videoUrl = params[0];
        String title = params[1];

        try {
            // Create a folder named "Download" in the external storage directory
            File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "YTMusic");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            // Set the output directory for downloaded video
            File outputFile = new File(outputDir, title+".mp3");

            // Download the video and convert it to MP3
            YoutubeDLRequest request = new YoutubeDLRequest(videoUrl, outputDir.getAbsolutePath());
            request.setOption("-x"); // Extract audio
            request.setOption("--audio-format", "mp3"); // Convert to MP3
            request.setOption( "--output", outputFile.getAbsolutePath());  // Set output file name

            YoutubeDLResponse response = YoutubeDL.execute(request);

            if (response.getExitCode() == 0) {
                return outputFile.getName();
            } else {
                return null;
            }
        } catch (YoutubeDLException e) {
            e.printStackTrace();
            return null;
        }
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
            Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity,"Downloading song "+fileTitle,Toast.LENGTH_LONG).show();
            execute(videoUrl,fileTitle);
        }
    }

}