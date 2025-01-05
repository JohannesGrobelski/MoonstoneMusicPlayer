/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import static com.example.moonstonemusicplayer.controller.PlayListActivity.SongListAdapter.removeFileType;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.SettingsFragment;

import java.io.File;
import java.util.List;

public class FolderListAdapter extends ArrayAdapter<File> {
  private static final String TAG = FolderListAdapter.class.getSimpleName();


  private final List<File> folderSongList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  public FolderListAdapter(@NonNull Context context, List<File> folderSongList) {
    super(context, R.layout.item_row_layout,folderSongList);
    this.folderSongList = folderSongList;
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

    File file = folderSongList.get(position);
    holder.tv_folderSongItem.setText(removeFileType(file.getName()));
    holder.tv_folderSongItem.setTextColor(SettingsFragment.getPrimaryColor(context));

    if (file.isDirectory()) {
      holder.setupFolderView(context);
    } else {
      holder.setupSongView(file);
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

  /** translate some english genres to german */
  private static String translateGenre(String genre){
    switch(genre.toLowerCase()){
      case "classical": return "Klassik";
      case "other": return "Andere";
      default: return genre;
    }
  }

  private static class ViewHolder {
    TextView tv_folderSongItem;
    ImageView iv_folderSongItem;
    TextView tv_item_artist;
    TextView tv_item_genre;
    TextView tv_item_duration;
    LinearLayout ll_artist_genre;
    LinearLayout item_row_ll_layout;

    ViewHolder(View view) {
      tv_folderSongItem = view.findViewById(R.id.tv_item_name);
      iv_folderSongItem = view.findViewById(R.id.iv_item);
      tv_item_artist = view.findViewById(R.id.tv_item_artist);
      tv_item_genre = view.findViewById(R.id.tv_item_genre);
      tv_item_duration = view.findViewById(R.id.item_tv_duration);
      ll_artist_genre = view.findViewById(R.id.ll_artist_genre);
      item_row_ll_layout = view.findViewById(R.id.item_row_ll_layout);
    }

    void resetViewStates() {
      // Reset all views to default state
      ll_artist_genre.setVisibility(View.GONE);
      tv_item_artist.setVisibility(View.GONE);
      tv_item_genre.setVisibility(View.GONE);
      tv_item_duration.setVisibility(View.GONE);

      // Completely reset the ImageView state
      iv_folderSongItem.setImageBitmap(null);
      iv_folderSongItem.setImageDrawable(null);
      iv_folderSongItem.setBackground(null);
      iv_folderSongItem.setColorFilter(null);
      ImageViewCompat.setImageTintList(iv_folderSongItem, null);
    }

    void setupFolderView(Context context) {
      iv_folderSongItem.setBackground(
              DrawableUtils.getTintedDrawable(
                      iv_folderSongItem.getContext(),
                      R.drawable.ic_folder,
                      SettingsFragment.getPrimaryColor(context)
              )
      );
      iv_folderSongItem.setOnClickListener(null);
    }

    void setupSongView(File file) {
      Context context = iv_folderSongItem.getContext();

      BrowserManager.getThumbnailForFile(file.getPath(), iv_folderSongItem);

      Song song = BrowserManager.getSongFromAudioFile(file);

      if (song.getArtist() != null && !song.getArtist().isEmpty()) {
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_artist.setVisibility(View.VISIBLE);
        tv_item_artist.setText(song.getArtist());
        tv_item_artist.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      if (song.getGenre() != null && !song.getGenre().isEmpty()) {
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setText(song.getGenre());
        tv_item_genre.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      if (song.getDurationString() != null && !song.getDurationString().isEmpty()) {
        tv_item_duration.setVisibility(View.VISIBLE);
        tv_item_duration.setText(song.getDurationString());
        tv_item_duration.setTextColor(SettingsFragment.getPrimaryColor(context));
      }

      iv_folderSongItem.setOnClickListener(v -> showSongInfoPopup(context, file));
    }
  }

}
