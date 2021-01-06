package com.example.moonstonemusicplayer.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Song;

import java.util.List;

public class SongListAdapter extends ArrayAdapter<Song> {
  List<Song> songList;
  Context context;
  LayoutInflater layoutInflater;

  public SongListAdapter(@NonNull Context context, List<Song> songList) {
    super(context, R.layout.song_row_layout,songList);
    this.songList = songList;
    this.context = context;
    this.layoutInflater = layoutInflater.from(context);

  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.song_row_layout, parent, false);
    }
    Song aktuellerSong = songList.get(position);

    TextView tv_title = rowView.findViewById(R.id.tv_title);
    TextView tv_artist = rowView.findViewById(R.id.tv_artist);
    TextView tv_duration = rowView.findViewById(R.id.tv_duration);

    tv_title.setText(aktuellerSong.getTitle());
    tv_artist.setText(aktuellerSong.getArtist());
    tv_duration.setText(Song.getDurationString((int) aktuellerSong.getDuration_ms()));

    return rowView;
  }
}
