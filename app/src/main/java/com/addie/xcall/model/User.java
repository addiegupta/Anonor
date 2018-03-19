package com.addie.xcall.model;

public class User {
    private long duration;
    private String fcm_token;
    private String call_request;

    public User(long duration, String fcm_token, String call_request) {
        this.duration = duration;
        this.fcm_token = fcm_token;
        this.call_request = call_request;
    }

    public User(long duration, String fcm_token) {

        this.duration = duration;
        this.fcm_token = fcm_token;
    }

    public User() {

    }

    public String isCall_request() {
        return call_request;
    }

    public void setCall_request(String call_request) {
        this.call_request = call_request;
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
