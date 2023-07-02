package com.example.moonstonemusicplayer.controller.MainActivity.FolderFragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;

import java.io.File;
import java.util.List;

public class FolderListAdapter extends ArrayAdapter<File> {
  private static final String TAG = FolderListAdapter.class.getSimpleName();


  private final List<File> folderSongList;
  private final Context context;
  private final LayoutInflater layoutInflater;

  private MediaMetadataRetriever mmr = new MediaMetadataRetriever();
  String meta_durationStr;
  String meta_artist;
  String meta_genre;
  String meta_title;

  public FolderListAdapter(@NonNull Context context, List<File> folderSongList) {
    super(context, R.layout.item_row_layout,folderSongList);
    this.folderSongList = folderSongList;
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

    //init the views of songRowView
    TextView tv_folderSongItem = rowView.findViewById(R.id.tv_item_name);
    ImageView iv_folderSongItem = rowView.findViewById(R.id.iv_item);
    tv_folderSongItem.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    iv_folderSongItem.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    ImageViewCompat.setImageTintList(iv_folderSongItem, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary)));

    File file = folderSongList.get(position);
    if(folderSongList.get(position).isDirectory()){
      iv_folderSongItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_folder));
    } else {
      iv_folderSongItem.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_music));

      //init open song info button
      ImageButton btn_song_info = rowView.findViewById(R.id.btn_song_info);
      btn_song_info.setVisibility(View.VISIBLE);
      btn_song_info.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          // Handle button click
          showSongInfoPopup(context, file);
        }
      });
    }
    tv_folderSongItem.setText(file.getName());


    return rowView;
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
