package com.morlunk.mumbleclient;

import com.morlunk.jumble.model.User;

import java.util.ArrayList;

public class Chat {
    String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {

        return id;
    }

    String title;
    String image;
    String bio;
    String type;
//    ArrayList<User> users;
}
