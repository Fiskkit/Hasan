package com.fiskkit.instantEmail.models;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by joshuaellinger on 4/8/15; updated by hasan on 6/02/17.
 */
@Entity
public class EmailPriority {
  @Id @GeneratedValue private Integer id;
    private Integer userId;
    private Integer sendAfter;
    private LocalDateTime addedOn;

    public EmailPriority() { 
      addedOn = LocalDateTime.now();
    }

    public LocalDateTime getAddedOn() {
      return addedOn;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSendAfter() {
        return sendAfter;
    }

    public void setSendAfter(Integer sendAfter) {
        this.sendAfter = sendAfter;
    }
}
