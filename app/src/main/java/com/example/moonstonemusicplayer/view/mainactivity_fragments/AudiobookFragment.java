package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.AudiobookFragment.AudiobookFragmentListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudiobookFragment extends Fragment {
  public static final String FOLDERAUDIOBOOKINDEXEXTRA = "FOLDERAUDIOBOOKINDEXEXTRA";

  private static final String TAG = AudiobookFragment.class.getSimpleName();
  private static final boolean DEBUG = false;
  private AudiobookFragmentListener audiobookFragmentListener;


  //Views
  public ListView lv_folderList;
  private LinearLayout ll_folder_back;
  private TextView tv_folder_back;

  public AudiobookFragment() {/*empty constructor, no context*/}

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param index Parameter 1.
   * @return A new instance of fragment AudiobookFragment.
   */
  public static AudiobookFragment newInstance(int index) {
    AudiobookFragment fragment = new AudiobookFragment();
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, //zum Layout verbinden
                           @Nullable ViewGroup container,    //wo das Layout angezeigt wird
                           @Nullable Bundle savedInstanceState) { //falls es gespeicherte Werte gibt


    //Das Layout verbinden für das Fragment
    View view = inflater.inflate(R.layout.fragment_folder, container, false);
    //Referenz des listviews
    lv_folderList = view.findViewById(R.id.lv_folderlist);
    ll_folder_back = view.findViewById(R.id.ll_back_folder);
    tv_folder_back = view.findViewById(R.id.tv_folderBack);

    audiobookFragmentListener = new AudiobookFragmentListener(this);
    initViews();
    registerForContextMenu(lv_folderList);

    //rückgabe des Fragmentviews
    return view;
  }

  private void initViews(){
    lv_folderList.setOnItemClickListener(audiobookFragmentListener);
    ll_folder_back.setOnClickListener(audiobookFragmentListener);
  }

  public void searchMusic(String query) {
    audiobookFragmentListener.searchMusic(query);
  }



  public boolean onBackpressed() {
    return audiobookFragmentListener.onBackPressed();
  }

  public void reverse() {
    audiobookFragmentListener.reverse();
  }
}