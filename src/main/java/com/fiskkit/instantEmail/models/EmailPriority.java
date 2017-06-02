package com.fiskkit.model.models;

import java.time.LocalDateTime;
import javax.persistence.Entity;

/**
 * Created by joshuaellinger on 4/8/15; updated by hasan on 6/02/17.
 */
@Entity
public class EmailPriority {
    private int userId;
    private int sendAfter;
    private LocalDateTime addedOn;

    public EmailPriority() { 
      addedOn = LocalDateTime.now();
    }

    public LocalDateTime getAddedOn() {
      return addedOn;
    }

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