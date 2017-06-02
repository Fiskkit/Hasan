package com.fiskkit.instantEmail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by joshuaellinger on 3/30/15, updated by hdiwan on 6/02/17.
 */
@Entity
public class User {
	private static final Logger LOGGER = Logger.getLogger("");
    
    @Id @GeneratedValue private int id;
    private String FirstName;
    private String LastName;
    private String Name;
    private String Email;
    private String facebookId;
    private String linkedinProfileImage;
    private String imageUrl;
    private String respectCount;
    private String respectCountHumanized;
    private String userProfileLink;
    private LocalDateTime addedOn;

    public User() {
      addedOn = LocalDateTime.now();
    }

    public LocalDateTime getAddedOn() {
      return addedOn;
    }

    public void setAddedOn(LocalDate date) {
      addedOn = LocalDateTime().now();
    }

    public User(ResultSet rs) {
        try {
            int id = rs.getInt("id");
            if (id > 0) {
                setId(id);
            }

            String firstName = rs.getString("first_name");
            if (!firstName.isEmpty()) {
                setFirstName(firstName);
            }

            String lastName = rs.getString("last_name");
            if (!lastName.isEmpty()) {
                setLastName(lastName);
            }

            String name = rs.getString("name");
            if (!name.isEmpty()) {
                setName(name);
            }

            String email = rs.getString("email");
            if (!email.isEmpty()) {
                setEmail(email);
            }

            String facebookId = rs.getString("facebook_id");
            if (facebookId != null && !facebookId.isEmpty()) {
                setFacebookId(facebookId);
            }

            String linkedinProfileImage = rs.getString("linkedin_profile_image");
            if (linkedinProfileImage != null && !linkedinProfileImage.isEmpty()) {
                setLinkedinProfileImage(linkedinProfileImage);
            }

            String respectCount = rs.getString("respect_count");
            if (respectCount != null && !respectCount.isEmpty()) {
                setRespectCount(respectCount);
            }

        } catch (final SQLException e) {
        	LOGGER.log(Level.SEVERE, "", e);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.userProfileLink = "http://fiskkit.com/user/" + id;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getLinkedinProfileImage() {
        return linkedinProfileImage;
    }

    public void setLinkedinProfileImage(String linkedinProfileImage) {
        this.linkedinProfileImage = linkedinProfileImage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRespectCount() {
        return respectCount;
    }

    public void setRespectCount(String respectCount) {
        this.respectCount = respectCount;
        this.respectCountHumanized = Integer.valueOf(respectCount);
    }

    public String getRespectCountHumanized() {
        return respectCountHumanized;
    }

    public String getUserProfileLink() {
        return userProfileLink;
    }

    public void setUserProfileLink(String userProfileLink) {
        this.userProfileLink = userProfileLink;
    }

}
