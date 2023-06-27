package com.example.moonstonemusicplayer.controller.MainActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.AlbumFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.ArtistFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.FolderFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.GenreFragment;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;

/** Init the views from mainactivity and implement actions on them (mostly based on which fragment is active):
 *  * options menu:
 *    * define actions for menu items clicked (onOptionsItemSelected)
 *  * implement a request for runtime storage permissions (requestForPermission)
 *  * search view:
 *    * define what happens on input for search view (implementation onQueryTextChange)
 *  * define what happens if back button is pressed
 */
public class MainActivityListener implements SearchView.OnQueryTextListener {
  private static final boolean DEBUG = true;
  private static final String TAG = MainActivityListener.class.getSimpleName();

  private final MainActivity mainActivity;
  private final Fragment[] fragments;


  /** Init fields, ask for permissions (@this. requests runtime storage permissions)
   *
   * @param mainActivity
   * @param fragments
   */
  public MainActivityListener(MainActivity mainActivity,Fragment[] fragments) {
    this.mainActivity = mainActivity;
    this.fragments = fragments;
    if(DEBUG)Log.d(TAG,"fragments null: "+ (fragments == null));

    requestForPermission();
  }

  /** Init options menu and search view.
   *
   * @param menu
   * @return
   */
  public boolean onCreateOptionsMenu(Menu menu) {
    //create options menu
    mainActivity.getMenuInflater().inflate(R.menu.options_menu_mainactivity,menu);

    //create searchview
    MenuItem searchItem = menu.findItem(R.id.miSearch);
    mainActivity.searchView = (SearchView) searchItem.getActionView();
    mainActivity.searchView.setOnQueryTextListener(this);
    return true;
  }

  /** Define actions that are taken dependend on selected menu item in menu (these include all options across different fragments).
   *  In all cases there is an action method called in the corresponding fragment.
   *  Breakdown of the actions taken for a selected option:
   *  - load locale audio file: create a dialog and a positive response action (Folderfragment reload Allmusic)
   *  - sort name, artist, duration, genre, reverse: call sort method of fragment
   * @param item
   * @return if menu item exists
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mi_loadLocaleAudioFile: {
        if (fragments != null && fragments[1] != null) {
          requestForPermission();
          //show alert dialog before refreshing audio files
          AlertDialog alertDialog = new AlertDialog.Builder(mainActivity)
              .setTitle("Reloads local audio files.")
              .setMessage("This can take a few minutes.")
              .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  ((FolderFragment) fragments[0]).reloadAllMusic();
                }
              })
              .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  //abort
                }
              })
          .create();
          alertDialog.show();
          break;
        } else {
          Log.e(TAG, "fragment null");
        }
        //folderManager.getRootFolder().print(0);
        break;
      } case R.id.miSortNameMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch(currentItem){
          case 0: {((FolderFragment) fragments[0]).sortSongsByName(); break;}
          case 1: {((PlayListFragment) fragments[1]).sortSongsByName();break;}
          //case 2: {((FavoritesFragment) fragments[2]).sortSongsByName();break;}
          case 2: {((AlbumFragment) fragments[2]).sortSongsByName();break;}
          case 3: {((ArtistFragment) fragments[3]).sortSongsByName();break;}
          case 4: {((GenreFragment) fragments[4]).sortSongsByName();break;}
        }
        break;
      } case R.id.miSortArtistMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch(currentItem){
          case 0: {((FolderFragment) fragments[0]).sortSongsByArtist(); break;}
          case 1: {((PlayListFragment) fragments[1]).sortSongsByArtist();break;}
          //case 2: {((FavoritesFragment) fragments[2]).sortSongsByArtist();break;}
          case 2: {((AlbumFragment) fragments[2]).sortSongsByArtist();break;}
          case 3: {((ArtistFragment) fragments[3]).sortSongsByArtist();break;}
          case 4: {((GenreFragment) fragments[4]).sortSongsByArtist();break;}
        }
        break;
      } case R.id.miSortDurationMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch(currentItem){
          case 0: {((FolderFragment) fragments[0]).sortSongsByDuration(); break;}
          case 1: {((PlayListFragment) fragments[1]).sortSongsByDuration(); break;}
          //case 2: {((FavoritesFragment) fragments[2]).sortSongsByDuration();break;}
          case 2: {((AlbumFragment) fragments[2]).sortSongsByDuration();break;}
          case 3: {((ArtistFragment) fragments[3]).sortSongsByDuration();break;}
          case 4: {((GenreFragment) fragments[4]).sortSongsByDuration();break;}
        }
        break;
      } case R.id.miSortGenreMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch(currentItem){
          case 0: {((FolderFragment) fragments[0]).sortSongsByGenre(); break;}
          case 1: {((PlayListFragment) fragments[1]).sortSongsByGenre(); break;}
          //case 2: {((FavoritesFragment) fragments[2]).sortSongsByGenre();break;}
          case 2: {((AlbumFragment) fragments[2]).sortSongsByGenre();break;}
          case 3: {((ArtistFragment) fragments[3]).sortSongsByGenre();break;}
          case 4: {((GenreFragment) fragments[4]).sortSongsByGenre();break;}
        }
        break;
      } case R.id.miReverseMain: {
        int currentItem = mainActivity.viewPager.getCurrentItem();
        switch(currentItem){
          case 0: {((FolderFragment) fragments[0]).reverse(); break;}
          case 1: {((PlayListFragment) fragments[1]).reverse(); break;}
          //case 2: {((FavoritesFragment) fragments[2]).reverse();break;}
          case 2: {((AlbumFragment) fragments[2]).reverse();break;}
          case 3: {((ArtistFragment) fragments[3]).reverse();break;}
          case 4: {((GenreFragment) fragments[4]).reverse();break;}
        }
        break;
      }
      }
      return true;
  }

  /** requests runtime storage permissions (API>=23) for loading files from sd-card */
  public boolean requestForPermission() {
    int permissionCheck = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
      } else {
        mainActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
      }
    } else {
      //Toast.makeText(mainActivity, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
    }
    return permissionCheck == PackageManager.PERMISSION_GRANTED;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  /**
   *
   * @param query
   * @return
   */
  @Override
  public boolean onQueryTextChange(String query) {
    //search in different fragments

    //Fragment currentFragment = mainActivity.getSupportFragmentManager().findFragmentById(R.id.view_pager_main);
    int currentItem = mainActivity.viewPager.getCurrentItem();
    Log.d("search","in fragment: "+currentItem);
    switch(currentItem){
      case 0: {
        Log.v(TAG, "search the current fragment FolderFragment");
        ((FolderFragment) fragments[0]).searchMusic(query);
        break;
      }
      case 1: {
        Log.v(TAG, "search the current fragment PlaylistFragment");
        ((PlayListFragment) fragments[1]).searchMusic(query);
        break;
      }
     /* case 2: {
        Log.v(TAG, "search the current fragment FavoritesFragment");
        ((FavoritesFragment) fragments[2]).searchMusic(query);
        break;
      }*/
      case 2: {
        Log.v(TAG, "search the current fragment FavoritesFragment");
        ((AlbumFragment) fragments[2]).searchMusic(query);
        break;
      }
      case 3: {
        Log.v(TAG, "search the current fragment FavoritesFragment");
        ((ArtistFragment) fragments[3]).searchMusic(query);
        break;
      }
      case 4: {
        Log.v(TAG, "search the current fragment FavoritesFragment");
        ((GenreFragment) fragments[4]).searchMusic(query);
        break;
      }
    }
    return false;
  }



  /**
   * @return false if nothing was done => normal onBackPressed behavior
   */
  public boolean onBackPressed() {
    int currentItem = mainActivity.viewPager.getCurrentItem();
    switch(currentItem){
      case 0: {
        return ((FolderFragment) fragments[0]).onBackpressed();
      }
      case 1: {
        return ((PlayListFragment) fragments[1]).onBackpressed();
      }
      case 2: {
        return false;
      }
      case 3: {
        return ((AlbumFragment) fragments[3]).onBackpressed();
      }
    }
    return false;
  }
}
