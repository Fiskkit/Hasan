package com.fiskkit.model;

import javax.persistence.Entity;

/**
 * Created by joshuaellinger on 4/8/15.
 */
@Entity
public class EmailPriority {
    private int userId;
    private int sendAfter;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSendAfter() {
        return sendAfter;
    }

    public void setSendAfter(int sendAfter) {
        this.sendAfter = sendAfter;
    }
}
