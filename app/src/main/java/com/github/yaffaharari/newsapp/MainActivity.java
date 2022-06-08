package com.github.yaffaharari.newsapp;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.yaffaharari.newsapp.databinding.ActivityMainBinding;
import com.github.yaffaharari.newsapp.databinding.NavHeaderMainBinding;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.OnRegisterSuccessListener;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.SignInFragment;
import com.github.yaffaharari.newsapp.firebase.authentication.localAuth.SignUpFragment;
import com.github.yaffaharari.newsapp.models.Constants;
import com.github.yaffaharari.newsapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnRegisterSuccessListener {

    //private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

  /*  private Toolbar toolbar;
    private NavigationView navigationView;*/

    private Fragment fragment = null;
    private Class fragmentClass;
    private FragmentManager fragmentManager;
    private String currentFragmentName;

    private ActivityMainBinding binding;
    private NavHeaderMainBinding headerMainBinding;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    //private HashMap<String, User> userListHashMap;
    private boolean isMainMenu = true;

    private User user;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener authListener;
    private IntentFilter intentFilter;
    private String deviceKey;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        View headerView = binding.navView.getHeaderView(0);
        headerMainBinding = NavHeaderMainBinding.bind(headerView);

        setToolbarTitle(getString(R.string.home_fragment_title));

        mToggle = new ActionBarDrawerToggle(this, binding.drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawer.addDrawerListener(mToggle);
        mToggle.syncState();

        setupDrawerContent(binding.navView);

        fragmentClass = HomeFragment.class;
        fillMainActivityWithFragment(fragmentClass);

        //listenToUserState();
        listenToBackStackChange();
        listenToRealtimeFirebase();

        binding.navView.getMenu().getItem(0).setChecked(true);

       // AccessibilityServiceInfo info = new AccessibilityServiceInfo();
       // info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    }

    private void listenToRealtimeFirebase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(Constants.androidId);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.child(Constants.USER_PROFILE_ACCOUNT_KEY).getValue(User.class);
                if (user != null) { //if user already exist in realtime database
                    isRegistered = true;
                    fillUserHeader(user);
                    signInUser(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void listenToBackStackChange() {
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                List<Fragment> f = fragmentManager.getFragments();
                currentFragmentName = f.get(f.size() - 1).getClass().getSimpleName();
            }
        });
    }

    private void setupDefaultUserHeader() {
        headerMainBinding.nickName.setText(getResources().getString(R.string.nav_header_title));
        headerMainBinding.email.setText(getResources().getString(R.string.nav_header_subtitle));
        headerMainBinding.imageProfile.setImageResource(R.drawable.sharp_account_circle_black_18dp);
        headerMainBinding.arrowDropDown.setVisibility(View.INVISIBLE);
    }

    private void setNavigationViewMenu(int iconImgResId, int menuResId) {
        //headerMainBinding.arrowDropDown.setImageResource(R.drawable.baseline_arrow_drop_up_black_18dp);
        headerMainBinding.arrowDropDown.setImageResource(iconImgResId);
        binding.navView.getMenu().clear();
        binding.navView.inflateMenu(menuResId);
        binding.navView.invalidate();
    }

    public void setToolbarTitle(String title) {
        binding.mainAppBar.toolbar.setTitle(title);
        setSupportActionBar(binding.mainAppBar.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void fillMainActivityWithFragment(Class fragmentClass) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        fragmentManager = getSupportFragmentManager();
        switch (fragmentClass.getSimpleName()) {
            case "UserProfileFragment":
                changeFragWithoutAddToBackStack(fragment);
                ((UserProfileFragment) fragment).setOnRegisterSuccessListener(this);
                break;
            case "SignInFragment": //added to backstack for when user click on backPress he will go to userProfileFragment
                changeFragWithAddToBackStack(fragment);
                ((SignInFragment) fragment).setOnRegisterSuccessListener(this);
                break;
            case "SignUpFragment":
                changeFragWithAddToBackStack(fragment);
                ((SignUpFragment) fragment).setOnRegisterSuccessListener(this);
                break;
            default:
                changeFragWithoutAddToBackStack(fragment);
                break;
        }
    }

    private void changeFragWithAddToBackStack(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment)
                .addToBackStack(SignInFragment.class.getSimpleName())
                .commit();
    }

    private void changeFragWithoutAddToBackStack(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment)
                .commit();
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //selectDrawerItem(item);
                if (isMainMenu) {
                    selectDrawerItem(item);
                } else {
                    selectSubmenuDrawerItem(item);
                }
                return false;
            }
        });
    }

    private void selectSubmenuDrawerItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_delete_account:
                mDatabaseReference = mFirebaseDatabase.getReference();
                Query query = mDatabaseReference.orderByChild("user/uid").equalTo(user.getUid());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        Set<String> keySet = map.keySet();
                        List<String> list = new ArrayList<String>(keySet);
                        deviceKey = list.get(0);
                        if (!deviceKey.equals(Constants.androidId)) //if someone try to delete accounts from other device where it created
                            Toast.makeText(MainActivity.this, "can't delete this account from this device. please try delete from device who created this accounts", Toast.LENGTH_LONG).show();
                        else
                            showAlertStatusAndResetHeaderAndNavMenu();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                break;
        }
    }

    //after delete account action
    private void showAlertStatusAndResetHeaderAndNavMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        builder.setMessage("האם אתה בטוח שברצונך למחוק את החשבון?");
        builder.setCancelable(false);
        builder.setNegativeButton("כן", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //userListHashMap.remove(user.getEmail());
                deleteAccountFromFirebaseAuth();
                isMainMenu = !isMainMenu;
                setNavigationViewMenu(R.drawable.baseline_arrow_drop_down_black_18dp, R.menu.activity_main_drawer);
                setupDefaultUserHeader();
                //todo to go HomeFragment (good when someone delete account not from HomeFragment.
            }
        });
        builder.setPositiveButton("לא", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        // Close the navigation drawer
        binding.drawer.closeDrawer(GravityCompat.START);
        // binding.navView.getMenu().getItem(0).setChecked(true);
    }

    private void deleteAccountFromFirebaseAuth() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null)
            signInUser(user); //if user uninstall app and install it again cause that who exists in firebase but not login in this pariod of life app
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), user.getPassword());
        firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mDatabaseReference.child(deviceKey).child(Constants.USER_PROFILE_ACCOUNT_KEY).removeValue();

                            //dataSnapshot.getRef().child(Constants.USER_PROFILE_ACCOUNT_KEY).removeValue();

                        }
                    }
                });
            }
        });
    }

    private void signInUser(User user) {
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful())
                            // there was an error
                            Log.i(MainActivity.class.getSimpleName(), "faild to connect and delete your account");
                        else
                            Log.i(MainActivity.class.getSimpleName(), "success to connect to your account");
                    }
                });
    }

    private void saveUseListInRealtimeFirebase(HashMap<String, User> userListHashMap) {
        @SuppressLint("HardwareIds")
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(androidId);
        String json = new Gson().toJson(userListHashMap);
        mDatabaseReference.child(Constants.USER_PROFILE_ACCOUNT_KEY).setValue(json);
    }

    private void selectDrawerItem(MenuItem item) {
        fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_home_page:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.nav_favorite:
                fragmentClass = FavoriteFragment.class;
                break;
            case R.id.nav_user_profile:
                fragmentClass = UserProfileFragment.class;
                break;
            case R.id.nav_setting:
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                break;
            case R.id.nav_private_policy:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://yaffah19.wixsite.com/newsapp/about"));
                startActivity(intent);
                break;
            default:
                fragmentClass = HomeFragment.class;
        }
        currentFragmentName = fragmentClass.getSimpleName();
        fillMainActivityWithFragment(fragmentClass);

        // Highlight the selected item has been done by NavigationView
        // Set action bar title
        //toolbar.setTitle(item.getTitle());
        setToolbarTitle(String.valueOf(item.getTitle()));
        // Close the navigation drawer
        binding.drawer.closeDrawer(GravityCompat.START);
        binding.navView.setCheckedItem(item.getItemId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switch (currentFragmentName) {
            case "SignUpFragment":
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentClass = UserProfileFragment.class;
                currentFragmentName = fragmentClass.getSimpleName();
                binding.navView.getMenu().getItem(2).setChecked(true);
                fillMainActivityWithFragment(fragmentClass);
                break;
            case "SignInFragment":
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentClass = UserProfileFragment.class;
                currentFragmentName = fragmentClass.getSimpleName();
                binding.navView.getMenu().getItem(2).setChecked(true);
                fillMainActivityWithFragment(fragmentClass);
                break;
            case "HomeFragment":
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                super.onBackPressed();
                break;
            default:
                fragmentClass = HomeFragment.class;
                currentFragmentName = fragmentClass.getSimpleName();
                binding.navView.getMenu().getItem(0).setChecked(true);
                fillMainActivityWithFragment(fragmentClass);
                break;
        }
    }

    @Override
    public void fillUserHeader(User user) {
        // user = userListHashMap.get(email);  //if we here from SignInFragment. if we here from SignUpfragment it is possible to run setUpUserHeader method also but is same.
        this.user = user;
        headerMainBinding.nickName.setText(user.getNickName());
        headerMainBinding.email.setText(user.getEmail());
        //headerMainBinding.imageProfile.setImageURI(Uri.parse(user.getImageUri()));
        Bitmap imgBitmap =  decodeFromFirebaseBase64(user.getImageUri());
        Glide.with(this)
                .load(imgBitmap)
                .apply(RequestOptions.circleCropTransform())
                .into(headerMainBinding.imageProfile);
        headerMainBinding.arrowDropDown.setVisibility(View.VISIBLE);
        headerMainBinding.arrowDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isMainMenu = !isMainMenu;
                if (isMainMenu) {
                    setupDrawerContent(binding.navView);
                    setNavigationViewMenu(R.drawable.baseline_arrow_drop_down_black_18dp, R.menu.activity_main_drawer);
                } else {
                    setupDrawerContent(binding.navView);
                    setNavigationViewMenu(R.drawable.baseline_arrow_drop_up_black_18dp, R.menu.account_submenu);
                }
            }
        });
    }

    public static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    @Override
    public void replaceFragment(Fragment currentFrag, Class replaceTo, int pointInNavMenu) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentClass = replaceTo;
        currentFragmentName = fragmentClass.getSimpleName();
        binding.navView.getMenu().getItem(pointInNavMenu).setChecked(true);
        fillMainActivityWithFragment(fragmentClass);
    }

    @Override
    public boolean isRegistered() {
        return isRegistered;
    }
}
