package com.example.moonstonemusicplayer.view.mainactivity_fragments;

import android.content.Context;
import android.os.Bundle;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils.YoutubeAPIUtil;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.controller.MainActivity.YoutubeMusicFragment.YTVideoListAdapter;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.VideoModel;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnlineMusicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnlineMusicFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = GenreFragment.class.getSimpleName();
    private static final boolean DEBUG = false;

    // TODO: Rename and change types of parameters
    private EditText searchBar;
    private Button searchButton;
    private ListView listView;

    private YTVideoListAdapter adapter ;
    private List<VideoModel> mockDataList;

    private static OnlineMusicFragment instance;

    public OnlineMusicFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OnlineMusic.
     */
    // TODO: Rename and change types and number of parameters
    public static OnlineMusicFragment newInstance(int index) {
        OnlineMusicFragment fragment = new OnlineMusicFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        instance = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_online_music, container, false);

        this.searchBar = rootView.findViewById(R.id.searchBar);
        this.searchButton = rootView.findViewById(R.id.searchButton);

        this.listView = rootView.findViewById(R.id.listViewItems);
        this.mockDataList = new ArrayList<VideoModel>();
        this.adapter = new YTVideoListAdapter(instance.getActivity(), mockDataList);
        this.listView.setAdapter(adapter);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = searchBar.getText().toString();
                // Use the searchText for further processing, such as performing a search operation
                // or passing it to another method for handling.
                Toast.makeText(OnlineMusicFragment.this.getContext(), "Search Text: "+searchText, Toast.LENGTH_SHORT).show();

                try {
                    new YoutubeAPIUtil(instance.getContext()).searchVideosByKeyword(instance.getContext(), searchText, (result, error) -> {
                        if (error != null) {
                            // Handle the exception
                            Toast.makeText(instance.getContext(), "Error for "+searchText, Toast.LENGTH_SHORT).show();

                        } else {
                            // Use the video models
                            Toast.makeText(instance.getContext(), "Found "+result.size()+" Results for: "+searchText, Toast.LENGTH_SHORT).show();
                            adapter.removeAll();
                            for(VideoModel videoModel : result){
                                adapter.add(videoModel);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        registerForContextMenu(listView);

        // Inflate the layout for this fragment
        return rootView;
    }




}