package com.example.android.talktime.model;

public class User {
    private String email;
    private long duration;
    private String fcmToken;

    public User(String email, long duration, String fcmToken) {

        this.email = email;
        this.duration = duration;
        this.fcmToken = fcmToken;
    }

    public User() {

    }

    public User(String email, long duration) {
        this.email = email;
        this.duration = duration;
    }

    public String getEmail() {
        return this.email;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {

        return fcmToken;
    }
}
