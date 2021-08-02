package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.util.List;

public class SongListAdapter extends ArrayAdapter<Song> {
  private final List<Song> songList;
  private String selectedSongPath = "";

  private final Context context;
  private final LayoutInflater layoutInflater;

  public SongListAdapter(@NonNull Context context, List<Song> songList) {
    super(context, R.layout.song_row_layout,songList);
    this.songList = songList;
    this.context = context;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Song currentSong = songList.get(position);

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.song_row_layout, parent, false);
    }

    //init the views of songRowView
    LinearLayout ll_song_background = rowView.findViewById(R.id.ll_song_background);
    TextView tv_title = rowView.findViewById(R.id.tv_name_song);
    TextView tv_artist = rowView.findViewById(R.id.tv_artist_song);
    TextView tv_duration = rowView.findViewById(R.id.tv_duration_song);
    TextView tv_genre = rowView.findViewById(R.id.tv_genre_song);

    //set the views of songRowView
    tv_title.setText(currentSong.getName());
    if(!currentSong.getArtist().isEmpty())tv_artist.setText(currentSong.getArtist());
    else tv_artist.setText("unknown artist");
    tv_genre.setText(currentSong.getGenre());


    if(currentSong.getPath().equals(selectedSongPath))ll_song_background.setBackgroundColor(Color.LTGRAY);
    else ll_song_background.setBackgroundColor(Color.WHITE);

    tv_duration.setText(Song.getDurationString((int) currentSong.getDuration_ms()));
    return rowView;
  }

  public String getSelectedSongPath() {
    return selectedSongPath;
  }

  public void setSelectedSongPath(String selectedSongIndex) {
    this.selectedSongPath = selectedSongIndex;
  }
}
