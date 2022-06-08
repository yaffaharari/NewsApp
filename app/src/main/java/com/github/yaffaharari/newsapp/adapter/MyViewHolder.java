package com.github.yaffaharari.newsapp.adapter;


import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.yaffaharari.newsapp.DetailsActivity;
import com.github.yaffaharari.newsapp.databinding.ArticleRowBinding;
import com.github.yaffaharari.newsapp.models.Article;
import com.github.yaffaharari.newsapp.models.Constants;

public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String CURRENT_ACTICAL = "currentArticleForDetailsActivity";
    private ArticleRowBinding rowBinding;
    private Article mArticle;
    private onLongItemClickListener mListener;
    private boolean isFromFavFrag;

    public MyViewHolder(ArticleRowBinding binding, onLongItemClickListener listener, boolean isFromFavFrag) {
        super(binding.getRoot());
        this.rowBinding = binding;
        this.mListener = listener;
        this.isFromFavFrag = isFromFavFrag;
    }

    public void bind(Article currentArticle, final int position) {
        mArticle = currentArticle;
        rowBinding.primaryText.setText(currentArticle.getTitle());
        rowBinding.subText.setText(currentArticle.getDescription());
        Glide.with(rowBinding.getRoot().getContext())
                .load(currentArticle.getUrlToImage())
                .into(rowBinding.mediaImage);

        rowBinding.getRoot().setOnClickListener(this);
        rowBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null)
                    mListener.ItemLongClicked(view, position);
                return true;
            }
        });
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(rowBinding.getRoot().getContext(), DetailsActivity.class);
        intent.putExtra(CURRENT_ACTICAL, mArticle);
        intent.putExtra(Constants.IS_FROM_FAV_FRAG_KEY, isFromFavFrag);
        view.getContext().startActivity(intent);
    }
}
