package com.fiskkit.instantEmail.models.mysql;

import org.joda.time.DateTime;

public class Users {
	Integer userId;
	String first_name;
	String last_name;
	String name;
	String bio;
	String updated_at;
	String created_at;
	String facebook_id;
	String linkedin_id;
	String linkedin_profile_image;
	String location;
	String org_id;
	String org_slug;
	String org_name;
	String org_mission;
	String org_logo;
	String org_url;
	String org_url_text;
	String org_created_at;
	String respect_count;
	String bookmarks_count;
	String share_count;
	String shared_count;
	String user_fisk_count;

	public Integer getUserId() {
		return userId;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public String getName() {
		return name;
	}

	public String getBio() {
		return bio;
	}

	public DateTime getUpdated_at() {
		return new DateTime(Long.parseLong(updated_at));
	}

	public DateTime getCreated_at() {
		return new DateTime(Long.parseLong(created_at));
	}

	public Long getFacebook_id() {
		return Long.parseLong(facebook_id);
	}

	public Long getLinkedin_id() {
		return Long.parseLong(linkedin_id);
	}

	public String getLinkedin_profile_image() {
		return linkedin_profile_image;
	}

	public String getLocation() {
		return location;
	}

	public Long getOrg_id() {
		return Long.parseLong(org_id);
	}

	public String getOrg_slug() {
		return org_slug;
	}

	public String getOrg_name() {
		return org_name;
	}

	public String getOrg_mission() {
		return org_mission;
	}

	public String getOrg_logo() {
		return org_logo;
	}

	public String getOrg_url() {
		return org_url;
	}

	public String getOrg_url_text() {
		return org_url_text;
	}

	public String getOrg_created_at() {
		return org_created_at;
	}

	public String getRespect_count() {
		return respect_count;
	}

	public String getBookmarks_count() {
		return bookmarks_count;
	}

	public String getShare_count() {
		return share_count;
	}

	public String getShared_count() {
		return shared_count;
	}

	public String getUser_fisk_count() {
		return user_fisk_count;
	}
}
