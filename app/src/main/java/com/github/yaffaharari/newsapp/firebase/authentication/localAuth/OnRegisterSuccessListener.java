package com.github.yaffaharari.newsapp.firebase.authentication.localAuth;

import android.support.v4.app.Fragment;

import com.github.yaffaharari.newsapp.models.User;

public interface OnRegisterSuccessListener {

    void fillUserHeader(User user);
    void replaceFragment(Fragment currentFrag, Class replaceTo, int pointInNavMenu);
    boolean isRegistered();
    //void addUserToUserProfileList(User user);
    //HashMap<String, User> getUserListHashMap();
}
