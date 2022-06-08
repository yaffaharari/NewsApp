package com.github.yaffaharari.newsapp;


import android.app.Application;
import android.content.Context;

public class NewsApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        NewsApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return NewsApplication.context;
    }
}
