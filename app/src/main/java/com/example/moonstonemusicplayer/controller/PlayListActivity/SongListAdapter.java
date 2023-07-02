package com.example.moonstonemusicplayer.controller.PlayListActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
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
import com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment.FolderListAdapter;
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
    File currentSong = songList.get(position);

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.song_row_layout, parent, false);
    }

    //init the views of songRowView
    LinearLayout ll_song_background = rowView.findViewById(R.id.ll_song_background);
    TextView tv_title = rowView.findViewById(R.id.tv_name_song);

    //set the views of songRowView
    tv_title.setText(currentSong.getName());

    if(currentSong.getPath().equals(selectedSongPath))ll_song_background.setBackgroundColor(Color.LTGRAY);
    else ll_song_background.setBackgroundColor(Color.WHITE);

    File file = songList.get(position);
    //init open song info button
    ImageView iv_song_playing = rowView.findViewById(R.id.iv_song_playing);
    iv_song_playing.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Handle button click
        showSongInfoPopup(context, file);
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

    String title = file.getName().substring(0, (file.getName().length() - 4));
    String path = file.getAbsolutePath();//Uri.fromFile(file).toString();
    String genre = "";
    String artist = "";
    String album = "";
    int duration = 0;

    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    try {
      mmr.setDataSource(Uri.fromFile(file).getPath());
    } catch (Exception e){
      Log.e(TAG, e.toString());
    }

    String meta_durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    String meta_artist =  mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
    String meta_genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
    String meta_title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
    String meta_album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

    if(meta_title != null && !meta_title.isEmpty() && !meta_title.equals("null")){
      title = meta_title;
    }
    if(meta_album != null && !meta_album.isEmpty() && !meta_album.equals("null")){
      album = meta_album;
    }
    if(meta_genre != null && !meta_genre.isEmpty() && !meta_genre.equals("null")){
      genre = translateGenre(meta_genre);
    }
    if(meta_artist != null && !meta_artist.isEmpty() && !meta_artist.equals("null")){
      artist = meta_artist;
    } else {artist = "unbekannter Künstler";}
    if(meta_durationStr != null && !meta_durationStr.isEmpty() && !meta_durationStr.equals("null") && meta_durationStr.matches("[0-9]*")){
      duration = Integer.parseInt(meta_durationStr);
    }


    titleTextView.setText(title);
    artistTextView.setText("Artist: " + artist);
    albumTextView.setText("Album: " + album);
    genreTextView.setText("Genre: " + genre);
    durationTextView.setText("Duration: " + duration + " ms");

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
