package com.fiskkit.instantEmail.models;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
/**
 * Created by joshuaellinger on 4/13/15, updated by hdiwan 6/01/17.
 */
public class Fisk {
    @Id @GeneratedValue(strategy=GenerationType.AUTO) private Integer id;
    @ManyToOne @JoinColumn private User user;

    // FIXME @ManyToOne @JoinColumn private Article fiskedArticle;
    private String fiskUrl;
    private LocalDateTime addedOn;

    public void setAddedOn(LocalDateTime date) {
      addedOn = LocalDateTime.now();
    }

    public LocalDateTime getAddedOn() { 
      return addedOn;
    }
    
    public Fisk() {
      setAddedOn(null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUrl(String article_id){
        this.fiskUrl = "http://fiskkit.com/articles/" + article_id + "/fisk/" + id;
    }

    public String getUrl(){
        return fiskUrl;
    }
}
