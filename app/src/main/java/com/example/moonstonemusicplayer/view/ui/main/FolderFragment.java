package com.example.moonstonemusicplayer.view.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.Folder;
import com.example.moonstonemusicplayer.model.MainActivity.FolderFragment.FolderManager;

import java.util.logging.Logger;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FolderFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String ARG_SECTION_NUMBER = "section_number";

  FolderManager folderManager;

  Folder currentFolder;
  ListView lv_folderList;
  Button btn_folder_back;

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  public FolderFragment() {
    folderManager = new FolderManager(this.getContext());
    folderManager.loadLocalMusicAsFolder(this.getContext());
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param index Parameter 1.
   * @return A new instance of fragment FolderFragment.
   */
  public static FolderFragment newInstance(int index) {
    FolderFragment fragment = new FolderFragment();
    Bundle bundle = new Bundle();
    bundle.putInt(ARG_SECTION_NUMBER, index);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
    folderManager = new FolderManager(this.getContext());
  }

  public void loadMusicAsFolders(){
    folderManager.loadLocalMusicAsFolder(this.getContext());
    currentFolder = folderManager.getRootFolder();

    String[] children = currentFolder.getAllChildrenAsStrings();
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this.getContext(),android.R.layout.simple_list_item_1,children);
    lv_folderList.setAdapter(arrayAdapter);
    lv_folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(currentFolder.getChildren_folders().length > 0 &&
            position < currentFolder.getChildren_folders().length){
              currentFolder = currentFolder.getChildren_folders()[position];
              //setAdapter
              String[] children = currentFolder.getAllChildrenAsStrings();
              ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FolderFragment.this.getContext(),android.R.layout.simple_list_item_1,children);
              lv_folderList.setAdapter(arrayAdapter);
        }
      }
    });
    btn_folder_back.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if(currentFolder.getParent() != null){
              currentFolder = currentFolder.getParent();
              //setAdapter
              String[] children = currentFolder.getAllChildrenAsStrings();
              ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FolderFragment.this.getContext(),android.R.layout.simple_list_item_1,children);
              lv_folderList.setAdapter(arrayAdapter);
            }
         }
       }
    );
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
    btn_folder_back = view.findViewById(R.id.btn_folder_back);
    /*
    fragmentListener = new FirstFragmentListener(this);
    btnFirst.setOnClickListener(fragmentListener);
     */

    //rückgabe des Fragmentviews
    return view;
  }
}