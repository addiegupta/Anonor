package com.addie.xcall.model;

public class User {
    private long duration;
    private String fcm_token;

    public User(long duration, String fcm_token) {

        this.duration = duration;
        this.fcm_token = fcm_token;
    }

    public User() {

    }



    public long getDuration() {
        return this.duration;
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
