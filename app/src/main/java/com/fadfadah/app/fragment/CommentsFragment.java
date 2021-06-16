package com.fadfadah.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.CommentsAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.NotificationType;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.NotificationHelper;
import com.fadfadah.app.models.CommentsModel;
import com.fadfadah.app.models.NotificationsModel;
import com.fadfadah.app.models.UsersModel;
import com.fadfadah.app.retrofit.MessageResponse;
import com.fadfadah.app.retrofit.RetrofitClient;
import com.fadfadah.app.retrofit.RetrofitServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;


public class CommentsFragment extends Fragment {

    @BindView(R.id.tv_comments)
    TextView tv_comments;
    @BindView(R.id.tv_likes)
    TextView tv_likes;
    @BindView(R.id.recycler_comments)
    RecyclerView recycler_comments;
    private Context mContext;
    @BindView(R.id.edt_comment)
    EditText edt_comment;
    private String postKey;
    private DatabaseReference postReference;
    private CommentsAdapter commentsAdapter;
    private ArrayList<CommentsModel> commentList = new ArrayList<>();
    private DatabaseReference notificationReference;
    private String postOwnerKey;
    private DatabaseReference userRef;
    private String name, token;
    private String myKey = "";
    @BindView(R.id.iv_send)
    ImageView iv_send;
    private long likes = 0;

    public CommentsFragment(String postKey, String postOwnerKey) {
        this.postKey = postKey;
        this.postOwnerKey = postOwnerKey;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    @OnClick(R.id.tv_likes)
    void addOrRemoveLike() {


        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        if (tv_likes.getTag() == null || tv_likes.getTag() == "0") {
            postReference.child(postKey).child(DataBaseTablesConstants.LIKES).child(String.valueOf(myKey)).setValue("1")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            getNumberOfLikes(tv_likes, postKey);
                            sendNotification(postKey, postOwnerKey,
                                    NotificationType.LIKE, "");

                        }
                    });
        } else {
            postReference.child(postKey).child(DataBaseTablesConstants.LIKES).child(String.valueOf(myKey)).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            getNumberOfLikes(tv_likes, postKey);
                        }
                    });
        }
    }

    private void loadUser(String userKey) {
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

                        name = (usersModel.getFirstName() + " " + usersModel.getLastName());
                        token = usersModel.getUserToken();

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler_comments.setLayoutManager(new LinearLayoutManager(mContext));
        recycler_comments.setHasFixedSize(true);
        recycler_comments.setItemViewCacheSize(20);
        commentsAdapter = new CommentsAdapter(mContext, commentList, postKey);
        recycler_comments.setAdapter(commentsAdapter);
        loadUser(postOwnerKey);
        edt_comment.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    {
                        if (edt_comment.getText().toString().trim().length() > 0) {
                            addComment(edt_comment.getText().toString());
                            edt_comment.setText("");
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        getComments();

        getNumberOfLikes(tv_likes, postKey);
    }

    @OnClick(R.id.iv_send)
    void send() {
        if (edt_comment.getText().toString().trim().length() > 0) {
            addComment(edt_comment.getText().toString());
            edt_comment.setText("");
        }
    }

    private void addComment(String commnt) {
        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        String key = postReference.push().getKey();
        //String commentKey, String comment, long commentTimeInMillis, String commentedByUserKey
        CommentsModel commentsModel = new CommentsModel(key, commnt, getCurrentTimeMilliSecond(),
                String.valueOf(myKey));
        postReference.child(postKey).child(DataBaseTablesConstants.COMMENTS).child(key).setValue(commentsModel).
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sendNotification(postKey, postOwnerKey, NotificationType.COMMENT, commnt);
                    }
                });
    }

    private void sendNotification(String postKey, String userKey, NotificationType notificationType, String comment) {
        if (userKey.equals(myKey))
            return;
        if (notificationReference == null)
            notificationReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.NOTIFICATION);
        String key = notificationReference.push().getKey();

        NotificationsModel model = new NotificationsModel(key, 0,
                String.valueOf(myKey)
                , postKey, notificationType, getCurrentTimeMilliSecond());
///if my key don't send
        notificationReference.child(userKey).child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ///send here
//                sendNotification(token, comment, notificationType.name());
                NotificationHelper.sendNotification(mContext,token,
                        "commented : "+comment, Constant.NOTIFICATION_ACTION,
                        postKey,"", myKey);
            }
        });

    }

//    private void sendNotification(String token, String body, String type) {
//        SharedPreferences sharedEdit = mContext.getSharedPreferences("user_data", Context.MODE_PRIVATE);
//        JsonObject map = new JsonObject();
//        try {
//            map.addProperty("to", token);
//            map.addProperty("priority", "high");
//            JsonObject notification = new JsonObject();
//            JsonObject data = new JsonObject();
//            data.addProperty("title", sharedEdit.getString("getFirstName", "someone"));
//            data.addProperty("body", body);
//            data.addProperty("type", type);
//            data.addProperty("id", postKey);
//            map.add("notification", notification);
//            map.add("data", data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    private void getComments() {
        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postReference.child(postKey).child(DataBaseTablesConstants.COMMENTS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    CommentsModel commentsModel = dataSnapshot.getValue(CommentsModel.class);
                    commentList.add(0, commentsModel);
                    commentsAdapter.notifyItemInserted(0);
                    tv_comments.setText(String.valueOf(commentList.size()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent("com.fadfadah.app.ACTION_UPDATE_LIKES_COMMENTS");
        intent.putExtra("post",postKey);
        mContext.sendBroadcast(intent);
    }


    private void getNumberOfLikes(TextView textView, String postKey) {
        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postReference.child(postKey).child(DataBaseTablesConstants.LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    likes = dataSnapshot.getChildrenCount();
                    textView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    if (dataSnapshot.child(String.valueOf(myKey)).exists()) {
                        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_fill_red_24,
                                0, 0, 0);
                        textView.setTag("1");
                    } else {
                        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_red_24,
                                0, 0, 0);
                        textView.setTag("0");
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

}