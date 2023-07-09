package com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment;

public class VideoModel {
    String title;
    String channel;
    String duration;
    String uploadDate;
    String thumbnail;
    String videoURL;

    public VideoModel(String title, String channel, String duration, String uploadDate, String thumbnail, String videoURL) {
        this.title = title;
        this.channel = channel;
        this.duration = duration;
        this.uploadDate = uploadDate;
        this.thumbnail = thumbnail;
        this.videoURL = videoURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
}