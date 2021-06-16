package com.fadfadah.app.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.fadfadah.app.BuildConfig;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.fragment.AboutAppFragment;
import com.fadfadah.app.fragment.ChatInboxFragment;
import com.fadfadah.app.fragment.CommentsFragment;
import com.fadfadah.app.fragment.NotifictionFragment;
import com.fadfadah.app.fragment.ProfileFragment;
import com.fadfadah.app.fragment.SearchCategoryFragment;
import com.fadfadah.app.fragment.SearchResultFragment;
import com.fadfadah.app.fragment.SettingFragment;
import com.fadfadah.app.fragment.StoriesFragment;
import com.fadfadah.app.fragment.WallFragment;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.UsersModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

public class HomeActivity extends AppCompatActivity implements FragmentSelectionListener {

    @BindView(R.id.tv_profile)
    TextView tvProfile;
    private SearchResultFragment searchResultFragment;
    private SearchCategoryFragment searchCategoryFragment;
    private ChatInboxFragment chatInboxFragment;
    private ProfileFragment profileFragment;
    private SettingFragment settingFragment;
    private  StoriesFragment storiesFragment;

    private  CircleImageView imageUser;
//    private WallFragment wallFragment;
        private NotifictionFragment notifictionFragment;

    private CommentsFragment commentsFragment;
    private AboutAppFragment aboutAppFragment;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer_layout;
    @BindView(R.id.nav_view)
    NavigationView nav_view;
    private DatabaseReference userRef;
    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();
    BottomNavigationView navigationView;
    private CircleImageView imageView;
            ImageView darwerIcon;
    private TextView tv_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);


navigationView=findViewById(R.id.bottom_navigation);
//        wallFragment = new WallFragment(this);
        notifictionFragment = new NotifictionFragment(this);
        searchResultFragment = new SearchResultFragment(this);
        searchCategoryFragment = new SearchCategoryFragment(this);
        chatInboxFragment = new ChatInboxFragment(this);
        profileFragment = new ProfileFragment();
        settingFragment = new SettingFragment();
        darwerIcon=findViewById(R.id.menu);
        darwerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawer_layout.isDrawerOpen(nav_view)){
                    drawer_layout.closeDrawers();
                }else{
                    drawer_layout.openDrawer(nav_view);
                }
            }
        });

        imageUser=findViewById(R.id.imageView_user);
        getImageUser();
        imageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!profileFragment.isAdded())
                    getSupportFragmentManager().beginTransaction().replace(R.id.home_container, profileFragment).commit();



            }
        });
        storiesFragment = new StoriesFragment(this);
        aboutAppFragment = new AboutAppFragment();
      //  home();
        createNavigationView();
        if (getIntent().hasExtra(Constant.CHAT_KEY) && getIntent().hasExtra(Constant.USER_KEY) &&
                !getIntent().getStringExtra(Constant.CHAT_KEY).trim().isEmpty() && !
                getIntent().getStringExtra(Constant.USER_KEY).trim().isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constant.USER_KEY, getIntent().getStringExtra(Constant.USER_KEY));
            intent.putExtra(Constant.CHAT_KEY, getIntent().getStringExtra(Constant.CHAT_KEY));
            startActivity(intent);
        }else   if (getIntent().hasExtra(Constant.POST_KEY) && getIntent().hasExtra(Constant.USER_KEY) &&
                !getIntent().getStringExtra(Constant.POST_KEY).trim().isEmpty() && !
                getIntent().getStringExtra(Constant.USER_KEY).trim().isEmpty()) {
            selectViewComments(getIntent().getStringExtra(Constant.POST_KEY), "" + FirebaseAuth.getInstance().getUid());
        }else   if ( getIntent().hasExtra(Constant.USER_KEY) &&
                !getIntent().getStringExtra(Constant.USER_KEY).trim().isEmpty())
        {
            Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constant.USER_KEY, getIntent().getStringExtra(Constant.USER_KEY));
            startActivity(intent);
        }


//
//        if (getIntent().hasExtra("type") && getIntent().hasExtra("id")) {
//
//            if (type.toLowerCase().contains("chat")) {
//                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra(Constant.USER_KEY, id);
//                intent.putExtra(Constant.CHAT_KEY, getIntent().getStringExtra("chat"));
//                startActivity(intent);
//            } else if (type.toLowerCase().contains("like_comment")) {
//                selectViewComments(id, "" + FirebaseAuth.getInstance().getUid());
//            } else if (type.toLowerCase().contains("like")) {
//                selectViewComments(id, "" + FirebaseAuth.getInstance().getUid());
//            } else if (type.toLowerCase().contains("comment")) {
//                selectViewComments(id, "" + FirebaseAuth.getInstance().getUid());
//            } else if (type.toLowerCase().contains("friend")) {
//                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.putExtra(AddFriendActivity.USER_KEY, id);
//                startActivity(intent);
//            }
//        }


    }

    private void getImageUser() {
        userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        assert usersModel.getImage() != null;
                        assert !usersModel.getImage().isEmpty();
                        try {
                            Glide.with(getApplicationContext()).load(usersModel.getImage())
                                    .placeholder(R.drawable.profile_img).into(imageUser);
                        } catch (Exception e) {
                            imageUser.setImageResource(R.drawable.profile_img);
                        }
                        try {

                        } catch (Exception e) {

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getUid() == null ||
                FirebaseAuth.getInstance().getCurrentUser() == null ) {

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {

            FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS)
                    .child("" + FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                        saveUser(usersModel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            try {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(53);
            } catch (Exception e) {
            }
        }

    }
    private void createNavigationView() {
        navigationView.performClick();

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_home:

                        if (!searchCategoryFragment.isAdded())
                            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, searchCategoryFragment).commit();

                            return true;



                    case R.id.story:

                        if (!storiesFragment.isAdded())
                            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, storiesFragment).commit();
                            return true;



                    case R.id.chat:

                        if (!chatInboxFragment.isAdded())
                            getSupportFragmentManager().beginTransaction().replace(R.id.home_container, chatInboxFragment).commit();

                            return true;

                    case R.id.notification:
                    if (!notifictionFragment.isAdded())
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_container, notifictionFragment).commit();
                    return true;


                }

return true;
            }



        });
        navigationView.setSelectedItemId(R.id.story);

    }

    private void hideAllFragments() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        if (wallFragment.isAdded())
//            transaction.hide(wallFragment);
        if (notifictionFragment.isAdded())
            transaction.hide(notifictionFragment);
        if (searchResultFragment.isAdded())
            transaction.hide(searchResultFragment);
        if (searchCategoryFragment.isAdded())
            transaction.hide(searchCategoryFragment);
        if (chatInboxFragment.isAdded())
            transaction.hide(chatInboxFragment);
        if (settingFragment.isAdded())
            transaction.hide(settingFragment);
        if (profileFragment.isAdded())
            transaction.hide(profileFragment);
        if (storiesFragment.isAdded())
            transaction.hide(storiesFragment);
        if (aboutAppFragment.isAdded())
            transaction.hide(aboutAppFragment);
        transaction.commit();
    }

    @Override
    public void selectFragment(SelectedFragment selection) {
        setOnline();
        Fragment fragment = null;
//        if (selection == SelectedFragment.HOME)
//            fragment = wallFragment;
//        else
            if (selection == SelectedFragment.NOTIFICATION) {
            fragment = notifictionFragment;
            notifictionFragment.onResume();
        } else if (selection == SelectedFragment.SEARCH) {
            fragment = searchResultFragment;
//            searchResultFragment.onResume();
        } else if (selection == SelectedFragment.SEARCH_DATE)
            fragment = searchCategoryFragment;
        else if (selection == SelectedFragment.CHAT)
            fragment = chatInboxFragment;
        else if (selection == SelectedFragment.PROFILE) {
            fragment = profileFragment;
            profileFragment.update();
        } else if (selection == SelectedFragment.SETTINGS)
            fragment = settingFragment;
        else if (selection == SelectedFragment.STORIES) {
            fragment = storiesFragment;
            storiesFragment.onResume();
        } else if (selection == SelectedFragment.ABOUT_APP) {
            fragment = aboutAppFragment;
        }
        if (fragment == null)
            return;
        hideAllFragments();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment.isAdded())
            transaction.show(fragment);
        else transaction.add(R.id.home_container, fragment, "");
        transaction.commit();
    }

    @Override
    public void selectViewComments(String postkey, String postOwnerKey) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_out_up, R.anim.slide_in_up);
        commentsFragment = new CommentsFragment(postkey, postOwnerKey);
        transaction.add(R.id.home_container, commentsFragment, "").addToBackStack("");
        transaction.commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideAllFragments();
            }
        }, 100);
    }


    @Override
    public void search(String year) {
        searchResultFragment.setDate(year, null, null);
        selectFragment(SelectedFragment.SEARCH);
    }

    @Override
    public void search() {
        searchResultFragment.setDate(null, null, null);
        selectFragment(SelectedFragment.SEARCH);
    }

    @Override
    public void openDrawer() {
        drawer_layout.openDrawer(nav_view);
    }

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(nav_view)) {
            drawer_layout.closeDrawers();
            return;
        }
//        if (commentsFragment != null && commentsFragment.isAdded()) {
//            selectFragment(SelectedFragment.HOME);
//            super.onBackPressed();
//            return;
//        }
        if (!searchCategoryFragment.isVisible()) {
            selectFragment(SelectedFragment.SEARCH_DATE);
            return;
        } else if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getResources().getString(R.string.double_back_to_exit), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    boolean doubleBackToExitPressedOnce = false;


  /*  @OnClick(R.id.tv_home)
    void home() {
        selectFragment(SelectedFragment.SEARCH_DATE);
        drawer_layout.closeDrawers();

    }*/

    @OnClick(R.id.tv_settings)
    void settings() {
        selectFragment(SelectedFragment.SETTINGS);
        drawer_layout.closeDrawers();
    }

    @OnClick(R.id.tv_about_app)
    void aboutApp() {
        selectFragment(SelectedFragment.ABOUT_APP);
        drawer_layout.closeDrawers();

    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @OnClick(R.id.tv_rate_app)
    void rateApp() {
        drawer_layout.closeDrawers();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID));
        startActivity(intent);
    }

    @OnClick(R.id.tv_share_app)
    void shareAp() {
        drawer_layout.closeDrawers();
        shareApp();
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            String shareMessage = getResources().getString(R.string.share_app_descr);
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_one)));
        } catch (Exception e) {
            //e.toString();
        }
    }

    @OnClick(R.id.tv_logout)
    void logout() {
        FirebaseAuth.getInstance().signOut();
        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }
        drawer_layout.closeDrawers();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    public void loadFragment(Fragment fr) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.home_container, fr);
        transaction.commit();
    }

    @OnClick(R.id.tv_profile)
    public void onViewClicked() {
        drawer_layout.closeDrawers();
        selectFragment(SelectedFragment.PROFILE);
    }

    private void setOnline() {
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child("lastOnlineTime")
                .setValue(getCurrentTimeMilliSecond());
    }

    private void saveUser(UsersModel model) {
        if (model == null)
            return;
        imageView = nav_view.getHeaderView(0).findViewById(R.id.imageView);
        tv_username = nav_view.getHeaderView(0).findViewById(R.id.tv_username);
        try {
            Glide.with(this).load(model.getImage()).placeholder(R.drawable.profile_img).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tv_username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.USER_KEY, "" + FirebaseAuth.getInstance().getUid());
                startActivity(intent);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constant.USER_KEY, "" + FirebaseAuth.getInstance().getUid());
                startActivity(intent);
            }
        });
        tv_username.setText(model.getFirstName());
        SharedPreferences.Editor sharedEdit = getSharedPreferences("user_data", MODE_PRIVATE).edit();
        sharedEdit.putString("getEmailAddress", model.getEmailAddress());
        sharedEdit.putString("getFirstName", model.getFirstName());
        sharedEdit.putString("getLastName", model.getLastName());
        sharedEdit.putString("getImage", model.getImage());
        sharedEdit.putString("getUserToken", model.getUserToken());
        sharedEdit.putInt("getDay", model.getDay());
        sharedEdit.putInt("getMonth", model.getMonth());
        sharedEdit.putInt("getYear", model.getYear());
        sharedEdit.putInt("getPrivacy", model.getPrivacy());
        sharedEdit.putInt("getSex", model.getSex());
        sharedEdit.apply();

    }
}