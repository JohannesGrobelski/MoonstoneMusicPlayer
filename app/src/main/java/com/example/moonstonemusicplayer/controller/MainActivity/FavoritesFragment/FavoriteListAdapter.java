package com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class FavoriteListAdapter extends ArrayAdapter<Song> {
  private List<Song> songList;

  Context context;
  LayoutInflater layoutInflater;

  public FavoriteListAdapter(@NonNull Context context, List<Song> songList) {
    super(context, R.layout.song_row_layout,songList);
    this.songList = songList;
    this.context = context;
    this.layoutInflater = layoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Song aktuellerSong = songList.get(position);

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.item_row_layout, parent, false);
    }


    //init the views of songRowView
    TextView tv_playlistSongItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_playlistSongItem = rowView.findViewById(R.id.iv_item);
    tv_playlistSongItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_playlistSongItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_playlistSongItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    iv_playlistSongItem.setBackground(context.getDrawable(R.drawable.ic_music));
    tv_playlistSongItem.setText(aktuellerSong.getName());

    if(aktuellerSong != null){
      TextView tv_artist_song = rowView.findViewById(R.id.tv_item_artist);
      TextView tv_duration_song = rowView.findViewById(R.id.item_tv_duration);
      tv_artist_song.setVisibility(View.VISIBLE);
      tv_duration_song.setVisibility(View.VISIBLE);
      tv_artist_song.setText(aktuellerSong.getArtist());
      if(aktuellerSong.getArtist().isEmpty())tv_artist_song.setText("unknown artist");
      tv_duration_song.setText(aktuellerSong.getDurationString());
    }

    return rowView;
  }


}
