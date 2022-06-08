package com.github.yaffaharari.newsapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.github.yaffaharari.newsapp.adapter.CustomCursorAdapter;
import com.github.yaffaharari.newsapp.api.APIClient;
import com.github.yaffaharari.newsapp.api.APIInterface;
import com.github.yaffaharari.newsapp.databinding.FragmentHomeBinding;
import com.github.yaffaharari.newsapp.models.Article;
import com.github.yaffaharari.newsapp.models.Constants;
import com.github.yaffaharari.newsapp.models.NewItem;
import com.github.yaffaharari.newsapp.sqlite.NewsDatabaseContract;
import com.github.yaffaharari.newsapp.utils.NetworkUtils;
import com.google.android.gms.ads.AdRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private AdRequest adRequest;

    private OnFragmentInteractionListener mListener;

    private FragmentHomeBinding binding;


    // private FragmentH
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.home_fragment_title);
       // initAds();
        Cursor cursor = getActivity().getContentResolver().query(NewsDatabaseContract.Articles.CONTENT_URI, null, null, null, null);
        if (cursor.getCount() == 0)
            if (NetworkUtils.isNetworkAvailable(getContext()))
                fillArticleList(); // insert news articles from news api first time and display it. 
             else
                //binding.internetStatus.setVisibility(View.VISIBLE);
                binding.listView.setEmptyView(getActivity().findViewById(R.id.emptyElement));
        fetchNewsArticlesFromDB(cursor);
    }

    private void fetchNewsArticlesFromDB(Cursor cursor) {
        cursor = getActivity().getContentResolver().query(NewsDatabaseContract.Articles.CONTENT_URI, null, null, null, NewsDatabaseContract.Articles._ID+" DESC");
        CustomCursorAdapter mAdapter = new CustomCursorAdapter(getContext(), cursor);
        binding.listView.setAdapter(mAdapter);
        binding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToDetailsActivity(id);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null)
            mListener.onFragmentInteraction(uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

//    private void initAds() {
//        MobileAds.initialize(getActivity().getApplicationContext(), "ca-app-pub-4010545542405638~9553326745");
//        adRequest = new AdRequest.Builder().build();
//        binding.adView.loadAd(adRequest);
//     /*   binding.adView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//                Log.d(HomeFragment.class.getSimpleName(),"onAdLoaded");
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Code to be executed when an ad request fails.
//                Log.d(HomeFragment.class.getSimpleName(),"onAdFailedToLoad" + errorCode);
//            }
//
//            @Override
//            public void onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//                Log.d(HomeFragment.class.getSimpleName(),"onAdOpened");
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//                Log.d(HomeFragment.class.getSimpleName(),"onAdLeftApplication");
//            }
//
//            @Override
//            public void onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//                Log.d(HomeFragment.class.getSimpleName(),"onAdClosed");
//            }
//        });*/
//    }

    private void fillArticleList() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);

        Call<NewItem> call = apiInterface.getListOfArticles();
        call.enqueue(new Callback<NewItem>() {
            @Override
            public void onResponse(Call<NewItem> call, Response<NewItem> response) {
                Log.d("MainActivity", response.message());
                if (response.isSuccessful() && response.body() != null) {
                    NewItem newItem = response.body();
                    List<Article> articles = newItem.getArticles();
                    insertArticleListIntoDB(articles);
                    //Collections.shuffle(articles);

                }
            }

            @Override
            public void onFailure(Call<NewItem> call, Throwable t) {
                Log.d("MainActivity", t.getMessage());

            }
        });
    }

    private void goToDetailsActivity(long id) {
        //Articles._ID + "=" + position
        Cursor cursor = getActivity().getContentResolver().query(Uri.withAppendedPath(NewsDatabaseContract.Articles.CONTENT_URI, String.valueOf(id)), null, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(NewsDatabaseContract.Articles.TITLE));
            @SuppressLint("Range")String description = cursor.getString(cursor.getColumnIndex(NewsDatabaseContract.Articles.DESCRIPTION));
            @SuppressLint("Range")String imgUrl = cursor.getString(cursor.getColumnIndex(NewsDatabaseContract.Articles.Image_URI));
            @SuppressLint("Range")String link = cursor.getString(cursor.getColumnIndex(NewsDatabaseContract.Articles.LINK));
            @SuppressLint("Range")String pubDate = cursor.getString(cursor.getColumnIndex(NewsDatabaseContract.Articles.PUB_DATE));
            @SuppressLint("Range")Article article = new Article(null, null, title, description, imgUrl, link, pubDate, null);

            Intent intent = new Intent(getContext(), DetailsActivity.class);
            intent.putExtra(Constants.CURRENT_ACTICAL, article);
            intent.putExtra(Constants.IS_FROM_FAV_FRAG_KEY, false); // is from fav frag? = false
            getContext().startActivity(intent);
        }
    }

    private void insertArticleListIntoDB(List<Article> articles) {
        for (Article article : articles) {
            ContentValues values = new ContentValues();
            values.put(NewsDatabaseContract.Articles.TITLE, article.getTitle());
            values.put(NewsDatabaseContract.Articles.DESCRIPTION, article.getDescription());
            values.put(NewsDatabaseContract.Articles.Image_URI, article.getUrlToImage());
            values.put(NewsDatabaseContract.Articles.LINK, article.getUrl());
            values.put(NewsDatabaseContract.Articles.PUB_DATE, article.getPublishedAt());
            getActivity().getContentResolver().insert(NewsDatabaseContract.Articles.CONTENT_URI, values);
        }
    }

}
