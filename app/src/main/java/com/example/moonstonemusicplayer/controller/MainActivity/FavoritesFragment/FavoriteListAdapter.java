package com.example.moonstonemusicplayer.controller.MainActivity.FavoritesFragment;

import android.content.Context;
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
    View rowView = convertView;
    if(convertView == null){
      rowView = layoutInflater.inflate(R.layout.mainact_song_row_layout, parent, false);
    }
    aktuellerSong = ((Song) songList.get(position));

    //init the views of songRowView
    TextView tv_name_song_main = rowView.findViewById(R.id.tv_name_song_main);
    TextView tv_artist_song_main = rowView.findViewById(R.id.tv_artist_song_main);
    TextView tv_duration_song_main = rowView.findViewById(R.id.main_tv_duration_song);

    tv_name_song_main.setText(aktuellerSong.getName());
    tv_artist_song_main.setText(aktuellerSong.getArtist());
    if(aktuellerSong.getArtist().isEmpty())tv_artist_song_main.setText("unknown artist");
    tv_duration_song_main.setText(aktuellerSong.getDurationString());

    return rowView;
  }


}
