package com.github.yaffaharari.newsapp.sqlite;


import android.net.Uri;

public class NewsDatabaseContract {

    public static final int DB_VERTION = 1;
    public static final String DB_NAME = "news.db";

    public static class Articles{

        public static String TABLE_NAME = "articles";
        public static String _ID = "_id";
        public static String TITLE = "title";
        public static String DESCRIPTION = "description";
        public static String Image_URI = "imageUri";
        public static String LINK = "link";
        public static String PUB_DATE = "pubDate";

        public static final int STATUS_DIR = 1;
        public static final int STATUS_ITEM_ID = 2;

        public static final String AUTHORITY = "com.github.yaffaharari.newsapp";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    }
}
