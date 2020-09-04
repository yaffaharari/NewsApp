package com.github.yaffaharari.newsapp.sqlite;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.yaffaharari.newsapp.sqlite.NewsDatabaseContract.Articles;

public class DBOpenHelper extends SQLiteOpenHelper {

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql =
                " CREATE TABLE " + NewsDatabaseContract.Articles.TABLE_NAME
                        + "("
                        + NewsDatabaseContract.Articles._ID + " INTEGER PRIMARY KEY autoincrement ,"
                        + Articles.TITLE + " TEXT ,"
                        + Articles.DESCRIPTION + " TEXT ,"
                        + Articles.Image_URI + " REAL ,"
                        + Articles.LINK + " TEXT ,"
                        + Articles.PUB_DATE + " TEXT "
                        + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql= "DROP TABLE IF EXISTS " + NewsDatabaseContract.Articles.TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }
}

