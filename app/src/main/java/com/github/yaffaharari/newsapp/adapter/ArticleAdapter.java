package com.github.yaffaharari.newsapp.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.github.yaffaharari.newsapp.R;
import com.github.yaffaharari.newsapp.databinding.ArticleRowBinding;
import com.github.yaffaharari.newsapp.models.Article;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private List<Article> articleList;
    private onLongItemClickListener mOnLongItemClickListener;
private boolean isFromFavFrag;

    public ArticleAdapter(List<Article> articleList, boolean isFromFavFrag) {
        this.articleList = articleList;
        this.isFromFavFrag = isFromFavFrag;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ArticleRowBinding rowBinding = DataBindingUtil.inflate(layoutInflater, R.layout.article_row, parent, false);
        return new MyViewHolder(rowBinding, mOnLongItemClickListener, isFromFavFrag);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Article currentArticle = articleList.get(position);
        holder.bind(currentArticle, position);
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public void setOnLongItemClickListener(onLongItemClickListener onLongItemClickListener) {
        mOnLongItemClickListener = onLongItemClickListener;
    }

}
