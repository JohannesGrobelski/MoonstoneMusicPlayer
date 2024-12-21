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

import com.daimajia.swipe.SwipeLayout;
import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

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
    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.item_row_layout, parent, false);
    }

    //init the views of songRowView
    TextView tv_folderSongItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_folderSongItem = rowView.findViewById(R.id.iv_item);
    tv_folderSongItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_folderSongItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_folderSongItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    File file = folderSongList.get(position);
    SwipeLayout item_row_swipe_layout = rowView.findViewById(R.id.item_row_swipe_layout);
    if(folderSongList.get(position).isDirectory()){
      iv_folderSongItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_folder));
      item_row_swipe_layout.setSwipeEnabled(false);
    } else {
      item_row_swipe_layout.setSwipeEnabled(true);
      iv_folderSongItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_music));

      TextView tv_item_artist = rowView.findViewById(R.id.tv_item_artist);
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
      TextView tv_item_genre = rowView.findViewById(R.id.tv_item_genre);
      TextView tv_item_duration = rowView.findViewById(R.id.item_tv_duration);

      Song song = BrowserManager.getSongFromAudioFile(file);
      if(song.getArtist() != null && !song.getArtist().isEmpty()){
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_artist.setVisibility(View.VISIBLE);
        tv_item_artist.setText(song.getArtist());
      }
      if(song.getGenre() != null && !song.getGenre().isEmpty()){
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setText(song.getGenre());
      }
      if(song.getDurationString() != null && !song.getDurationString().isEmpty()){
        tv_item_duration.setVisibility(View.VISIBLE);
        tv_item_duration.setText(song.getDurationString());
      }


      //init open song info button
      ImageView iv_item = rowView.findViewById(R.id.iv_item);
      iv_item.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          // Handle button click
          showSongInfoPopup(context, file);
        }
      });

      //set show mode.
      item_row_swipe_layout.setShowMode(SwipeLayout.ShowMode.LayDown);

      //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
      item_row_swipe_layout.addDrag(SwipeLayout.DragEdge.Left, rowView.findViewById(R.id.add_remove_favorites));
      item_row_swipe_layout.addDrag(SwipeLayout.DragEdge.Right, rowView.findViewById(R.id.add_to_playlist));

      item_row_swipe_layout.addSwipeListener(new SwipeLayout.SwipeListener() {
        @Override
        public void onStartOpen(SwipeLayout layout) {

        }

        @Override
        public void onOpen(SwipeLayout layout) {

        }

        @Override
        public void onStartClose(SwipeLayout layout) {

        }

        @Override
        public void onClose(SwipeLayout layout) {

        }

        @Override
        public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

        }

        @Override
        public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

        }
      });

      ImageButton add_remove_favorites = rowView.findViewById(R.id.add_remove_favorites);
      final boolean[] isInFavorites = {DBPlaylists.getInstance(context).isInFavorites(context, song)};
      if(isInFavorites[0]){
        add_remove_favorites.setImageResource(R.drawable.is_favorites);
      } else {
        add_remove_favorites.setImageResource(R.drawable.is_not_favorites);
      }

      add_remove_favorites.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if(isInFavorites[0]){
            Toast.makeText(context, "Remove from favorites: "+song.getName(), Toast.LENGTH_SHORT).show();
            DBPlaylists.getInstance(context).removeFromFavorites(context,song);
            add_remove_favorites.setImageResource(R.drawable.is_not_favorites);
          } else {
            Toast.makeText(context, "Added to favorites: "+song.getName(), Toast.LENGTH_SHORT).show();
            DBPlaylists.getInstance(context).addToFavorites(context,song);
            add_remove_favorites.setImageResource(R.drawable.is_favorites);
          }
          isInFavorites[0] = !isInFavorites[0];

          item_row_swipe_layout.close(true);
        }
      });

      ImageButton add_to_playlist = rowView.findViewById(R.id.add_to_playlist);
      add_to_playlist.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          FolderFragmentListener.showAlertDialogAddToPlaylists(layoutInflater, context, song);
          item_row_swipe_layout.close(true);
        }
      });

    }
    tv_folderSongItem.setText(removeFileType(file.getName()));



    return rowView;
  }

  private void showSongInfoPopup(Context context, File file) {
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



}
