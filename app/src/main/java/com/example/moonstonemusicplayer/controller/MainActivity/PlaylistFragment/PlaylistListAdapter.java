package com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment;

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
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class PlaylistListAdapter extends ArrayAdapter<Object> {

  private List<Object> playlistSongList;
  Context context;
  LayoutInflater layoutInflater;

  public PlaylistListAdapter(@NonNull Context context, List<Object> playlistSongList) {
    super(context, R.layout.item_row_layout,playlistSongList);
    this.playlistSongList = playlistSongList;
    this.context = context;
    this.layoutInflater = layoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Song aktuellerSong = null; Playlist aktuellePlaylist = null;
    if(playlistSongList.get(position) instanceof Song){
      aktuellerSong = ((Song) playlistSongList.get(position));
    } else {
      aktuellePlaylist = ((Playlist) playlistSongList.get(position));
    }

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.item_row_layout, parent, false);
    }

    //init the views of songRowView
    TextView tv_playlistSongItem = rowView.findViewById(R.id.tv_item);
    ImageView iv_playlistSongItem = rowView.findViewById(R.id.iv_item);
    tv_playlistSongItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_playlistSongItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_playlistSongItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    if(aktuellePlaylist != null){
      iv_playlistSongItem.setBackground(context.getDrawable(R.drawable.ic_playlist));
      tv_playlistSongItem.setText(aktuellePlaylist.getName());
    } else {
      iv_playlistSongItem.setBackground(context.getDrawable(R.drawable.ic_music));
      tv_playlistSongItem.setText(aktuellerSong.getName());
    }
    return rowView;
  }

}
