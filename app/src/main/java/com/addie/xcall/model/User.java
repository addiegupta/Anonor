package com.addie.xcall.model;

public class User {
    private String email;
    private long duration;
    private String fcm_token;

    public User(String email, long duration, String fcm_token) {

        this.email = email;
        this.duration = duration;
        this.fcm_token = fcm_token;
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

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getFcm_token() {

        return fcm_token;
    }
}
