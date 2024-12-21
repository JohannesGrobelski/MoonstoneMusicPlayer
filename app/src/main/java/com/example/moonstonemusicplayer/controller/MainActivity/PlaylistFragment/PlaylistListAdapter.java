/*
 * Copyright (c) 2025 Johannes Grobelski 
 * All rights reserved.
 * 
 * This file is part of MoonStone Music Player and is protected under
 * the proprietary license found in the LICENSE file in the root directory.
 */

package com.example.moonstonemusicplayer.controller.MainActivity.PlaylistFragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.PlayListFragment.Playlist;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;
import com.example.moonstonemusicplayer.view.mainactivity_fragments.PlayListFragment;
import com.woxthebox.draglistview.DragItemAdapter;

import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.MOSTLY_PLAYED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_ADDED_PLAYLIST_NAME;
import static com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists.RECENTLY_PLAYED_PLAYLIST_NAME;

import java.util.ArrayList;
import java.util.List;

public class PlaylistListAdapter extends DragItemAdapter<Object, PlaylistListAdapter.ViewHolder> {

    private int lastLongClickedPosition = -1; // Default to an invalid position

    private final List<Object> playlistSongList;
    private ItemClickListener itemClickListener;
    private PlayListFragment playListFragment;

    public interface ItemClickListener {
        void onItemClicked(Object item, int position);
    }
    public PlaylistListAdapter(PlayListFragment playListFragment, List<Object> playlistSongList) {
        super(); // true enables drag functionality
        this.playlistSongList = playlistSongList;
        this.playListFragment = playListFragment;
        setItemList(playlistSongList);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.itemView.setTag(position); // Save position in the tag
        Object item = playlistSongList.get(position);

        // Reset visibility and styling
        /*
        holder.ll_artist_genre.setVisibility(View.GONE);
        holder.tv_artist_song.setVisibility(View.GONE);
        holder.tv_duration_song.setVisibility(View.GONE);
        holder.tv_duration_genre.setVisibility(View.GONE);
         */

        holder.tv_playlistSongItem.setTextColor(playListFragment.getContext().getResources().getColor(R.color.colorPrimary));
        holder.iv_playlistSongItem.setColorFilter(ContextCompat.getColor(playListFragment.getContext(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN);
        ImageViewCompat.setImageTintList(holder.iv_playlistSongItem,
                ColorStateList.valueOf(ContextCompat.getColor(playListFragment.getContext(), R.color.colorPrimary)));

        if (item instanceof Playlist) {
            bindPlaylistItem(holder, (Playlist) item);
        } else if (item instanceof Song) {
            bindSongItem(holder, (Song) item);
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> handleItemClick(item, position));

        holder.itemView.setOnLongClickListener(v -> {
            // Save the clicked position in a global variable
            lastLongClickedPosition = position;
            v.showContextMenu();
            return true;
        });
    }


    public int getLastLongClickedPosition(){
        return lastLongClickedPosition;
    }

    private void handleItemClick(Object clickItem, int position) {
        if (clickItem != null) {
            playListFragment.srl_playlist.setEnabled(false);

            if (clickItem instanceof Playlist) {
                playListFragment.dlv_playlistSongList.setDragEnabled(true);
                Playlist playlist = (Playlist) clickItem;
                playListFragment.playlistFragmentListener.setCurrentPlaylist(playlist.getName());
                List<Object> itemList = new ArrayList<>();
                itemList.addAll(playlist.getPlaylist());
                setItemList(itemList); // Update the adapter's list
                notifyDataSetChanged();

                playListFragment.getPlaylistManager().setCurrentPlaylist(playlist);
                playListFragment.srl_playlist.setEnabled(
                        playlist.getName().equals(RECENTLY_ADDED_PLAYLIST_NAME)
                );
            } else if (clickItem instanceof Song) {
                playListFragment.playlistFragmentListener.startPlaylist(
                        playListFragment.getPlaylistManager().getCurrentPlaylist(),
                        position
                );
            }

            // Notify through interface if set
            if (itemClickListener != null) {
                itemClickListener.onItemClicked(clickItem, position);
            }
        }
    }

    private void bindPlaylistItem(ViewHolder holder, Playlist playlist) {
        holder.iv_playlistSongItem.setBackground(playListFragment.getContext().getDrawable(R.drawable.ic_playlist));

        switch (playlist.getName()) {
            case RECENTLY_ADDED_PLAYLIST_NAME:
                holder.tv_playlistSongItem.setTypeface(null, Typeface.BOLD);
                holder.tv_playlistSongItem.setText(R.string.RecentlyAddedPlaylist);
                break;
            case RECENTLY_PLAYED_PLAYLIST_NAME:
                holder.tv_playlistSongItem.setTypeface(null, Typeface.BOLD);
                holder.tv_playlistSongItem.setText(R.string.RecentlyPlayedPlaylist);
                break;
            case MOSTLY_PLAYED_PLAYLIST_NAME:
                holder.tv_playlistSongItem.setTypeface(null, Typeface.BOLD);
                holder.tv_playlistSongItem.setText(R.string.MostlyPlayedPlaylist);
                break;
            default:
                holder.tv_playlistSongItem.setText(playlist.getName());
                break;
        }
    }

    private void bindSongItem(ViewHolder holder, Song song) {
        holder.iv_playlistSongItem.setBackground(playListFragment.getContext().getDrawable(R.drawable.ic_music));
        holder.tv_playlistSongItem.setText(song.getName());

        holder.ll_artist_genre.setVisibility(View.VISIBLE);
        holder.tv_artist_song.setVisibility(View.VISIBLE);
        holder.tv_duration_song.setVisibility(View.VISIBLE);
        holder.tv_duration_genre.setVisibility(View.VISIBLE);

        holder.tv_artist_song.setText(song.getArtist().isEmpty() ? "unknown artist" : song.getArtist());
        holder.tv_duration_genre.setText(song.getGenre());
        holder.tv_duration_song.setText(song.getDurationString());
    }

    @Override
    public long getUniqueItemId(int position) {
        return playlistSongList.get(position).hashCode();
    }

    public static class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView tv_playlistSongItem;
        ImageView iv_playlistSongItem;
        LinearLayout ll_artist_genre;
        TextView tv_artist_song;
        TextView tv_duration_song;
        TextView tv_duration_genre;

        public ViewHolder(View itemView) {
            super(itemView, R.id.iv_item, false); // Using the ImageView as the drag handle
            tv_playlistSongItem = itemView.findViewById(R.id.tv_item_name);
            iv_playlistSongItem = itemView.findViewById(R.id.iv_item);
            ll_artist_genre = itemView.findViewById(R.id.ll_artist_genre);
            tv_artist_song = itemView.findViewById(R.id.tv_item_artist);
            tv_duration_song = itemView.findViewById(R.id.item_tv_duration);
            tv_duration_genre = itemView.findViewById(R.id.tv_item_genre);
        }
    }
}
