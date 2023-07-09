package com.example.moonstonemusicplayer.controller.MainActivity.YoutubeMusicFragment;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.VideoModel;
import com.example.moonstonemusicplayer.model.MainActivity.OnlineMusicFragment.utils.YouTubeDownloader;
import com.example.moonstonemusicplayer.view.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public final class YTVideoListAdapter extends ArrayAdapter {

    private final static String TAG = YTVideoListAdapter.class.getSimpleName();
    private final List<VideoModel> videoModelList;
    private final Activity activity;
    private final LayoutInflater layoutInflater;

    public YTVideoListAdapter(@NonNull Activity activity, List<VideoModel> videoModelList) {
        super(activity, R.layout.item_row_layout, videoModelList);
        this.videoModelList = videoModelList;
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    public void removeAll(){
        videoModelList.clear();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView;
        if(convertView != null){
            rowView = convertView;
        } else {
            rowView = layoutInflater.inflate(R.layout.yt_video_list_item, parent, false);
        }

        VideoModel currentVideoModel = ((VideoModel) videoModelList.get(position));

        //init the views of yt_video_list_item
        ImageView imageViewThumbnail = rowView.findViewById(R.id.imageViewThumbnail);
        TextView textViewItemTitle = rowView.findViewById(R.id.textViewItemTitle);
        TextView textViewItemChannel = rowView.findViewById(R.id.textViewItemChannel);
        TextView textViewItemDuration = rowView.findViewById(R.id.textViewItemDuration);
        TextView textViewItemUploadDate = rowView.findViewById(R.id.textViewItemUploadDate);
        Button downloadSongButton = rowView.findViewById(R.id.downloadSongButton);

        textViewItemTitle.setText(currentVideoModel.getTitle());
        textViewItemChannel.setText(currentVideoModel.getChannel());
        textViewItemDuration.setText(currentVideoModel.getDuration());
        textViewItemUploadDate.setText(currentVideoModel.getUploadDate());
        if(currentVideoModel.getThumbnail() != null){
            loadThumbnail(currentVideoModel.getThumbnail(),imageViewThumbnail);
        }

        downloadSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ((MainActivity) activity).downloadSong(currentVideoModel);
            }
        });

        return rowView;
    }

    public Object getItem(int index){
        if(index >= 0 && index <= videoModelList.size()){
            return videoModelList.get(index);
        }
        return null;
    }

    public static void loadThumbnail(String thumbnailUrl, ImageView imageView) {
        try {
            Picasso.get()
                    .load(thumbnailUrl)
                    .into(imageView);
        } catch (Exception e){
            Log.e(TAG,"Could not load Thumbnail!");
        }

    }


}
