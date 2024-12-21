/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.GenreFragment;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.example.moonstonemusicplayer.model.MainActivity.GenreFragment.Genre;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

/** describes how genres and the songs in it are displayed in listview*/
public class GenreListAdapter extends ArrayAdapter<Object> {

  private final List<Object> genreSongList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  public GenreListAdapter(@NonNull Context context, List<Object> genreSongList) {
    super(context, R.layout.item_row_layout,genreSongList);
    this.genreSongList = genreSongList;
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

    Song currentSong = null; Genre currentGenre = null;
    if(genreSongList.get(position) instanceof Song){
      currentSong = ((Song) genreSongList.get(position));
    } else if(genreSongList.get(position) instanceof Genre){
      currentGenre = ((Genre) genreSongList.get(position));
    } else {return rowView;}

    //init the views of songRowView
    TextView tv_genreSongItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_genreSongItem = rowView.findViewById(R.id.iv_item);
    tv_genreSongItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_genreSongItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_genreSongItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    if(currentGenre != null){
      iv_genreSongItem.setBackground(context.getDrawable(R.drawable.ic_folder));
      tv_genreSongItem.setText(currentGenre.getName());
    } else {
      iv_genreSongItem.setBackground(context.getDrawable(R.drawable.ic_music));
      tv_genreSongItem.setText(currentSong.getName());
    }

    if(currentSong != null){
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
      TextView tv_artist_song = rowView.findViewById(R.id.tv_item_artist);
      TextView tv_duration_song = rowView.findViewById(R.id.item_tv_duration);
      TextView tv_duration_genre = rowView.findViewById(R.id.tv_item_genre);

      ll_artist_genre.setVisibility(View.VISIBLE);
      tv_artist_song.setVisibility(View.VISIBLE);
      tv_duration_song.setVisibility(View.VISIBLE);
      tv_duration_genre.setVisibility(View.VISIBLE);

      tv_artist_song.setText(currentSong.getArtist());
      if(currentSong.getArtist().isEmpty())tv_artist_song.setText("unknown artist");
      tv_duration_genre.setText(currentSong.getGenre());
      tv_duration_song.setText(currentSong.getDurationString());
    }
    return rowView;
  }

  public Object getItem(int index){
    if(index >= 0 && index <= genreSongList.size()){
      return genreSongList.get(index);
    }
    return null;
  }

}
