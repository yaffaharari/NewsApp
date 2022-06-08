package com.github.yaffaharari.newsapp.adapter;


//import com.github.yaffaharari.newsapp.databinding.ArticleRowBinding;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.github.yaffaharari.newsapp.R;
import com.github.yaffaharari.newsapp.databinding.ArticleRowBinding;
import com.github.yaffaharari.newsapp.sqlite.NewsDatabaseContract.Articles;

public class CustomCursorAdapter extends CursorAdapter {

    public CustomCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ArticleRowBinding rowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.article_row, parent, false);
        return rowBinding.getRoot();
      //  return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(Articles.TITLE));
        @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(Articles.DESCRIPTION));
        @SuppressLint("Range") String imgUrl = cursor.getString(cursor.getColumnIndex(Articles.Image_URI));
        @SuppressLint("Range") String link = cursor.getString(cursor.getColumnIndex(Articles.LINK));
        @SuppressLint("Range") String bupDate = cursor.getString(cursor.getColumnIndex(Articles.PUB_DATE));

        ArticleRowBinding rowBinding = DataBindingUtil.getBinding(view);

        rowBinding.primaryText.setText(title);
        rowBinding.subText.setText(description);
        Glide.with(rowBinding.getRoot().getContext())
                .load(imgUrl)
                .into(rowBinding.mediaImage);
        }
}
