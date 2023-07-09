package com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Pair;

import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.VideoModel;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class YoutubeAPIUtil {
    private String API_KEY;

    private Context context;

    public YoutubeAPIUtil(Context context) throws GeneralSecurityException, IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();

        try {
            // Open the config.properties file
            InputStream inputStream = assetManager.open("config.properties");

            // Load properties from the input stream
            properties.load(inputStream);

            // Close the input stream
            inputStream.close();

            API_KEY = properties.getProperty("yt_api_key");
        } catch (IOException e) {
            e.printStackTrace();
            API_KEY = null;
        }
    }

    private final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    private final JsonFactory JSON_FACTORY = Utils.getDefaultJsonFactory();

    public void searchVideosByKeyword(Context context, String keyword, Callback<List<VideoModel>, Exception> callback) {
        new AsyncTask<Void, Void, Pair<List<VideoModel>, Exception>>() {
            @Override
            protected Pair<List<VideoModel>, Exception> doInBackground(Void... params) {
                try {
                    YouTube youtube = createYouTubeClient();
                    YouTube.Search.List search = youtube.search().list("id,snippet");
                    search.setKey(API_KEY);
                    search.setQ(keyword);
                    search.setType("video");
                    search.setMaxResults(10L);

                    SearchListResponse response = search.execute();
                    List<VideoModel> videoModels = mapResponseToVideoModels(response.getItems());
                    return new Pair<>(videoModels, null);
                } catch (IOException e) {
                    return new Pair<>(null, e);
                }
            }

            @Override
            protected void onPostExecute(Pair<List<VideoModel>, Exception> result) {
                List<VideoModel> videoModels = result.first;
                Exception exception = result.second;

                callback.onResult(videoModels, exception);
            }
        }.execute();
    }

    private YouTube createYouTubeClient() {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                .setApplicationName("YoutubeAnalyser")
                .build();
    }

    private List<VideoModel> mapResponseToVideoModels(List<SearchResult> items) {
        List<VideoModel> videoModels = new ArrayList<>();
        for (SearchResult item : items) {
            VideoModel videoModel = new VideoModel(
                    item.getSnippet().getTitle(),
                    item.getSnippet().getChannelTitle(),
                    "",
                    item.getSnippet().getPublishedAt().toStringRfc3339(),
                    item.getSnippet().getThumbnails().getDefault().getUrl(),
                    "https://www.youtube.com/watch?v=" + item.getId().getVideoId()
            );
            videoModels.add(videoModel);
        }
        return videoModels;
    }


}

