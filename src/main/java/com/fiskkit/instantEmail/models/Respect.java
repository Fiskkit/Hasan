package com.fiskkit.instantEmail.models;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * Created by joshuaellinger on 3/30/15.
 */
public class Respect {
    Integer id;
    // FIXME @ManyToOne @JoinColumn Article article;
    String articleId;
    // FIXME @ManyToOne @JoinColumn Fisk fisk
    Integer fiskId;
    @OneToOne @JoinColumn User user;
    @OneToOne @JoinColumn User author;
    @OneToOne @JoinColumn SentenceComment sentenceComment;
    LocalDateTime addedOn;

    public Respect() {
      addedOn = LocalDateTime.now();
    }

    public LocalDateTime getAddedOn() {
      return addedOn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public Integer getFiskId() {
        return fiskId;
    }

    public void setFiskId(Integer fiskId) {
        this.fiskId = fiskId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public SentenceComment getSentenceComment() {
        return sentenceComment;
    }

    public void setSentenceComment(SentenceComment sentenceComment) {
        this.sentenceComment = sentenceComment;
    }
}
