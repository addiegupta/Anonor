package com.example.android.talktime.model;

/**
 * Created by addie on 24-10-2017.
 */

public class Report {

    public String reportedUser;
    public String problem;

    public Report(String reportedUser, String problem) {

        this.reportedUser = reportedUser;
        this.problem = problem;
    }

    public Report() {
    }

}
