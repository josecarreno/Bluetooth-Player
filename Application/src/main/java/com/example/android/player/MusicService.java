package com.example.android.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.android.bluetoothchat.BluetoothChatService;
import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //lista de canciones
    private ArrayList<Song> songs;
    //posicion actual
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private String songArtist = "";
    private String songDuration = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        songPosn = 0;
        initMusicPlayer();
        rand = new Random();
    }
    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // setear la clase para cuando la instancia del reproductor este preparada
        player.setOnPreparedListener(this);
        // setear la clase para cuando se hay completado el playback de la cancion
        player.setOnCompletionListener(this);
        // setea la clase para cuando haya un error
        player.setOnErrorListener(this);

    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    private void sendMessage(String message) {
        BluetoothChatService bcs = BluetoothChatService.getInstance(null, null);
        // Check that we're actually connected before trying anything
        /*
        if (bcs.getState() != BluetoothChatService.STATE_CONNECTED) {
            return;
        }
        */

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bcs.write(send);
        }
    }

    public void playSong(){
        player.reset();
        // obtener cancion
        Song playSong = songs.get(songPosn);
        // seteo el titulo de la cancion para ser mostrado
        // como notificacion
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        songDuration = playSong.getDuration();

        // obtener id
        long currSong = playSong.getId();
        //setear uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        sendMessage(songTitle + "-" + songArtist + "-" + songDuration);
        player.prepareAsync();

    }
    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        System.out.println("OnErrorService");
        mp.reset();
        return false;
    }

    @Override
    public void onDestroy() {
        System.out.println("OnDestroyService");
        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Iniciar playback
        System.out.println("OnPrepared");
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Reproduciendo")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0)
            songPosn=songs.size()-1;
        playSong();
    }

    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn >= songs.size())
                songPosn=0;
        }
        playSong();
    }

    // clase anidada
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
}
