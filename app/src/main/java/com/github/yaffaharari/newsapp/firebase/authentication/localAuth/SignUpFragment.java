package com.github.yaffaharari.newsapp.firebase.authentication.localAuth;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.yaffaharari.newsapp.BuildConfig;
import com.github.yaffaharari.newsapp.HomeFragment;
import com.github.yaffaharari.newsapp.R;
import com.github.yaffaharari.newsapp.databinding.FragmentSignUpBinding;
import com.github.yaffaharari.newsapp.databinding.ResetPasswordCustomAlertDialogBinding;
import com.github.yaffaharari.newsapp.databinding.UploadImageAlertBinding;
import com.github.yaffaharari.newsapp.models.Constants;
import com.github.yaffaharari.newsapp.models.User;
import com.github.yaffaharari.newsapp.utils.NetworkUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SignUpFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private FragmentSignUpBinding binding;
    private ResetPasswordCustomAlertDialogBinding resetPassDialogBinding;
    private UploadImageAlertBinding uploadImageAlertBinding;

    private View resetPassAlertView;
    private View chooseImgAlertView;

    private FirebaseAuth auth;

    private AlertDialog.Builder resetPasswordBuilder;
    private AlertDialog resetPassDialog;

    private AlertDialog.Builder chooseImgBuilder;
    private AlertDialog chooseImgDialog;

    private OnRegisterSuccessListener listener;
    private boolean isResetPass;
    private boolean isToCloseAlert;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    //private Uri picUri;
    private Uri picUri;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragment.
     */
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false);
        resetPassDialogBinding = DataBindingUtil.inflate(inflater, R.layout.reset_password_custom_alert_dialog, container, false);
        uploadImageAlertBinding = DataBindingUtil.inflate(inflater, R.layout.upload_image_alert, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // alertCustomizedLayout();
        binding.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageFromCameraOrGalleryAlert();

            }
        });
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpClickEvent();
            }
        });
        binding.btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetPasswordAlertDialog();
            }
        });
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignInFrag();
            }
        });
    }

    private void chooseImageFromCameraOrGalleryAlert() {
        chooseImgBuilder = new AlertDialog.Builder(getContext());
        //builder.setView()
        chooseImgDialog = chooseImgBuilder.create();
        chooseImgDialog.setView(getUploadImgAlertDialogView(uploadImageAlertBinding));
        chooseImgDialog.setCancelable(true);
        uploadImageAlertBinding.openGallery.setOnClickListener(this);
        uploadImageAlertBinding.openCamera.setOnClickListener(this);
        //resetPasswordBuilder.show();
        chooseImgDialog.show();
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

    private void signUpClickEvent() {
        String nickName = binding.nickNameEd.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        Drawable d = this.getResources().getDrawable(R.drawable.sharp_account_circle_black_18dp);
        Bitmap bitmap = ((BitmapDrawable)d).getBitmap();

        if (isValidationEmailAndPassword(nickName, email, password)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            if (NetworkUtils.isNetworkAvailable(getContext()))
                if (!listener.isRegistered()) //it is possible to create only 1 accounts per device. if already exsits user per this device in realtime firebase
                    createUser(nickName, email, password, bitmap);
                else {
                    binding.commentFromServer.setText("you already have registered from this device");
                    binding.progressBar.setVisibility(View.GONE);
                }
            else {
                Toast.makeText(getContext(), "check your internet connection", Toast.LENGTH_SHORT).show();
                binding.commentFromServer.setText("without internet. check your internet connection");
            }
        }
    }

    private void createUser(final String nickName, final String email, String password, Bitmap bitmap) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        binding.progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            }
                            // if user enters wrong email.
                            catch (FirebaseAuthWeakPasswordException weakPassword) {
                                binding.commentFromServer.setText(weakPassword.getReason());
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                binding.commentFromServer.setText(malformedEmail.getMessage());
                            }
                            //if email already exist
                            catch (FirebaseAuthUserCollisionException existEmail) {
                                binding.commentFromServer.setText(existEmail.getMessage());
                                binding.btnResetPassword.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Log.d("UserProfilefragment", "onComplete: " + e.getMessage());
                            }
                        } else {
                            // binding.commentFromServer.setText("you have been success to register");
                            //listener.fillUserHeader(new User(nickName, "Yaffa harari", email));
                            String uid = task.getResult().getUser().getUid();
                            encodeImage(bitmap);
                            User user = new User(uid, nickName, email, password, picUri.toString());
                            //listener.addUserToUserProfileList(user);
                            //HashMap<String, User> userList = listener.getUserListHashMap();
                            saveUserProfileInRealTimeFirebase(user);
                            //listener.fillUserHeader(email);
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

    //we want to save userlist in realtime database because when the user uninstall this app or doing clean data no data have in sharedPreference
    //when he will be install this app again he will do signIn and it will be ok but we don't have any data to display in header from sharedPreference
    private void saveUserProfileInRealTimeFirebase(User user) {
        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(androidId);
        //String json = new Gson().toJson(userList);
        mDatabaseReference.child(Constants.USER_PROFILE_ACCOUNT_KEY).setValue(user);
    }

    private void goToHomeFragment() {
        // listener.goToHomeFragment(this);
        listener.replaceFragment(this, HomeFragment.class, 0);
    }

    private void goToSignInFrag() {
        listener.replaceFragment(this, SignInFragment.class, 5);
    }

    private boolean isValidationEmailAndPassword(String nickName, String email, String password) {

        if (TextUtils.isEmpty(nickName)) {
            // Toast.makeText(getContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            binding.commentFromServer.setText("It is must to fill nick name");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            // Toast.makeText(getContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            binding.commentFromServer.setText("It is must to fill Email");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            //Toast.makeText(getContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            binding.commentFromServer.setText("It is must to fill password");
            return false;
        }

        if (password.length() < 6) {
            //Toast.makeText(getContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            binding.commentFromServer.setText("Password too short, enter minimum 6 characters!");
            return false;
        }
        return true;
    }

    //onClick into reserPassword alert options
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reset_password:
                if (!isToCloseAlert) {
                    resetPassword();
                    break;
                } else { //if user success to reset we want take him to sign in fragment
                    resetAlertDialog();
                    resetPassDialog.dismiss();
                    getFragmentManager().popBackStack();
                    goToSignInFrag();
                    //  אנחנו רוצי םבנקודה זו לסמן לsignInFragment שהמשתמש אחרי איפוס ויש להציג הודעה מתאימה
                    //todo: here need to put something who say to signInFragment that the user after reset password and display appropriate notice
                }
                break;
            case R.id.btn_back:
                resetAlertDialog();
                resetPassDialog.dismiss();
                isToCloseAlert = false;
                break;
            case R.id.open_gallery:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, Constants.PICK_IMAGE_GALLERY);
                chooseImgDialog.dismiss();
                break;
            case R.id.open_camera:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File pathDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String fileName = "userProfile.jpg";
                File mediaFile = new File(pathDir, fileName);
                picUri = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider", mediaFile);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, picUri); // set the image file
                startActivityForResult(takePicture, Constants.PICK_IMAGE_CAMERA);
                chooseImgDialog.dismiss();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case Constants.PICK_IMAGE_CAMERA: // from open camera
                if (resultCode == FragmentActivity.RESULT_OK) {
                    if (picUri != null) {
                        uploadProfileImage(picUri);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), picUri);
                            encodeImage(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case Constants.PICK_IMAGE_GALLERY: //from open gallery
                if (resultCode == FragmentActivity.RESULT_OK) {
                    picUri = imageReturnedIntent.getData();
                    uploadProfileImage(picUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), picUri);
                        encodeImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int maxSize = 300;
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
         Bitmap.createScaledBitmap(bitmap,
                width,
                height,
                true);
       // bitmap.createScaledBitmap(bitmap, bitmap.getWidth() - 5)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        picUri = Uri.parse(imageEncoded);
    /*    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference uploadeRef = storageRef.child(cloudFilePath);*/

    }

    private void uploadProfileImage(Uri imgUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imgUri);
            Glide.with(getActivity())
                    .load(bitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImg);
            //binding.profileImg.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnRegisterSuccessListener(OnRegisterSuccessListener listener) {
        this.listener = listener;
    }

    private void showResetPasswordAlertDialog() {
        resetPasswordBuilder = new AlertDialog.Builder(getContext());
        resetPassDialog = resetPasswordBuilder.create();
        resetPassDialog.setView(getResetPassAlertDialogView(resetPassDialogBinding));
        resetPassDialog.setCancelable(false);
        resetPassDialogBinding.btnResetPassword.setOnClickListener(this);
        resetPassDialogBinding.btnBack.setOnClickListener(this);
        //resetPasswordBuilder.show();
        resetPassDialog.show();
    }

    private boolean resetPassword() {

        String email = resetPassDialogBinding.email.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Enter your registered email ", Toast.LENGTH_LONG).show();
            isResetPass = false;
            return isResetPass;
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
                            isResetPass = true;
                            changeBtnAction(isResetPass);
                        } else {
                            Toast.makeText(getActivity(), "Failed to send reset email!", Toast.LENGTH_LONG).show();
                            resetPassDialogBinding.lblResetDescription.setTextColor(getResources().getColor(R.color.red));
                            resetPassDialogBinding.lblResetDescription.setText("Failed to send reset email!");
                            isResetPass = false;
                            changeBtnAction(isResetPass);
                        }
                        resetPassDialogBinding.progressBar.setVisibility(View.GONE);
                    }
                });
        return isResetPass;
    }

    private void changeBtnAction(boolean isReseetPass) {
        if (isReseetPass) {
            isToCloseAlert = true;
            resetPassDialogBinding.btnResetPassword.setText("סגור");
        } else
            isToCloseAlert = false;
    }

    private void resetAlertDialog() {
        resetPassDialogBinding.lblResetDescription.setText(getString(R.string.forgot_password_msg));
        resetPassDialogBinding.lblResetDescription.setTextColor(getResources().getColor(R.color.white_70));
        resetPassDialogBinding.email.setText("");
        resetPassDialogBinding.btnResetPassword.setText(getString(R.string.btn_reset_password));
    }

    private View getResetPassAlertDialogView(ResetPasswordCustomAlertDialogBinding alertDialogBinding) {
        if (resetPassAlertView != null)
            ((ViewGroup) resetPassAlertView.getParent()).removeView(resetPassAlertView);
        resetPassAlertView = alertDialogBinding.getRoot();
        return resetPassAlertView;
    }

    private View getUploadImgAlertDialogView(UploadImageAlertBinding uploadImageAlertBinding) {
        if (chooseImgAlertView != null)
            ((ViewGroup) chooseImgAlertView.getParent()).removeView(chooseImgAlertView);
        chooseImgAlertView = uploadImageAlertBinding.getRoot();
        return chooseImgAlertView;
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
