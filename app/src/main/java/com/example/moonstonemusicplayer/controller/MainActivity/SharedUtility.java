package com.example.moonstonemusicplayer.controller.MainActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.Database.Playlist.DBPlaylists;
import com.example.moonstonemusicplayer.model.PlayListActivity.Song;

public class SharedUtility {

    public static void showAlertDialogAddToPlaylists(LayoutInflater inflater, Context context, final Song song){
        final String[] allPlaylistNames = DBPlaylists.getInstance(context).getAllPlaylistNames();

        View dialogView = inflater.inflate(R.layout.add_to_playlist_layout, null);
        ListView lv_playlist_alert = dialogView.findViewById(R.id.lv_playlists_alert);
        final EditText et_addNewPlaylist = dialogView.findViewById(R.id.et_addNewPlaylist);

        lv_playlist_alert.setAdapter(new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,allPlaylistNames));

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton(android.R.string.no,null);
        dialogBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = et_addNewPlaylist.getText().toString();
                if(!text.isEmpty()){
                    DBPlaylists.getInstance(context).addToPlaylist(context,song,text);
                }
            }
        });
        dialogBuilder.setTitle("Füge den Song einer Playlist hinzu \noder erstelle eine neue.");

        final AlertDialog alertDialog  = dialogBuilder.show();

        lv_playlist_alert.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DBPlaylists.getInstance(context).addToPlaylist(context,song,allPlaylistNames[position]);
                alertDialog.dismiss();
            }
        });
    }
}
