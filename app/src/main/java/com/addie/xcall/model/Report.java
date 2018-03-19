package com.addie.xcall.model;

/**
 * Class to store attributes of a post-call report to be submitted to the server
 */
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
