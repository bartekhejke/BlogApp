package com.bartekhejke.blogapp;

import java.sql.Timestamp;
import java.util.Date;

public class BlogPost {
    public String description, image_url, user_id;
    public Date timestamp;

    public BlogPost(String description, String image_url, String user_id, Date timestamp) {
        this.description = description;
        this.image_url = image_url;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BlogPost(){

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }


}
