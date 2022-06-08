package com.github.yaffaharari.newsapp.firebase.notifitations;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.sax.Element;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.bumptech.glide.request.target.NotificationTarget;
import com.github.yaffaharari.newsapp.DetailsActivity;
import com.github.yaffaharari.newsapp.MainActivity;
import com.github.yaffaharari.newsapp.R;
import com.github.yaffaharari.newsapp.models.Article;
import com.github.yaffaharari.newsapp.models.Constants;
import com.github.yaffaharari.newsapp.sqlite.NewsDatabaseContract;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final String ADMIN_CHANNEL_ID = "admin_channel";
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private String title, description, link, imageUri, desText, pubDate;
    private Document doc;
    private Element img;
    private Notification notification;
    private final int NOTIFICATION_ID = 123;
    private NotificationTarget notificationTarget;
    private Article article;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().isEmpty()) //when the app in foreground
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        else
            showNotification(remoteMessage.getData());
        insertAndUpdateDB();
    }

    private void showNotification(String title, String body) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            setupChannels();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentInfo("Info")
                        .setContentIntent(pendingIntent);
        notification = notificationBuilder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    private void showNotification(Map<String, String> data) {

        title = data.get("title");
        description = data.get("description");
        link = data.get("link");
        pubDate = data.get("pubDate");

      /*  doc = Jsoup.parse(description);

        desText = doc.body().text();
        img = doc.select("img").first();
        imageUri = img.attr("src");
*/
        //article = new Article(null, null, title, desText, imageUri, link, pubDate, null);
        article = new Article(null, null, title, description, null, link, pubDate, null);

        RemoteViews mContentView = new RemoteViews(getPackageName(), R.layout.notification_layout);

        // mContentView.setImageViewUri(R.id.image, Uri.parse(imageUri));

        //Bitmap bitmap = getImgBitmap(imageUri);
        //mContentView.setImageViewBitmap(R.id.image, bitmap);
        mContentView.setTextViewText(R.id.title, title);
        mContentView.setTextViewText(R.id.description, desText);
        mContentView.setTextViewText(R.id.link, link);

        //Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent resultIntent = new Intent(this, DetailsActivity.class);
        Intent backToMainIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(Constants.CURRENT_ACTICAL, article);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(backToMainIntent);
        stackBuilder.addNextIntent(resultIntent);

        pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            setupChannels();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //.setContentTitle(title)
                        //.setContentText(body)
                        .setContentInfo("Info")
                        .setContentIntent(pendingIntent)
                        .setContent(mContentView);

        notification = notificationBuilder.build();
        notificationManager.notify(new Random().nextInt(), notification);
    }

    private void insertAndUpdateDB() {
        Cursor cursor = getBaseContext().getContentResolver().query(NewsDatabaseContract.Articles.CONTENT_URI, null, null, null, NewsDatabaseContract.Articles._ID+" DESC");
        if(cursor.getCount() >= 40){
            if (cursor.moveToFirst()) {
                int row = getApplicationContext().getContentResolver().delete(NewsDatabaseContract.Articles.CONTENT_URI, NewsDatabaseContract.Articles._ID + "=?", new String[]{String.valueOf(40)});
                Log.d(TAG, String.valueOf(row) + "deleted");
            }
        }else{
            ContentValues values = new ContentValues();
            values.put(NewsDatabaseContract.Articles.TITLE, article.getTitle());
            values.put(NewsDatabaseContract.Articles.DESCRIPTION, article.getDescription());
            values.put(NewsDatabaseContract.Articles.Image_URI, article.getUrlToImage());
            values.put(NewsDatabaseContract.Articles.LINK, article.getUrl());
            values.put(NewsDatabaseContract.Articles.PUB_DATE, article.getPublishedAt());
            getApplicationContext().getContentResolver().insert(NewsDatabaseContract.Articles.CONTENT_URI, values);
        }
    }

    private Bitmap getImgBitmap(String imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageUri).getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}