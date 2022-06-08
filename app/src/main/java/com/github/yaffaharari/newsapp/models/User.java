package com.github.yaffaharari.newsapp.models;

import java.io.Serializable;

public class User implements Serializable {

    private String uid;
    private String nickName;
    private String email;
    private String password;
    private String imageUri;

    public User() {
    }

    public User(String uid, String nickName, String email, String password, String imageUri) {
        this.uid = uid;
        this.nickName = nickName;
        this.email = email;
        this.password = password;
        this.imageUri = imageUri;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

}
