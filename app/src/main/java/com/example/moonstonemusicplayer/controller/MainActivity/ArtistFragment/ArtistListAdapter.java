/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.ArtistFragment;

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
import com.example.moonstonemusicplayer.controller.Utility.DrawableUtils;
import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.MainActivity.ArtistFragment.Artist;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.settingsactivity_fragments.ColorSettingsFragment;

import java.util.List;

/** describes how Albums and the songs in it are displayed in listview*/
public class ArtistListAdapter extends ArrayAdapter<Object> {

  private final List<Object> artistAlbumSongList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  public ArtistListAdapter(@NonNull Context context, List<Object> albumSongList) {
    super(context, R.layout.item_row_layout, albumSongList);
    this.artistAlbumSongList = albumSongList;
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

    Song currentSong = null; Album currentAlbum = null; Artist currentArtist = null;
    if(artistAlbumSongList.get(position) instanceof Song){
      currentSong = ((Song) artistAlbumSongList.get(position));
    } else if(artistAlbumSongList.get(position) instanceof Album){
      currentAlbum = ((Album) artistAlbumSongList.get(position));
    } else if(artistAlbumSongList.get(position) instanceof Artist){
      currentArtist = ((Artist) artistAlbumSongList.get(position));
    } else {return rowView;}

    //init the views of songRowView
    TextView tv_AlbumSongItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_AlbumSongItem = rowView.findViewById(R.id.iv_item);
    TextView tv_item_artist = rowView.findViewById(R.id.tv_item_artist);
    TextView tv_item_duration = rowView.findViewById(R.id.item_tv_duration);
    TextView tv_item_genre = rowView.findViewById(R.id.tv_item_genre);

    tv_AlbumSongItem.setTextColor(ColorSettingsFragment.getPrimaryColor(context));
    iv_AlbumSongItem.setColorFilter(ColorSettingsFragment.getPrimaryColor(context), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_AlbumSongItem, ColorStateList.valueOf(ColorSettingsFragment.getPrimaryColor(context)));
    tv_AlbumSongItem.setTextColor(ColorSettingsFragment.getPrimaryColor(context));

    if(currentArtist != null) {
      iv_AlbumSongItem.setBackground(
              DrawableUtils.getTintedDrawable(
                      iv_AlbumSongItem.getContext(),
                      R.drawable.ic_artist,
                      ColorSettingsFragment.getPrimaryColor(context)
              )
      );
      tv_AlbumSongItem.setText(currentArtist.getName());
    } else if(currentAlbum != null){
      iv_AlbumSongItem.setBackground(
              DrawableUtils.getTintedDrawable(
                      iv_AlbumSongItem.getContext(),
                      R.drawable.ic_music_album,
                      ColorSettingsFragment.getPrimaryColor(context)
              )
      );
      tv_AlbumSongItem.setText(currentAlbum.getName());
    } else {
      // Reset ImageView state
      iv_AlbumSongItem.setImageDrawable(null);
      iv_AlbumSongItem.setBackground(null);
      iv_AlbumSongItem.setColorFilter(null);
      ImageViewCompat.setImageTintList(iv_AlbumSongItem, null);
      //get song art
      BrowserManager.getThumbnailForFile(currentSong.getPath(), iv_AlbumSongItem);

      tv_AlbumSongItem.setText(currentSong.getName());
    }

    if(currentArtist != null) {
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
      ll_artist_genre.setVisibility(View.VISIBLE);
      tv_item_artist.setVisibility(View.VISIBLE);
      tv_item_duration.setVisibility(View.VISIBLE);

      tv_item_artist.setText(currentArtist.getName());
      if(currentArtist.getName().isEmpty())tv_item_artist.setText("unknown artist");
      tv_item_duration.setText(currentArtist.getDurationString());
    } else if(currentAlbum != null) {
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
      ll_artist_genre.setVisibility(View.VISIBLE);
      tv_item_artist.setVisibility(View.VISIBLE);
      tv_item_duration.setVisibility(View.VISIBLE);

      tv_item_duration.setText(currentAlbum.getDurationString());
    } else if(currentSong != null){
      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);

      ll_artist_genre.setVisibility(View.VISIBLE);
      tv_item_artist.setVisibility(View.VISIBLE);
      tv_item_duration.setVisibility(View.VISIBLE);
      tv_item_genre.setVisibility(View.VISIBLE);

      tv_item_artist.setText(currentSong.getArtist());
      if(currentSong.getArtist().isEmpty())tv_item_artist.setText("unknown artist");
      tv_item_duration.setText(currentSong.getDurationString());
      tv_item_genre.setText(currentSong.getGenre());
    }
    tv_item_artist.setTextColor(ColorSettingsFragment.getPrimaryColor(context));
    tv_item_duration.setTextColor(ColorSettingsFragment.getPrimaryColor(context));

    return rowView;
  }

  public Object getItem(int index){
    if(index >= 0 && index <= artistAlbumSongList.size()){
      return artistAlbumSongList.get(index);
    }
    return null;
  }

}
