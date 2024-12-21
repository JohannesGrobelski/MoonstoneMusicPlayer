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
import com.example.moonstonemusicplayer.model.PlayListActivity.Audiobook;

import java.io.File;
import java.util.List;

public class AudiobookListAdapter extends ArrayAdapter<File> {
  private static final String TAG = AudiobookListAdapter.class.getSimpleName();


  private final List<File> folderAudiobookList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  public AudiobookListAdapter(@NonNull Context context, List<File> folderAudiobookList) {
    super(context, R.layout.item_row_layout,folderAudiobookList);
    this.folderAudiobookList = folderAudiobookList;
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

    //init the views of audiobookRowView
    TextView tv_folderAudiobookItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_folderAudiobookItem = rowView.findViewById(R.id.iv_item);
    tv_folderAudiobookItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_folderAudiobookItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_folderAudiobookItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    File file = folderAudiobookList.get(position);
    SwipeLayout item_row_swipe_layout = rowView.findViewById(R.id.item_row_swipe_layout);
    if(folderAudiobookList.get(position).isDirectory()){
      iv_folderAudiobookItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_folder));
      item_row_swipe_layout.setSwipeEnabled(false);
    } else {
      item_row_swipe_layout.setSwipeEnabled(true);
      iv_folderAudiobookItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_audiobook));

      TextView tv_item_artist = rowView.findViewById(R.id.tv_item_artist);
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
      TextView tv_item_genre = rowView.findViewById(R.id.tv_item_genre);
      TextView tv_item_duration = rowView.findViewById(R.id.item_tv_duration);

      Audiobook audiobook = BrowserManager.getAudiobookFromAudioFile(file);
      if(audiobook.getArtist() != null && !audiobook.getArtist().isEmpty()){
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_artist.setVisibility(View.VISIBLE);
        tv_item_artist.setText(audiobook.getArtist());
      }
      if(audiobook.getGenre() != null && !audiobook.getGenre().isEmpty()){
        ll_artist_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setVisibility(View.VISIBLE);
        tv_item_genre.setText(audiobook.getGenre());
      }
      if(audiobook.getDurationString() != null && !audiobook.getDurationString().isEmpty()){
        tv_item_duration.setVisibility(View.VISIBLE);
        tv_item_duration.setText(audiobook.getDurationString());
      }


      //init open audiobook info button
      ImageView iv_item = rowView.findViewById(R.id.iv_item);
      iv_item.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          // Handle button click
          showAudiobookInfoPopup(context, file);
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



    }
    tv_folderAudiobookItem.setText(removeFileType(file.getName()));



    return rowView;
  }

  private void showAudiobookInfoPopup(Context context, File file) {
    View popupView = LayoutInflater.from(context).inflate(R.layout.popup_song_info, null);
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
            .setView(popupView)
            .setTitle("Audiobook Information")
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

    // Set the audiobook information in the popup
    Audiobook audiobook = BrowserManager.getAudiobookFromAudioFile(file);

    titleTextView.setText(audiobook.getName());
    artistTextView.setText("Artist: " + audiobook.getArtist());
    albumTextView.setText("Album: " + audiobook.getAlbum());
    genreTextView.setText("Genre: " + audiobook.getGenre());
    durationTextView.setText("Duration: " + audiobook.getDurationString());

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
