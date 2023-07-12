package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.swipe.SwipeLayout;
import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment.FolderFragmentListener;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.MainActivity.BrowserManager;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

import java.io.File;
import java.util.List;

public class SongListAdapter extends ArrayAdapter<File> {
  private static final String TAG = SongListAdapter.class.getSimpleName();

  private final List<File> songList;
  private String selectedSongPath = "";

  private final Context context;
  private final LayoutInflater layoutInflater;

  public SongListAdapter(@NonNull Context context, List<File> songList) {
    super(context, R.layout.song_row_layout,songList);
    this.songList = songList;
    this.context = context;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    File currentSongFile = songList.get(position);

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.song_row_layout, parent, false);
    }

    //init the views of songRowView
    LinearLayout ll_song_background = rowView.findViewById(R.id.ll_song_background);
    TextView tv_title = rowView.findViewById(R.id.tv_name_song);

    TextView tv_artist_song = rowView.findViewById(R.id.tv_artist_song);
    TextView tv_genre_song = rowView.findViewById(R.id.tv_genre_song);
    TextView tv_duration_song = rowView.findViewById(R.id.tv_duration_song);

    //set the views of songRowView
    Song song = BrowserManager.getSongFromAudioFile(currentSongFile);
    tv_title.setText(song.getName());
    if(song.getArtist() != null && !song.getArtist().isEmpty()){
      tv_artist_song.setVisibility(View.VISIBLE);
      tv_artist_song.setText(song.getArtist());
    }
    if(song.getGenre() != null && !song.getGenre().isEmpty()){
      tv_genre_song.setVisibility(View.VISIBLE);
      tv_genre_song.setText(song.getGenre());
    }
    if(song.getDurationString() != null && !song.getDurationString().isEmpty()){
      tv_duration_song.setVisibility(View.VISIBLE);
      tv_duration_song.setText(song.getDurationString());
    }

    int nightModeFlags =
            getContext().getResources().getConfiguration().uiMode &
                    Configuration.UI_MODE_NIGHT_MASK;

    if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
      if(currentSongFile.getPath().equals(selectedSongPath))ll_song_background.setBackgroundColor(Color.LTGRAY);
      else ll_song_background.setBackgroundColor(Color.DKGRAY);
    } else {
      if(currentSongFile.getPath().equals(selectedSongPath))ll_song_background.setBackgroundColor(Color.LTGRAY);
      else ll_song_background.setBackgroundColor(Color.WHITE);
    }

    SwipeLayout item_row_swipe_layout = rowView.findViewById(R.id.song_row_swipe_layout);
    item_row_swipe_layout.setSwipeEnabled(true);
    item_row_swipe_layout.setShowMode(SwipeLayout.ShowMode.LayDown);

    //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
    item_row_swipe_layout.addDrag(SwipeLayout.DragEdge.Left, rowView.findViewById(R.id.add_song_to_favorites));
    item_row_swipe_layout.addDrag(SwipeLayout.DragEdge.Right, rowView.findViewById(R.id.add_song_to_playlist));

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

    ImageButton add_to_favorites = rowView.findViewById(R.id.add_song_to_favorites);
    add_to_favorites.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(context, "Added to favorites: "+song.getName(), Toast.LENGTH_SHORT).show();
        DBPlaylists.getInstance(context).addToFavorites(context,song);
        item_row_swipe_layout.close(true);
      }
    });

    ImageButton add_to_playlist = rowView.findViewById(R.id.add_song_to_playlist);
    add_to_playlist.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FolderFragmentListener.showAlertDialogAddToPlaylists(layoutInflater, context, song);
        item_row_swipe_layout.close(true);
      }
    });

    return rowView;
  }

  public String getSelectedSongPath() {
    return selectedSongPath;
  }

  public void setSelectedSongPath(String selectedSongIndex) {
    this.selectedSongPath = selectedSongIndex;
  }

  public static String removeFileType(String fileName) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    }
    return fileName;
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
