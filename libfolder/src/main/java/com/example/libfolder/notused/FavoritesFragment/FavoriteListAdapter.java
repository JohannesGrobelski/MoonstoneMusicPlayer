package com.example.libfolder.notused.FavoritesFragment;
//
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.core.widget.ImageViewCompat;
//
//import com.example.moonstonemusicplayer.R;
//import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
//
//import java.util.List;
//
//public class FavoriteListAdapter extends ArrayAdapter<Song> {
//  private List<Song> songList;
//
//  private Context context;
//  private LayoutInflater layoutInflater;
//
//  public FavoriteListAdapter(@NonNull Context context, List<Song> songList) {
//    super(context, R.layout.song_row_layout,songList);
//    this.songList = songList;
//    this.context = context;
//    this.layoutInflater = layoutInflater.from(context);
//  }
//
//  @NonNull
//  @Override
//  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//    Song currentSong = songList.get(position);
//
//    View rowView;
//    if(convertView != null){
//      rowView = convertView;
//    } else {
//      rowView = layoutInflater.inflate(R.layout.item_row_layout, parent, false);
//    }
//
//
//    //init the views of songRowView
//    TextView tv_playlistSongItem = rowView.findViewById(R.id.tv_item_name);
//    ImageView iv_playlistSongItem = rowView.findViewById(R.id.iv_item);
//    tv_playlistSongItem.setTextColor(SettingsFragment.getPrimaryColor());
//    iv_playlistSongItem.setColorFilter(SettingsFragment.getPrimaryColor(context), android.graphics.PorterDuff.Mode.SRC_IN);
//    ImageViewCompat.setImageTintList(iv_playlistSongItem, ColorStateList.valueOf(SettingsFragment.getPrimaryColor(context)));
//
//    iv_playlistSongItem.setBackground(context.getDrawable(R.drawable.ic_music));
//    tv_playlistSongItem.setText(currentSong.getName());
//
//    if(currentSong != null){
//      LinearLayout ll_artist_genre = rowView.findViewById(R.id.ll_artist_genre);
//      TextView tv_artist_song = rowView.findViewById(R.id.tv_item_artist);
//      TextView tv_duration_song = rowView.findViewById(R.id.item_tv_duration);
//      TextView tv_duration_genre = rowView.findViewById(R.id.tv_item_genre);
//
//      ll_artist_genre.setVisibility(View.VISIBLE);
//      tv_artist_song.setVisibility(View.VISIBLE);
//      tv_duration_song.setVisibility(View.VISIBLE);
//      tv_duration_genre.setVisibility(View.VISIBLE);
//
//      tv_artist_song.setText(currentSong.getArtist());
//      if(currentSong.getArtist().isEmpty())tv_artist_song.setText("unknown artist");
//      tv_duration_genre.setText(currentSong.getGenre());
//      tv_duration_song.setText(currentSong.getDurationString());
//    }
//
//    return rowView;
//  }
//
//
//}
//