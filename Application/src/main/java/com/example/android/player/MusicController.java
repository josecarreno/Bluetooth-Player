package com.example.android.player;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.MediaController.MediaPlayerControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Jose on 11/05/2015.
 */
public class MusicController implements MediaPlayerControl {

    // Lista de canciones
    private ArrayList<Song> songList;
    // Servicio en background
    private MusicService musicSrv;

    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = true, playbackPaused = false;

    // Conectar el servicio
    private ServiceConnection musicConnection;

    public MusicController(Context context) {
        updateSongList(null, null, context);
        musicConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                //get service
                musicSrv = binder.getService();
                //pass list
                musicSrv.setList(songList);
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void startController(Context context) {

        if(playIntent == null){
            playIntent = new Intent(context, MusicService.class);
            context.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            context.startService(playIntent);
        }
    }

    public void updateSongList(Integer page, Integer itemsPerPage, Context context) {
        songList = null;
        songList = new ArrayList<>();
        if (page == null || page <= 0){
            page = 1;
        }
        if (itemsPerPage == null || itemsPerPage <=0) {
            itemsPerPage = 10;
        }
        ContentResolver musicResolver = context.getContentResolver();
        //Obtengo todos los archivos de musica del SD card
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            // obtener columnas
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            // agregar canciones a la lista
            int limInf = (page - 1) * itemsPerPage;
            int limSup = page * itemsPerPage;
            int i = 1;
            do {
                if (i > limInf && i <= limSup) {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    songList.add(new Song(thisId, thisTitle, thisArtist));
                }
                i++;
            }
            while (musicCursor.moveToNext());
        }

        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    public void pickSong(Integer song) {
        System.out.println("pickSong");
        try {
            musicSrv.setSong(song);
            musicSrv.playSong();
            if (playbackPaused) {
                playbackPaused = false;
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void destroyPlayer(Context context) {
        context.unbindService(musicConnection);
        context.stopService(playIntent);
        musicSrv=null;
    }

    public void playPrev() {
        musicSrv.playPrev();
    }

    public void playNext() {
        musicSrv.playNext();
    }

    @Override
    public void start() {
        musicSrv.playNext();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int i) {
        musicSrv.seek(i);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
