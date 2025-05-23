/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment;


import static com.example.moonstonemusicplayer.controller.PlayListActivity.SongListAdapter.removeFileType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;

import java.io.File;
import java.util.List;

public class AudiobookListAdapter extends ArrayAdapter<File> {
  private static final String TAG = AudiobookListAdapter.class.getSimpleName();
  private final List<File> folderAudiobookList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  public AudiobookListAdapter(@NonNull Context context, List<File> folderAudiobookList) {
    super(context, R.layout.item_row_layout, folderAudiobookList);
    this.folderAudiobookList = folderAudiobookList;
    this.context = context;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      convertView = layoutInflater.inflate(R.layout.item_row_layout, parent, false);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    // Reset view states
    holder.resetViewStates();

    File file = folderAudiobookList.get(position);
    holder.tv_item.setText(removeFileType(file.getName()));
    holder.tv_item.setTextColor(SettingsFragment.getPrimaryColor(context));

    if (file.isDirectory()) {
      holder.setupFolderView(context);
    } else {
      holder.setupAudiobookView(context, file);
    }

    return convertView;
  }

  private static void showSongInfoPopup(Context context, File file) {
    View popupView = LayoutInflater.from(context).inflate(R.layout.popup_song_info, null);
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
            .setView(popupView)
            .setTitle("Song Information")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            });

    TextView titleTextView = popupView.findViewById(R.id.titleTextView);
    TextView artistTextView = popupView.findViewById(R.id.artistTextView);
    TextView albumTextView = popupView.findViewById(R.id.albumTextView);
    TextView genreTextView = popupView.findViewById(R.id.genreTextView);
    TextView durationTextView = popupView.findViewById(R.id.durationTextView);

    // Set the song information in the popup
    Song song = BrowserManager.getSongFromAudioFile(file);

    titleTextView.setText(song.getName());
    artistTextView.setText("Artist: " + song.getArtist());
    albumTextView.setText("Album: " + song.getAlbum());
    genreTextView.setText("Genre: " + song.getGenre());
    durationTextView.setText("Duration: " + song.getDurationString());

    AlertDialog dialog = dialogBuilder.create();
    dialog.show();
  }


  private static class ViewHolder {
    TextView tv_item;
    ImageView iv_item;
    TextView tv_artist;
    TextView tv_genre;
    TextView tv_duration;
    LinearLayout ll_artist_genre;
    LinearLayout item_row_layout;

    ViewHolder(View view) {
      tv_item = view.findViewById(R.id.tv_item_name);
      iv_item = view.findViewById(R.id.iv_item);
      tv_artist = view.findViewById(R.id.tv_item_artist);
      tv_genre = view.findViewById(R.id.tv_item_genre);
      tv_duration = view.findViewById(R.id.item_tv_duration);
      ll_artist_genre = view.findViewById(R.id.ll_artist_genre);
      item_row_layout = view.findViewById(R.id.item_row_ll_layout);
    }

    void resetViewStates() {
      // Reset all views to default state
      ll_artist_genre.setVisibility(View.GONE);
      tv_artist.setVisibility(View.GONE);
      tv_genre.setVisibility(View.GONE);
      tv_duration.setVisibility(View.GONE);

      // Reset ImageView state
      iv_item.setImageDrawable(null);
      iv_item.setBackground(null);
      iv_item.setColorFilter(null);
      ImageViewCompat.setImageTintList(iv_item, null);
      iv_item.setOnClickListener(null);
    }

    void setupFolderView(Context context) {
      iv_item.setBackground(
              DrawableUtils.getTintedDrawable(
                      iv_item.getContext(),
                      R.drawable.ic_folder,
                      SettingsFragment.getPrimaryColor(context)
              )
      );
    }

    void setupAudiobookView(Context context, File file) {
      Audiobook audiobook = BrowserManager.getAudiobookFromAudioFile(file);
      BrowserManager.getThumbnailForFile(file.getPath(), iv_item);

      if (audiobook.getArtist() != null && !audiobook.getArtist().isEmpty()) {
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_artist.setVisibility(View.VISIBLE);
        tv_artist.setText(audiobook.getArtist());
        tv_artist.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      if (audiobook.getGenre() != null && !audiobook.getGenre().isEmpty()) {
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_genre.setVisibility(View.VISIBLE);
        tv_genre.setText(audiobook.getGenre());
        tv_genre.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      if (audiobook.getDurationString() != null && !audiobook.getDurationString().isEmpty()) {
        tv_duration.setVisibility(View.VISIBLE);
        tv_duration.setText(audiobook.getDurationString());
        tv_duration.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      iv_item.setOnClickListener(v -> showSongInfoPopup(context, file));
    }

  }
}