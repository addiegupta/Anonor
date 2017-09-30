package com.example.android.talktime.model;

public class User {
    private String email;
    private long  duration;

    public User(String email, long duration){
        this.email = email;
        this.duration = duration;
    }
    public String getEmail(){return this.email;}
    public long getDuration(){return this.duration;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
