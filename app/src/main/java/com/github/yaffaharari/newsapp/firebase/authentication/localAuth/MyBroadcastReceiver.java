package com.github.yaffaharari.newsapp.firebase.authentication.localAuth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.yaffaharari.newsapp.models.Constants;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Constants.androidId))
            Log.i(context.getPackageName(), "delete account happen");
          //  Toast.makeText(context, "delete account happen", Toast.LENGTH_LONG).show();
    }
}
