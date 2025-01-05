/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.moonstonemusicplayer.R;


/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class MainactivitySectionsPagerAdapter extends FragmentPagerAdapter {

  @StringRes
  private static final int[] TAB_TITLES = new int[]{R.string.tab_folders,R.string.tab_audiobooks,R.string.tab_playlists,R.string.tab_albums,R.string.tab_artists,R.string.tab_genre};
  private final Context mContext;

  Fragment[] fragments = new Fragment[6];

  public MainactivitySectionsPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
    mContext = context;
  }

  @Override
  public Fragment getItem(int position) {
    // getItem is called to instantiate the fragment for the given page.
    // Return a PlaceholderFragment (defined as a static inner class below).
    if(position == 0){
      fragments[0] = FolderFragment.newInstance();
      return fragments[0];
    } else if(position == 1){
      fragments[1] = AudiobookFragment.newInstance();
      return fragments[1];
    } else if(position == 2){
      fragments[2] = PlayListFragment.newInstance();
      return fragments[2];
    } else if(position == 3){
      fragments[3] = AlbumFragment.newInstance();
      return fragments[3];
    } else if(position == 4){
      fragments[4] = ArtistFragment.newInstance();
      return fragments[4];
    } else if(position == 5){
      fragments[5] = GenreFragment.newInstance();
      return fragments[5];
    }
    return null;
  }

  public Fragment[] getFragments(){
    return fragments;
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return mContext.getResources().getString(TAB_TITLES[position]);
  }

  @Override
  public int getCount() {
    return fragments.length;
  }


}
