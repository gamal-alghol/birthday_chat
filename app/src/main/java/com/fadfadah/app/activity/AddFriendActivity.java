package com.fadfadah.app.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.adapter.WallsAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.fragment.CommentsFragment;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.NotificationHelper;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.ChatInboxModel;
import com.fadfadah.app.models.PostsModel;
import com.fadfadah.app.models.UsersModel;
import com.fadfadah.app.retrofit.MessageResponse;
import com.fadfadah.app.retrofit.RetrofitClient;
import com.fadfadah.app.retrofit.RetrofitServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

public class AddFriendActivity extends AppCompatActivity implements FragmentSelectionListener {


    private String userKey = null;
    @BindView(R.id.frame_container)
    FrameLayout frame_container;
    @BindView(R.id.background_imageView)
    ImageView background_imageView;
    @BindView(R.id.civ_profile_img)
    CircleImageView civ_profile_img;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.iv_chat)
    ImageView iv_chat;
    @BindView(R.id.tv_add_friend)
    TextView tv_add_friend;
    @BindView(R.id.tv_remove_friend)
    TextView tv_remove_friend;

    private DatabaseReference friendRef = null;
    private DatabaseReference inboxRef;
    private String userToken = "";
    @BindView(R.id.recyclerView_walls)
    RecyclerView recyclerView_walls;
    private WallsAdapter adapter;
    @BindView(R.id.tv_country)
    TextView tvcountry;
    @BindView(R.id.tv_birthday)
    TextView tv_birthday;
    @BindView(R.id.tv_gender)
    TextView tv_gender;
    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();
    private ArrayList<PostsModel> postsList = new ArrayList<>();
    private String userImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.activity_add_friend);
        ButterKnife.bind(this);
        userKey = getIntent().getStringExtra(Constant.USER_KEY);


//        recycerView_walls.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        recyclerView_walls.setHasFixedSize(true);
//        recyclerView_walls.setItemViewCacheSize(20);l


        loadUser(civ_profile_img, tv_name, userKey);
        checkFriend();
    }

//    private void getPosts(int friend) {
//        DatabaseReference postReference = FirebaseDatabase.getInstance()
//                .getReference(DataBaseTablesConstants.POSTS);
//        Query query = postReference.orderByChild("userKey").equalTo("" + userKey);
//        query.limitToLast(60).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                try {
//                    postsList.clear();
//                    for (DataSnapshot snap :
//                            dataSnapshot.getChildren()) {
//                        PostsModel postsModel = snap.getValue(PostsModel.class);
//                        if (friend != 0 || postsModel.getPostPrivacy() == 1)
//                            postsList.add(0, postsModel);
//                    }
//
//                    adapter = new WallsAdapter(AddFriendActivity.this, postsList, AddFriendActivity.this);
//                    recyclerView_walls.setAdapter(adapter);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//    }

    private void checkFriend() {
        if (userKey.equals("" + FirebaseAuth.getInstance().getUid())) {
            tv_add_friend.setVisibility(View.INVISIBLE);
            tv_remove_friend.setVisibility(View.INVISIBLE);
            iv_chat.setVisibility(View.INVISIBLE);
//            getPosts(1);
            return;
        }
        if (friendRef == null)
            friendRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS);
        friendRef.child("" + FirebaseAuth.getInstance().getUid()).child(userKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            tv_add_friend.setVisibility(View.INVISIBLE);
                            tv_remove_friend.setVisibility(View.VISIBLE);
//                            getPosts(1);
                            tv_add_friend.setTag("1");
                        } else {
//                            getPosts(0);
                            checkRequest();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, String userKey) {
        if (userKey == null)
            return;
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        assert usersModel.getImage() != null;
                        assert !usersModel.getImage().isEmpty();
                        try {
                            Glide.with(getApplicationContext()).load(usersModel.getImage())
                                    .placeholder(R.drawable.profile_img).into(civ_profile_img);
                        } catch (Exception e) {
                            civ_profile_img.setImageResource(R.drawable.profile_img);
                        }
                        tv_gender.setText(usersModel.getSex() == 0 ? getResources().getString(R.string.male) : getResources().getString(R.string.female));
                        try {
                            Glide.with(getApplicationContext()).load(usersModel.getCover())
                                    .into(background_imageView);
                        } catch (Exception e) {

                        }
                        userImage = usersModel.getImage();
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                        tv_birthday.setText(usersModel.getDay() + " / " + usersModel.getMonth() + " / " + usersModel.getYear());

                        tvcountry.setText(usersModel.getCountry() + " " + usersModel.getCity());
                        userToken = usersModel.getUserToken();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        checkFriend();

    }

    private void checkRequest() {
        if (addFriendRef == null)
            addFriendRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS_REQUEST);
        addFriendRef.child(userKey).child(String.valueOf(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tv_add_friend.setTag("1");
                    tv_add_friend.setText(getResources().getString(R.string.cancel_request));
                } else {
                    addFriendRef.child(String.valueOf(FirebaseAuth.getInstance().getUid()))
                            .child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                tv_remove_friend.setVisibility(View.GONE);
                                tv_add_friend.setVisibility(View.VISIBLE);
                                tv_add_friend.setTag("2");
                                tv_add_friend.setText(getResources().getString(R.string.confirm));
                            } else {
                                tv_add_friend.setTag("0");
                                tv_remove_friend.setVisibility(View.GONE);
                                tv_add_friend.setVisibility(View.VISIBLE);
                                tv_add_friend.setText(getResources().getString(R.string.add_friend));

                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

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
        try {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(52);
        } catch (Exception e) {
        }
    }

    DatabaseReference userRef = null;
    DatabaseReference addFriendRef = null;

    @OnClick(R.id.tv_add_friend)
    void addFriendRequest() {
        if (addFriendRef == null)
            addFriendRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS_REQUEST);
        if (tv_add_friend.getTag() == null || tv_add_friend.getTag().equals("0"))
            addFriendRef.child(userKey).child(String.valueOf("" +
                    FirebaseAuth.getInstance().getUid())).setValue(getCurrentTimeMilliSecond()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
//                    sendNotification(userToken, " sent a friend request ",
//                            SelectedFragment.FRIEND.name(), "" + FirebaseAuth.getInstance().getUid());

                    //Context mContext, String token, String body, String CLICK_ACTION
                    //    ,String postKey,String  chatKey,String userKey
                    NotificationHelper.sendNotification(getApplicationContext(), userToken,
                            getResources().getString(R.string.sent_friend_request), Constant.FRIEND_ACTION,
                            "", "", "" + FirebaseAuth.getInstance().getUid());

                    checkFriend();
                }
            });
        else if (tv_add_friend.getTag().equals("1")) {
            addFriendRef.child(userKey).child(String.valueOf("" + FirebaseAuth.getInstance().getUid()))
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    checkFriend();
                }
            });
        } else if (tv_add_friend.getTag() == null || tv_add_friend.getTag().equals("2")) {
            acceptFriendRequest(userKey, userToken);
        }
    }

//    private void sendNotification(String token, String body, String type, String userId) {
//        SharedPreferences sharedEdit = getSharedPreferences("user_data", Context.MODE_PRIVATE);
//
//
//        JsonObject map = new JsonObject();
//        try {
//            map.addProperty("to", token);
//            map.addProperty("priority", "high");
//            JsonObject notification = new JsonObject();
//            notification.addProperty("title", sharedEdit.getString("getFirstName", "someone"));
//            notification.addProperty("body", body);
//            notification.addProperty("click_action","FRIEND");
//            JsonObject data = new JsonObject();
//            data.addProperty("user key", userId);
//            map.add("notification", notification);
//            map.add("data", data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        RetrofitClient.inialize().create(RetrofitServices.class).sendNotification(map).enqueue(new Callback<MessageResponse>() {
//            @Override
//            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
//                Log.d("sendNotification", "onResponse: " + response.body().success);
//            }
//
//            @Override
//            public void onFailure(Call<MessageResponse> call, Throwable t) {
//                Log.d("sendNotification", "onFailure: " + t.getMessage());
//            }
//        });
//    }

    private void acceptFriendRequest(String toKey, String token) {
        DatabaseReference addFriendRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.FRIENDS);
        addFriendRef.child(toKey).child(String.valueOf(FirebaseAuth.getInstance().getUid())).setValue(getCurrentTimeMilliSecond());
        addFriendRef.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child(toKey).setValue(getCurrentTimeMilliSecond());
        cancelFriendRequest(toKey);
        //String token, String body, String type,String userId
//        sendNotification(token, getResources().getString(R.string.accept_your_request),
//                SelectedFragment.FRIEND.name(), "" + FirebaseAuth.getInstance().getUid());
        ///send here
        //Context mContext, String token, String body, String CLICK_ACTION
        //    ,String postKey,String  chatKey,String userKey
        NotificationHelper.sendNotification(getApplicationContext(), token,
                getResources().getString(R.string.accept_your_request), Constant.FRIEND_ACTION,
                "", "", "" + FirebaseAuth.getInstance().getUid());
    }

    private void cancelFriendRequest(String key) {
        DatabaseReference addFriendRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.FRIENDS_REQUEST);
        addFriendRef.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child(key).removeValue();
    }

    @OnClick(R.id.tv_remove_friend)
    void removeFriendRequest() {
        if (friendRef == null)
            friendRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS);

        friendRef.child(String.valueOf("" + FirebaseAuth.getInstance().getUid())).child(userKey)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                friendRef.child(userKey).child(String.valueOf("" + FirebaseAuth.getInstance().getUid()))
                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        checkFriend();
                    }
                });
            }
        });


    }

    @OnClick(R.id.iv_chat)
    public void onViewClicked() {
        createInbox(userKey);
    }


    private void createInbox(String key) {
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
        inboxRef.child("" + FirebaseAuth.getInstance().getUid()).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        ChatInboxModel model = dataSnapshot.getValue(ChatInboxModel.class);
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constant.USER_KEY, key);
                        intent.putExtra(ChatActivity.USER_NAME, tv_name.getText().toString());
                        intent.putExtra(Constant.CHAT_KEY, model.getChatId());
                        intent.putExtra(ChatActivity.USER_IMAGE, "" + userImage);
                        intent.putExtra(ChatActivity.USER_TOKEN, "" + userToken);
                        startActivity(intent);

                    } else {
                        //String chatId, String fromId, String lastMessage, int status, long timeStamp
                        String chatKey = inboxRef.push().getKey();
                        ChatInboxModel model1 = new ChatInboxModel(chatKey, "" + FirebaseAuth.getInstance().getUid(),
                                getResources().getString(R.string.waving_to_you), 0, getCurrentTimeMilliSecond());

                        ChatInboxModel model2 = new ChatInboxModel(chatKey,
                                key,
                                getResources().getString(R.string.waving_to_you), 0, getCurrentTimeMilliSecond());

                        inboxRef.child("" + FirebaseAuth.getInstance().getUid()).child(key).setValue(model2);
                        inboxRef.child(key).child("" + FirebaseAuth.getInstance().getUid()).setValue(model1);


                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constant.USER_KEY, key);
                        intent.putExtra(ChatActivity.USER_NAME, tv_name.getText().toString());
                        intent.putExtra(Constant.CHAT_KEY, model1.getChatId());
                        intent.putExtra(ChatActivity.USER_IMAGE, "" + model1.userImage);
                        intent.putExtra(ChatActivity.USER_TOKEN, "" + model1.userToken);
                        startActivity(intent);

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
    public void selectFragment(SelectedFragment fragment) {

    }

    @Override
    public void selectViewComments(String postkey, String postOwnerKey) {
        frame_container.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new CommentsFragment(postkey, postOwnerKey)).commit();

    }

    @Override
    public void onBackPressed() {
        if (frame_container.getVisibility() == View.VISIBLE)
            frame_container.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }

    @Override
    public void search(String year) {

    }

    @Override
    public void search() {

    }

    @Override
    public void openDrawer() {

    }
}