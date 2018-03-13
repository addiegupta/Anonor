package com.addie.xcall.model;


public class Report {

    public String remoteUser;
    public String problem;
    public boolean reportUser;

    public Report(String remoteUser, String problem,boolean reportUser) {

        this.remoteUser = remoteUser;
        this.problem = problem;
        this.reportUser = reportUser;
    }

    public Report() {
    }

}
