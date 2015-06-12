package com.example.android.player;

/**
 * Created by Alumnos on 07/05/2015.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private String duration;

    public Song(long id, String title, String artist, String duration) {
        this.setId(id);
        this.setTitle(title);
        this.setArtist(artist);
        this.setDuration(duration);
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
