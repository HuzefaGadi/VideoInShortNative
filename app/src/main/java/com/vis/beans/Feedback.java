package com.vis.beans;

/**
 * Created by huzefaasger on 12-01-2016.
 */
public class Feedback {

    private String userId;
    private String emailId;
    private String feedback;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
