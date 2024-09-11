package com.example.moonstonemusicplayer.controller.MainActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

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
      case R.id.miSortNameMain: {
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
      case 1: {
        Log.v(TAG, "search the current fragment FolderFragment");
        ((FolderFragment) fragments[1]).searchMusic(query);
        break;
      }
      case 2: {
        Log.v(TAG, "search the current fragment PlayListFragment");
        ((PlayListFragment) fragments[2]).searchMusic(query);
        break;
      }
     /* case 2: {
        Log.v(TAG, "search the current fragment FavoritesFragment");
        ((FavoritesFragment) fragments[2]).searchMusic(query);
        break;
      }*/
      case 3: {
        Log.v(TAG, "search the current fragment AlbumFragment");
        ((AlbumFragment) fragments[3]).searchMusic(query);
        break;
      }
      case 4: {
        Log.v(TAG, "search the current fragment ArtistFragment");
        ((ArtistFragment) fragments[4]).searchMusic(query);
        break;
      }
      case 5: {
        Log.v(TAG, "search the current fragment GenreFragment");
        ((GenreFragment) fragments[5]).searchMusic(query);
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
    if (isSearchBarOpen()) {
      closeSearchBar();
      return true;
    } else {
      switch(currentItem){
        case 1: {
          return ((FolderFragment) fragments[1]).onBackpressed();
        }
        case 2: {
          return ((PlayListFragment) fragments[2]).onBackpressed();
        }
        case 3: {
          return false;
        }
        case 4: {
          return ((ArtistFragment) fragments[4]).onBackpressed();
        }
        case 5: {
          return ((GenreFragment) fragments[5]).onBackpressed();
        }
      }
    }
    return false;
  }

  public Fragment getCurrentFragment(int position){
    return fragments[position];
  }

  private void closeSearchBar() {
    if(mainActivity.searchView != null) {
      mainActivity.searchView.setIconified(true);
      mainActivity.searchView.clearFocus();
    }
  }

  private boolean isSearchBarOpen() {
    return !mainActivity.searchView.isIconified();
  }
}
