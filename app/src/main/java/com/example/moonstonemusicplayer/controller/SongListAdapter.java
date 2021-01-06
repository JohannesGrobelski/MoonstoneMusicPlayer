package com.example.moonstonemusicplayer.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.model.Song;


public class SongListAdapter extends ArrayAdapter<Song> {
  public SongListAdapter(@NonNull Context context, int resource) {
    super(context, resource);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


    return super.getView(position, convertView, parent);
  }
}
