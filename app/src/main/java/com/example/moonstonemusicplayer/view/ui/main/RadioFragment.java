package com.example.moonstonemusicplayer.view.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.RadioFragment.RadioFragmentListener;
import com.example.moonstonemusicplayer.model.MainActivity.RadioFragment.RadioManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RadioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RadioFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_SECTION_NUMBER = "section_number";
  private PageViewModel pageViewModel;

  public RadioManager radioManager;
  RadioFragmentListener radioFragmentListener;
  public ListView lv_radiolist;

  public RadioFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param
   * @return A new instance of fragment FavoritesFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static RadioFragment newInstance(int index) {
    RadioFragment fragment = new RadioFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_SECTION_NUMBER, index);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
    int index = 1;
    if (getArguments() != null) {
      index = getArguments().getInt(ARG_SECTION_NUMBER);
    }
    pageViewModel.setIndex(index);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView =  inflater.inflate(R.layout.fragment_radio, container, false);

    lv_radiolist = rootView.findViewById(R.id.lv_radio);
    radioFragmentListener = new RadioFragmentListener(this);
    lv_radiolist.setOnItemClickListener(radioFragmentListener);
    return rootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    radioManager = new RadioManager(context);
  }
}