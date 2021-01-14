package com.example.libfolder.notused.RadioFragment;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moonstonemusicplayer.R;
import com.example.moonstonemusicplayer.model.MainActivity.RadioFragment.Radio;

import java.util.List;

public class RadioListAdapter extends ArrayAdapter<Radio> {
  private List<Radio> radiolist;

  Context context;
  LayoutInflater layoutInflater;

  public RadioListAdapter(@NonNull Context context, List<Radio> radiolist) {
    super(context, R.layout.song_row_layout, radiolist);
    this.radiolist = radiolist;
    this.context = context;
    this.layoutInflater = layoutInflater.from(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    Radio aktuellerRadio = radiolist.get(position);

    View rowView;
    if(convertView != null){
      rowView = convertView;
    } else {
      rowView = layoutInflater.inflate(R.layout.radio_row_layout, parent, false);
    }

    //init the views of songRowView
    TextView tv_title = rowView.findViewById(R.id.tv_name_radio);
    TextView tv_uri = rowView.findViewById(R.id.tv_uri_radio);

    //set the views of songRowView
    tv_title.setText(aktuellerRadio.getName());
    tv_uri.setText(aktuellerRadio.getURI());
    return rowView;
  }


}
