package com.github.yaffaharari.newsapp.firebase.authentication.localAuth;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.github.yaffaharari.newsapp.HomeFragment;
import com.github.yaffaharari.newsapp.R;
import com.github.yaffaharari.newsapp.databinding.FragmentSignInBinding;
import com.github.yaffaharari.newsapp.databinding.ResetPasswordCustomAlertDialogBinding;
import com.github.yaffaharari.newsapp.models.Constants;
import com.github.yaffaharari.newsapp.models.User;
import com.github.yaffaharari.newsapp.utils.NetworkUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignInFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FragmentSignInBinding binding;

    private View alertView;

    private FirebaseAuth auth;

    private ResetPasswordCustomAlertDialogBinding resetPassDialogBinding;
    private AlertDialog.Builder resetPasswordBuilder;
    private AlertDialog resetPassDialog;

    private OnRegisterSuccessListener listener;

    private boolean isReseetPass;
    private boolean isToCloseAlert;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    public SignInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignInFragment.
     */
    public static SignInFragment newInstance(String param1, String param2) {
        SignInFragment fragment = new SignInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false);
        resetPassDialogBinding = DataBindingUtil.inflate(inflater, R.layout.reset_password_custom_alert_dialog, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if (auth.getCurrentUser() != null)  //user already signIn
                signInClickEvent();
            }
        });
        binding.btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetPasswordAlertDialog();
            }
        });
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().getSupportFragmentManager().popBackStack();
                goToSignUpFragment(); //is actually SignUpFragment
            }
        });
    }

    private void signInUser(final String email, final String password) {
        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        binding.progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                binding.password.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(getContext(), getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // binding.commentFromServer.setText("you have been success to register");
                            //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            getUserFromRealtimeFirebase(email);
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    new ContextThemeWrapper(getActivity(), R.style.AlertDialogTheme));
                            builder.setMessage("הרישום בוצע בהצלחה");
                            builder.setCancelable(false);
                            builder.setNegativeButton("סגור", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    goToHomeFragment();
                                }
                            });
                            builder.show();
                        }
                    }
                });
    }

    private void getUserFromRealtimeFirebase(String email) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        Query query = mDatabaseReference.orderByChild("user/email").equalTo(email);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        //Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        User crntUser = childSnapshot.child(Constants.USER_PROFILE_ACCOUNT_KEY).getValue(User.class);
                        listener.fillUserHeader(crntUser);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void goToHomeFragment() {
        // listener.goToHomeFragment(this);
        listener.replaceFragment(this, HomeFragment.class, 0);
    }

    private void signInClickEvent() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        if (isValidationEmailAndPassword(email, password)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            if (NetworkUtils.isNetworkAvailable(getContext()))
                //sign in user
                signInUser(email, password);
            else {
                Toast.makeText(getContext(), "check your internet connection", Toast.LENGTH_SHORT).show();
                binding.commentFromServer.setText("without internet. check your internet connection");
            }
        }
    }

    private boolean isValidationEmailAndPassword(String email, String password) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void goToSignUpFragment() {
        listener.replaceFragment(this, SignUpFragment.class, 5);
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

    private void showResetPasswordAlertDialog() {
        resetPasswordBuilder = new AlertDialog.Builder(getContext());
        resetPassDialog = resetPasswordBuilder.create();
        resetPassDialog.setView(getAlertDialogView(resetPassDialogBinding));
        resetPassDialog.setCancelable(false);
        resetPassDialogBinding.btnResetPassword.setOnClickListener(this);
        resetPassDialogBinding.btnBack.setOnClickListener(this);
        //resetPasswordBuilder.show();
        resetPassDialog.show();
    }

    private View getAlertDialogView(ResetPasswordCustomAlertDialogBinding alertDialogBinding) {
        if (alertView != null)
            ((ViewGroup) alertView.getParent()).removeView(alertView);
        alertView = alertDialogBinding.getRoot();
        return alertView;
    }

    //onClick into reserPassword alert options
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset_password:
                if (!isToCloseAlert) {
                    resetPassword();
                    break;
                } else { //if user success to reset we want just to close alert and stay in this fragment
                    resetAlertDialog();
                    resetPassDialog.dismiss();
                    //אנחנו רוצים להציג הודעה שאומרת הכנס את הסיסמה שהוחלפה דרך תיבת המייל
                    //todo here need to put: please enter your email and password that you reset by email
                }
                break;
            case R.id.btn_back:
                resetAlertDialog();
                resetPassDialog.dismiss();
                isToCloseAlert = false;
                break;
        }
    }

    private void resetAlertDialog() {
        resetPassDialogBinding.lblResetDescription.setText(getString(R.string.forgot_password_msg));
        resetPassDialogBinding.lblResetDescription.setTextColor(getResources().getColor(R.color.white_70));
        resetPassDialogBinding.email.setText("");
        resetPassDialogBinding.btnResetPassword.setText(getString(R.string.btn_reset_password));
    }

    private boolean resetPassword() {

        String email = resetPassDialogBinding.email.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Enter your registered email ", Toast.LENGTH_LONG).show();
            isReseetPass = false;
            return isReseetPass;
        }

        resetPassDialogBinding.progressBar.setVisibility(View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                            resetPassDialogBinding.lblResetDescription.setTextColor(getResources().getColor(R.color.red));
                            resetPassDialogBinding.lblResetDescription.setText("We have sent you instructions to reset your password!");
                            isReseetPass = true;
                            changeBtnAction(isReseetPass);
                        } else {
                            Toast.makeText(getActivity(), "Failed to send reset email!", Toast.LENGTH_LONG).show();
                            resetPassDialogBinding.lblResetDescription.setTextColor(getResources().getColor(R.color.red));
                            resetPassDialogBinding.lblResetDescription.setText("Failed to send reset email!");
                            isReseetPass = false;
                            changeBtnAction(isReseetPass);
                        }
                        resetPassDialogBinding.progressBar.setVisibility(View.GONE);
                    }
                });
        return isReseetPass;
    }

    private void changeBtnAction(boolean isReseetPass) {
        if (isReseetPass) {
            isToCloseAlert = true;
            resetPassDialogBinding.btnResetPassword.setText("סגור");
        } else
            isToCloseAlert = false;
    }

    public void setOnRegisterSuccessListener(OnRegisterSuccessListener listener) {
        this.listener = listener;
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
}