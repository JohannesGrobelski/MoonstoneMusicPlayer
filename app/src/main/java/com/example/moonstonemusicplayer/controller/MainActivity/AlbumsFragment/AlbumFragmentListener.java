package com.example.moonstonemusicplayer.controller.MainActivity.AlbumsFragment;

import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.AlbumFragment.Album;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AlbumFragment;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragmentListener implements AdapterView.OnItemClickListener, View.OnClickListener {
  private static final String TAG = AlbumFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  public static final String ALBUMLISTEXTRA = "albumlistextra";

  private final AlbumFragment albumFragment;
  private GenreListAdapter genreListAdapter;

  private static List<Song> AlbumSongList;

  public AlbumFragmentListener(AlbumFragment AlbumFragment) {
    this.albumFragment = AlbumFragment;
    List<Album> albumList = AlbumFragment.albumManager.getAlbumList();
    setAdapterAlbumList(albumList);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    //set back text
    Object clickItem = genreListAdapter.getItem(position);
    if(clickItem != null) {
      if(clickItem instanceof Song) {
        startAlbumSonglist(albumFragment.albumManager.getCurrentAlbum().getSongList(),position);
      } else if(clickItem instanceof Album){
        albumFragment.albumManager.setCurrentAlbum((Album) clickItem);
        setAdapterSongList(albumFragment.albumManager.getCurrentAlbum().getSongList());
      }

      else { Log.e(TAG,"favorite list contains something different than a songs or album");}
    }
  }

  public void setAdapterAlbumList(List<Album> itemList){
    List<Object> objectList = new ArrayList<>();
    objectList.addAll(itemList);
    genreListAdapter = new GenreListAdapter(albumFragment.getContext(),objectList);
    albumFragment.lv_albums.setAdapter(genreListAdapter);
  }


  public void setAdapterSongList(List<Song> itemList){
    List<Object> objectList = new ArrayList<>();
    objectList.addAll(itemList);
    genreListAdapter = new GenreListAdapter(albumFragment.getContext(),objectList);
    albumFragment.lv_albums.setAdapter(genreListAdapter);
  }

  /** starts playlistactivity with selected songlist; playlistactivity grabs songlist by calling getPlaylistSonglist*/
  public void startAlbumSonglist(List<Song> albumSongList, int song_index){
    AlbumSongList = new ArrayList<>(albumSongList);
    Intent intent = new Intent(albumFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(ALBUMLISTEXTRA,song_index);
    albumFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static Song[] getAlbumSonglist(){
    Song[] songlistCopy = AlbumSongList.toArray(new Song[AlbumSongList.size()]);
    AlbumSongList = null;
    return songlistCopy;
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    //only show context menu if clicked on song
    /*
    if(AlbumFragment.albumManager.getCurrentAlbum() != null){
      //create menu item with groupid to distinguish between fragments
      menu.add(2, 21, 0, "aus Favoriten löschen");
      menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override

        public boolean onMenuItemClick(MenuItem item) {
          AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
          int index = info.position;
          Song song = AlbumFragment.albumManager.getAlbum().get(index);
          DBPlaylists.getInstance(AlbumFragment.getContext()).deleteFromAlbum(song);

          AlbumFragment.albumManager = new AlbumManager(AlbumFragment.getContext());
          setAdapter(AlbumFragment.albumManager.getAlbum());

          return false;
        }
      });
    }
     */
  }

  public void reloadAlbumManager() {
    if(albumFragment.albumManager != null){
      albumFragment.albumManager.loadAlbumsFromDB(albumFragment.getContext());
      List<Album> albumList = albumFragment.albumManager.getAlbumList();
      setAdapterAlbumList(albumList);
    }
  }

  public boolean onBackPressed(){
    if(albumFragment.albumManager.getCurrentAlbum() != null){
      albumFragment.albumManager.setCurrentAlbum(null);
      List<Album> albumList = albumFragment.albumManager.getAlbumList();
      setAdapterAlbumList(albumList);
      return true;
    }
    return false;
  }

  @Override
  public void onClick(View v) {
    if(v.getId() == R.id.ll_back_album){
      onBackPressed();
    }
  }
}
