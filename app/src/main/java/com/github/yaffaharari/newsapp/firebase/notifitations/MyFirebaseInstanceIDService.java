package com.github.yaffaharari.newsapp.firebase.notifitations;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.github.yaffaharari.newsapp.models.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";

    private final String REFERENCE_NAME = "first_reference";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseUserInfoReference, userToken;

    @Override
    public void onNewToken(String s) {
        Log.e("NEW_TOKEN", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        FirebaseMessaging.getInstance().subscribeToTopic("newsUpdateTopic");
        // Get updated InstanceID token.

        String refreshedToken = FirebaseMessaging.getInstance().getToken().toString();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //  sendRegistrationToServer(refreshedToken);
        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString(Constants.FIREBASE_TOKEN, refreshedToken);

        //preferences.edit().putString(Constants.ANDROID_ID, androidId);
        preferences.edit().apply();

        sendRegistrationToServer(refreshedToken);
    }

  /*  @Override
    public void onTokenRefresh() {
        FirebaseMessaging.getInstance().subscribeToTopic("newsUpdateTopic");
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //  sendRegistrationToServer(refreshedToken);
        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putString(Constants.FIREBASE_TOKEN, refreshedToken);

        //preferences.edit().putString(Constants.ANDROID_ID, androidId);
        preferences.edit().apply();

        sendRegistrationToServer(refreshedToken);
    }*/

    private void sendRegistrationToServer(String refreshedToken) {
     /*   SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String androidId = preferences.getString(Constants.ANDROID_ID, "");*/

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseUserInfoReference = mFirebaseDatabase.getReference(Constants.androidId);
        userToken = mDatabaseUserInfoReference.child(Constants.FIREBASE_TOKEN);

        //String userToken = mDatabaseUserInfoReference.push().getKey();

        userToken.setValue(refreshedToken);
    }
}
