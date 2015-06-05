package com.example.android.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;

import java.util.ArrayList;

/**
 * Created by Alumnos on 07/05/2015.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    //constructor
    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs=theSongs;
        songInf= LayoutInflater.from(c);
    }


    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // mapear cada cancion de la lista al layout
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.song, parent, false);
        // obtengo el titulo y el artista de la vista
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        // obtiene la cancion usando la posicion
        Song currSong = songs.get(position);
        // setea los titulos y artista de la Vista
        // usando los atributos del objeto cancion
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        // setea la posicion como tag
        songLay.setTag(position);
        return songLay;
    }
}
