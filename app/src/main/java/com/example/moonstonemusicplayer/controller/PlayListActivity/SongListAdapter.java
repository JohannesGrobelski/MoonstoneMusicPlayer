/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
    holder.itemView.setTag(holder.getAbsoluteAdapterPosition()); // Save position in the tag
    Object item = songList.get(holder.getAbsoluteAdapterPosition());
        
    //set the views of songRowView
    Song song = BrowserManager.getSongFromAudioFile((File) item);
    holder.tv_title.setText(song.getName());
    if(song.getArtist() != null && !song.getArtist().isEmpty()){
      holder.tv_artist_song.setVisibility(View.VISIBLE);
      holder.tv_artist_song.setText(song.getArtist());
    }
    if(song.getGenre() != null && !song.getGenre().isEmpty()){
      holder.tv_genre_song.setVisibility(View.VISIBLE);
      holder.tv_genre_song.setText(song.getGenre());
    }
    if(song.getDurationString() != null && !song.getDurationString().isEmpty()){
      holder.tv_duration_song.setVisibility(View.VISIBLE);
      holder.tv_duration_song.setText(song.getDurationString());
    }

    int nightModeFlags =
            playListActivityListener.playListActivity.getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK;

    if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
      if(((File) item).getPath().equals(selectedSongPath))holder.ll_song_background.setBackgroundColor(Color.LTGRAY);
      else holder.ll_song_background.setBackgroundColor(Color.DKGRAY);
    } else {
      if(((File) item).getPath().equals(selectedSongPath))holder.ll_song_background.setBackgroundColor(Color.LTGRAY);
      else holder.ll_song_background.setBackgroundColor(Color.WHITE);
    }

    holder.itemView.setOnClickListener(v -> handleItemClick(item, holder.getAbsoluteAdapterPosition()));

    holder.itemView.setOnLongClickListener(v -> {
      // Save the clicked position in a global variable
      lastLongClickedPosition = holder.getAbsoluteAdapterPosition();
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

    public ViewHolder(View itemView) { 
      super(itemView, R.id.iv_song_playing, false); // Using the ImageView as the drag handle
      //init the views of songRowView 
      ll_song_background = itemView.findViewById(R.id.ll_song_background);
      tv_title = itemView.findViewById(R.id.tv_name_song);

      tv_artist_song = itemView.findViewById(R.id.tv_artist_song);
      tv_genre_song = itemView.findViewById(R.id.tv_genre_song);
      tv_duration_song = itemView.findViewById(R.id.tv_duration_song);
    }
  }
}
