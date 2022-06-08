package com.github.yaffaharari.newsapp;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.github.yaffaharari.newsapp.databinding.FragmentUserProfileBinding;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.OnRegisterSuccessListener;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.SignInFragment;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.SignUpFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FragmentUserProfileBinding binding;

    private static final String EMAIL = "email";
    private static CallbackManager callbackManager;

    private OnRegisterSuccessListener listener;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    FirebaseUser user;
private boolean launchSingInFrag;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.user_profile_fragment_title);
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               user = firebaseAuth.getCurrentUser();
               // HashMap<String, User> userHashMap = listener.getUserListHashMap();
                if (user != null)
                    launchSingInFrag = true;
                else
                    launchSingInFrag = false;
            }
        };
        auth.addAuthStateListener(authListener);
        binding.btnLocalRegister.setOnClickListener(this);
    /*  binding.loginButton.setReadPermissions(Arrays.asList(EMAIL));
        binding.loginButton.setFragment(this);
        // Callback registration
        binding.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getActivity(), "you are currently login with your facebook account. great! we are friends", Toast.LENGTH_LONG).show();
                binding.facebookRegisterDetails.setText("User ID: "
                        + loginResult.getAccessToken().getUserId()
                        + "\n" +
                        "Auth Token: "
                        + loginResult.getAccessToken().getToken()
                );
            }

            @Override
            public void onCancel() {
                // App code
                binding.facebookRegisterDetails.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                binding.facebookRegisterDetails.setText("Login attempt failed.");
            }
        });*/
    }

    private void showToausst() {
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_local_register:
                replaceAppropriateFragment(user);
                break;
        }
    }

    private void replaceAppropriateFragment(FirebaseUser user) {
        if(launchSingInFrag)  // if (user != null) //launch SignInFragment
            listener.replaceFragment(this, SignInFragment.class, 5);
        else   //launch SignUpFragment
            listener.replaceFragment(this, SignUpFragment.class, 5);
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

    public void setOnRegisterSuccessListener(OnRegisterSuccessListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null)
            auth.removeAuthStateListener(authListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
