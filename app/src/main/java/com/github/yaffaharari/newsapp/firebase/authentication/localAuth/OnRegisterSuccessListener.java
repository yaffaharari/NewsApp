package com.github.yaffaharari.newsapp.firebase.authentication.localAuth;

import androidx.fragment.app.Fragment;

import com.github.yaffaharari.newsapp.models.User;

public interface OnRegisterSuccessListener {

    void fillUserHeader(User user);
    void replaceFragment(Fragment currentFrag, Class replaceTo, int pointInNavMenu);
    boolean isRegistered();
    //void addUserToUserProfileList(User user);
    //HashMap<String, User> getUserListHashMap();
}
