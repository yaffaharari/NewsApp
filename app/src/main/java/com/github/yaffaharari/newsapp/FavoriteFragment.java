package com.github.yaffaharari.newsapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.yaffaharari.newsapp.adapter.ArticleAdapter;
import com.github.yaffaharari.newsapp.adapter.onLongItemClickListener;
import com.github.yaffaharari.newsapp.databinding.FragmentFavoriteBinding;
import com.github.yaffaharari.newsapp.models.Article;
import com.github.yaffaharari.newsapp.models.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FavoriteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private RecyclerView.LayoutManager mLayoutManager;
    private FragmentFavoriteBinding binding;
    private ArrayList<Article> articles;
    private ArticleAdapter mAdapter;
    private int mCurrentItemPosition;
    private SharedPreferences preferences;
    private String strpref;
    private Boolean isFromFavFrag;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
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
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.favorite_fragment_title);

        preferences = getActivity().getSharedPreferences(Constants.FAVORITE_ARTICLE_KEY, Context.MODE_PRIVATE);
        strpref = preferences.getString(Constants.FAVORITE_ARTICLE_KEY, "");

        Type type = new TypeToken<List<Article>>() {
        }.getType();
        articles = new Gson().fromJson(strpref, type);
        fillFavoriteList();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.favorite_item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                articles.remove(mCurrentItemPosition);
                mAdapter.notifyDataSetChanged();
                fillFavoriteList();
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(Constants.FAVORITE_ARTICLE_KEY, new Gson().toJson(articles));
                editor.commit();
                Toast.makeText(getActivity(), "delete item click evenet", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void fillFavoriteList() {
        if (articles != null && articles.size() > 0) {
            mLayoutManager = new LinearLayoutManager(getActivity());
            mAdapter = new ArticleAdapter(articles, true);
            mAdapter.setOnLongItemClickListener(new onLongItemClickListener() {
                @Override
                public void ItemLongClicked(View v, int position) {
                    mCurrentItemPosition = position;
                    v.showContextMenu();
                }
            });
            binding.recyclerview.setLayoutManager(mLayoutManager);
            binding.recyclerview.setAdapter(mAdapter);
            registerForContextMenu(binding.recyclerview);
        } else
            binding.emptyListImg.setVisibility(View.VISIBLE);
    }

}
