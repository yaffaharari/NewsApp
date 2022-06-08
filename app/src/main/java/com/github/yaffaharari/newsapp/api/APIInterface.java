package com.github.yaffaharari.newsapp.api;

import com.github.yaffaharari.newsapp.models.NewItem;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIInterface {

    //https://newsapi.org/v2/top-headlines?country=il&apiKey=64979477d975402a918d4b57bbb91c7b
    //https://newsapi.org/v2/top-headlines?country=us&apiKey=64979477d975402a918d4b57bbb91c7b
    @GET("?country=il&apiKey=64979477d975402a918d4b57bbb91c7b")
    Call<NewItem> getListOfArticles();


}
