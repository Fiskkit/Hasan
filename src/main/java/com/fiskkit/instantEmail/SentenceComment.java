package com.fiskkit.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by joshuaellinger on 3/30/15; updated by hdiwan on 6/01/17.
 */
@Entity
public class SentenceComment {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) private Integer id;
    private String body;
    @ManyToOne @JoinColumn private User user;
    private Integer fiskId;
    private Integer articleId;
    private Integer wordCount;
    private Integer respectCount;
    private String commentUrl;
    private LocalDateTime addedOn;

    /**
     * Constructor
     */
    public SentenceComment() {
      addedOn = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getAddedOn() { 
      return addedOn;
    }

    public void setAddedOn(LocalDateTime d) {
      addedOn = LocalDateTime.now();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        // rely on email reader to do word wrapping
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getFiskId() {
        return fiskId;
    }

    public void setFiskId(Integer fiskId) {
        this.fiskId = fiskId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Integer getRespectCount() {
        return respectCount;
    }

    public void setRespectCount(Integer respectCount) {
        this.respectCount = respectCount;
    }

    public void setCommentUrl(String article_id) {
        this.commentUrl = "http://fiskkit.com/articles/" + article_id + "/fisk/" + fiskId;
    }
}
