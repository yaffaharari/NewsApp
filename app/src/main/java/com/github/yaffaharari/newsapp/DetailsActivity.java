package com.github.yaffaharari.newsapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.github.yaffaharari.newsapp.adapter.MyViewHolder;
import com.github.yaffaharari.newsapp.databinding.ActivityDetailsBinding;
import com.github.yaffaharari.newsapp.models.Article;
import com.github.yaffaharari.newsapp.models.Constants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {


    ActivityDetailsBinding binding;

    boolean appBarExpanded = true;
    private Article currentArticle;
    private InterstitialAd mInterstitialAd;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private boolean isItemFav = false;
    private String currentFavList;

    private ArrayList<Article> articles = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setOptionMenuListener();

       // initInterstitialAd();

        pref = getSharedPreferences(Constants.FAVORITE_ARTICLE_KEY, Context.MODE_PRIVATE);
        currentFavList = pref.getString(Constants.FAVORITE_ARTICLE_KEY, "");

        Intent intent = getIntent();
        currentArticle = (Article) intent.getExtras().getSerializable(MyViewHolder.CURRENT_ACTICAL);
        isItemFav = intent.getBooleanExtra(Constants.IS_FROM_FAV_FRAG_KEY, false);
        fillArticleDetailsLayout(currentArticle);

        if (isFavListExists(currentFavList)) {
            fillFavList(currentFavList);
            if(isArticleExistsInFav(currentArticle))
                isItemFav = true;
        }
    }

    private void fillFavList(String currentFavList) {
        Type type = new TypeToken<List<Article>>() {
        }.getType();
        articles = new Gson().fromJson(currentFavList, type);
    }

    private boolean isFavListExists(String currentFavListJson) {
        if (currentFavListJson != "")
            return true;
        else
            return false;
    }

 /*   private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        // ca-app-pub-4010545542405638/8645092267
        //for test: ca-app-pub-3940256099942544/1033173712
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest adRequestInter = new AdRequest.Builder().build();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }
        });
        mInterstitialAd.loadAd(adRequestInter);
    }*/

    private void setOptionMenuListener() {
        binding.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                //  Vertical offset == 0 indicates appBar is fully expanded.
                if (Math.abs(verticalOffset) > 200) {
                    appBarExpanded = false;
                    invalidateOptionsMenu();
                } else {
                    appBarExpanded = true;
                    invalidateOptionsMenu();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void fillArticleDetailsLayout(Article currentArticle) {
        binding.toolbarLayout.setTitle(" ");
        Glide.with(getApplicationContext()).load(currentArticle.getUrlToImage()).into(binding.expandedImage);
        BitmapFromURLAsyncTask bitmap = new BitmapFromURLAsyncTask();
        bitmap.execute(currentArticle.getUrlToImage());

        TextView articleTitle = (TextView) findViewById(R.id.article_title);
        TextView publishedDate = (TextView) findViewById(R.id.published_date);
        TextView sourceName = (TextView) findViewById(R.id.source_name);
        TextView author = (TextView) findViewById(R.id.author);
        TextView shortDescription = (TextView) findViewById(R.id.short_description);
        Button goToFullArticleBtn = (Button) findViewById(R.id.goto_full_article_btn);
        articleTitle.setText(currentArticle.getTitle());
        publishedDate.setText(formattingDate());
        //sourceName.setText(currentArticle.getSource().getName());
        //author.setText(currentArticle.getAuthor());
        shortDescription.setText(currentArticle.getDescription());
        goToFullArticleBtn.setTag(currentArticle);
        goToFullArticleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Article crntArticle = (Article) v.getTag();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"+crntArticle.getUrl()));
                startActivity(intent);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String formattingDate() {
        DateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SSX", Locale.US);
        String inputText = currentArticle.getPublishedAt();
        Date date = null;
        try {
            date = inputFormat.parse(inputText);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        String outputText = outputFormat.format(date);
        return outputText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, currentArticle.getUrl());
                startActivity(Intent.createChooser(shareIntent, "Share link using"));
                return true;
            case R.id.add_to_favorite:
                saveFavArticles(item);
                isItemFav = true;
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isItemFav)
            menu.getItem(1).setIcon(R.drawable.baseline_fill_favorite_white_18dp);
        return true;
    }

    private void saveFavArticles(MenuItem item) {
        //if we have favorite articles
        if (isFavListExists(currentFavList)) {
            fillFavList(currentFavList);
            //It returns true if the specified element is found in the list else it gives false.
            if (!isArticleExistsInFav(currentArticle)) {
                articles.add(currentArticle);
                editor = pref.edit();
                editor.putString(Constants.FAVORITE_ARTICLE_KEY, new Gson().toJson(articles));
            }
        } else {
            editor = pref.edit();
            articles.add(currentArticle);
            editor.putString(Constants.FAVORITE_ARTICLE_KEY, new Gson().toJson(articles));
            Toast.makeText(this, "current article added to favorites", Toast.LENGTH_SHORT).show();
        }
        editor.commit();
    }

    private boolean isArticleExistsInFav(Article currentArticle) {
        if (articles.contains(currentArticle))
            return true;
         else
            return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }


//------------------AsyncTask class-----------------------

    public class BitmapFromURLAsyncTask extends AsyncTask<String, Void, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            if (bitmap != null) {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @SuppressWarnings("ResourceType")
                    @Override
                    public void onGenerated(Palette palette) {
                        int vibrantColor = palette.getVibrantColor(R.color.primary_500);
                        binding.toolbarLayout.setContentScrimColor(vibrantColor);
                        binding.toolbarLayout.setStatusBarScrimColor(R.color.black_trans80);
                    }
                });
            } else
                binding.toolbarLayout.setBackgroundColor(getResources().getColor(R.color.black_trans80));
        }
    }
}


