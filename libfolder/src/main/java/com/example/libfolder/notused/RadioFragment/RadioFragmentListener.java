package com.example.libfolder.notused.RadioFragment;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment.PlaylistListAdapter;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.PlayListActivity;
import com.example.moonstonemusicplayer.view.ui.main.PlayListFragment;
import com.example.moonstonemusicplayer.view.ui.main.RadioFragment;

import java.util.ArrayList;
import java.util.List;

public class RadioFragmentListener implements AdapterView.OnItemClickListener {
  private static final String TAG = RadioFragmentListener.class.getSimpleName();
  private static final boolean DEBUG = false;
  public static final String RADIOLISTINDEXEXTRA = "radiolistextra";

  private final RadioFragment radioFragment;
  private RadioListAdapter radioListAdapter;

  private static List<Radio> Radiolist;

  public RadioFragmentListener(RadioFragment radioFragment) {
    this.radioFragment = radioFragment;
    List<Radio> radios = radioFragment.radioManager.getAllRadios();
    setAdapter(radios);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if(DEBUG)Log.d(TAG,"\n\n\n\n\n");

    //set back text
    Radio radio = radioListAdapter.getItem(position);
    if(radio != null) {
      if(radio instanceof Radio) {
        startRadiolist(radioFragment.radioManager.getAllRadios(),position);
      } else { Log.e(TAG,"radiolist contains non radio");}
    }
  }

  private void setAdapter(List<Radio> radiolist){
    radioListAdapter = new RadioListAdapter(radioFragment.getContext(),radiolist);
    radioFragment.lv_radiolist.setAdapter(radioListAdapter);
  }


  /** starts playlistactivity with selected radiolist; playlistactivity grabs radiolist by calling getRadiolist*/
  public void startRadiolist(List<Radio> radiolist, int radioPosition){
    Radiolist = new ArrayList<>(radiolist);
    Intent intent = new Intent(radioFragment.getActivity(), PlayListActivity.class);
    intent.putExtra(RADIOLISTINDEXEXTRA,radioPosition);
    radioFragment.startActivity(intent);
  }

  /** used by playlistactivity to get songs to play*/
  public static Radio[] getRadiolist(){
    Radio[] radiolistCopy = Radiolist.toArray(new Radio[Radiolist.size()]);
    Radiolist = null;
    return radiolistCopy;
  }
}
