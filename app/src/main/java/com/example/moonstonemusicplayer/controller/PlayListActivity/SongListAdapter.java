/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.controller.PlayListActivity.PlayListActivityListener;
import com.woxthebox.draglistview.DragItemAdapter;
import java.io.File;
import java.util.List;

public class SongListAdapter extends DragItemAdapter<Object, SongListAdapter.ViewHolder> {
  private int lastLongClickedPosition = -1; // Default to an invalid position
  private static final String TAG = SongListAdapter.class.getSimpleName();

  private final List<Object> songList;
  private String selectedSongPath = "";

  private final PlayListActivityListener playListActivityListener;
  public SongListAdapter(@NonNull PlayListActivityListener playListActivityListener, List<Object> songList) {
    super();
    this.songList = songList;
    this.playListActivityListener = playListActivityListener;
    setItemList(songList);
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_row_layout, parent, false);
        return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    super.onBindViewHolder(holder, holder.getAbsoluteAdapterPosition());
    int absolutePosition = holder.getAbsoluteAdapterPosition();
    holder.itemView.setTag(absolutePosition);

    // Reset view states
    holder.resetViewStates();

    Object item = songList.get(absolutePosition);
    File file = (File) item;
    Song song = BrowserManager.getSongFromAudioFile(file);

    // Bind the song data
    holder.bindSong(song);

    // Set background color based on selection and theme
    holder.updateBackground(
            file.getPath().equals(selectedSongPath),
            playListActivityListener.playListActivity.getResources().getConfiguration()
    );

    // Set click listeners
    holder.itemView.setOnClickListener(v -> handleItemClick(item, absolutePosition));
    holder.itemView.setOnLongClickListener(v -> {
      lastLongClickedPosition = absolutePosition;
      v.showContextMenu();
      return true;
    });
  }

  public int getLastLongClickedPosition() {
    return lastLongClickedPosition;
  }

  private void handleItemClick(Object clickItem, int position) {
    if (clickItem != null) {
      playListActivityListener.onItemClick(position);
    }
  }

  @Override
  public long getUniqueItemId(int position) {
    return songList.get(position).hashCode();
  }

  public void setSelectedSongPath(String selectedSongPath) {
    this.selectedSongPath = selectedSongPath;
  }

  public static String removeFileType(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    }
    return fileName;
  }

  /** Holds the views of the Adapter for later use (mostly in onCreateViewHolder method)
   *
   */
  public static class ViewHolder extends DragItemAdapter.ViewHolder {
    LinearLayout ll_song_background;
    TextView tv_title;
    TextView tv_artist_song;
    TextView tv_genre_song;
    TextView tv_duration_song;
    ImageView iv_song_playing;

    public ViewHolder(View itemView) {
      super(itemView, R.id.iv_song_playing, false);
      ll_song_background = itemView.findViewById(R.id.ll_song_background);
      tv_title = itemView.findViewById(R.id.tv_name_song);
      tv_artist_song = itemView.findViewById(R.id.tv_artist_song);
      tv_genre_song = itemView.findViewById(R.id.tv_genre_song);
      tv_duration_song = itemView.findViewById(R.id.tv_duration_song);
      iv_song_playing = itemView.findViewById(R.id.iv_song_playing);
    }

    void resetViewStates() {
      // Reset all views to default state
      tv_title.setText("");
      tv_artist_song.setVisibility(View.GONE);
      tv_genre_song.setVisibility(View.GONE);
      tv_duration_song.setVisibility(View.GONE);
      ll_song_background.setBackgroundColor(Color.TRANSPARENT);

      // Completely reset the ImageView state
      iv_song_playing.setImageBitmap(null);
      iv_song_playing.setImageDrawable(null);
      iv_song_playing.setBackground(null);
      iv_song_playing.setColorFilter(null);
      ImageViewCompat.setImageTintList(iv_song_playing, null);
    }

    void bindSong(Song song) {
      Context context = iv_song_playing.getContext();
      tv_title.setText(song.getName());

      if (song.getArtist() != null && !song.getArtist().isEmpty()) {
        tv_artist_song.setVisibility(View.VISIBLE);
        tv_artist_song.setText(song.getArtist());
      }

      if (song.getGenre() != null && !song.getGenre().isEmpty()) {
        tv_genre_song.setVisibility(View.VISIBLE);
        tv_genre_song.setText(song.getGenre());
      }

      if (song.getDurationString() != null && !song.getDurationString().isEmpty()) {
        tv_duration_song.setVisibility(View.VISIBLE);
        tv_duration_song.setText(song.getDurationString());
      }

      Bitmap image = BrowserManager.getThumbnailForFile(song.getPath());
      if (image != null) {
        // Clear any background and tint before setting the bitmap
        itemView.setBackground(null);
        iv_song_playing.setColorFilter(null);
        ImageViewCompat.setImageTintList(iv_song_playing, null);
        iv_song_playing.setImageBitmap(image);
      } else {
        // Set default music icon with tint
        iv_song_playing.setImageBitmap(null);
        iv_song_playing.setBackground(ContextCompat.getDrawable(context, R.drawable.ic_music));
        ImageViewCompat.setImageTintList(iv_song_playing,
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));
        iv_song_playing.setColorFilter(
                ContextCompat.getColor(context, R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
      }

    }

    void updateBackground(boolean isSelected, Configuration configuration) {
      int nightModeFlags = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
      boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

      if (isSelected) {
        ll_song_background.setBackgroundColor(Color.LTGRAY);
      } else {
        ll_song_background.setBackgroundColor(isNightMode ? Color.DKGRAY : Color.WHITE);
      }
    }
  }
}
