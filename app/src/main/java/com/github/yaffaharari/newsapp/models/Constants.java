package com.github.yaffaharari.newsapp.models;


import android.annotation.SuppressLint;
import android.provider.Settings;

import com.github.yaffaharari.newsapp.NewsApplication;

public class Constants {

    public static final String FAVORITE_ARTICLE_KEY = "favoriteArticleKey";

    public static final String USER_INFO = "userInfo";

    public static final String ANDROID_ID = "androidId";

    public static final String IS_FROM_FAV_FRAG_KEY = "isFromFavFragKey";

    public static final String USER_PROFILE_ACCOUNT_KEY = "user";

    public static final String FIREBASE_TOKEN = "firebaseToken";

    @SuppressLint("HardwareIds")
    public static final String androidId = Settings.Secure.getString(NewsApplication.getAppContext().getContentResolver(),
            Settings.Secure.ANDROID_ID);

    public static final int PICK_IMAGE_CAMERA = 1;
    public static final int PICK_IMAGE_GALLERY = 2;

    public static final String CURRENT_ACTICAL = "currentArticleForDetailsActivity";

}

