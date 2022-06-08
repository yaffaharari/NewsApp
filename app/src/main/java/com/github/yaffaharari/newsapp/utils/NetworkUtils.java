package com.github.yaffaharari.newsapp.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yassinegharsallah on 01/04/2017.
 */

public class NetworkUtils {


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
