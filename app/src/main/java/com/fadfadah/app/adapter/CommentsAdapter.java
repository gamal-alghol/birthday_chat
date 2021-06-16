package com.fadfadah.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.NotificationType;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.Helper;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.ponnamkarthik.richlinkpreview.RichLinkViewTelegram;
import io.github.ponnamkarthik.richlinkpreview.ViewListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {
    DatabaseReference notificationReference = null;
    DatabaseReference userRef = null;
    private Context mContext;
    private ArrayList<CommentsModel> commentsList;
    String postKey;
    private DatabaseReference postRef;
    private String myKey;

    public CommentsAdapter(Context mContext,
                           ArrayList<CommentsModel> commentsList, String postKey) {
        this.mContext = mContext;
        this.postKey = postKey;
        this.commentsList = commentsList;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comments, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        loadUser(holder.civ_profile_img, holder.tv_username, holder.view_active, commentsList.get(position)
                .getCommentedByUserKey(), position);

        checkLike(postKey, commentsList.get(position).getCommentKey(), holder.iv_like);

        holder.tv_time.setText(Helper.calcTime(commentsList.get(position).getCommentTimeInMillis(), mContext));

        holder.tv_comment.setText(commentsList.get(position).getComment());


        try {   String[] link = extractLinks(commentsList.get(position).getComment());

            if (link != null && link.length > 0) {
                holder.richLinkView.setLink(link[0].contains("https://")?link[0]
                        :"https://"+link[0], new ViewListener() {

                    @Override
                    public void onSuccess(boolean status) {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }else holder.richLinkView.removeAllViews();
        }catch (Exception e){

        }
        holder.iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.iv_like.getTag() == null || holder.iv_like.getTag().equals("0"))
                    addLike(postKey, commentsList.get(position).getCommentKey(), holder.iv_like,position);
                else
                    removeLike(postKey, commentsList.get(position).getCommentKey(), holder.iv_like);
            }
        });
    }

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
//            Log.d(TAG, "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
    }

    private void addLike(String postKey, String commentKey, TextView tvlike,int position) {
        if (postRef == null)
            postRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postRef.child(postKey).child(DataBaseTablesConstants.COMMENTS).child(commentKey)
                .child(DataBaseTablesConstants.LIKES)
                .child(myKey).setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                tvlike.setTag("1");
                int number;
                try {
                    number = Integer.valueOf(tvlike.getText().toString());
                } catch (Exception e) {
                    number = 0;
                }
                number++;
                tvlike.setText("" + number);
                tvlike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_blue, 0, 0, 0);

                sendNotification(postKey, commentsList.get(position).getCommentedByUserKey(),
                        NotificationType.LIKE_COMMENT, commentsList.get(position).usertoken);            }
        });
    }

    private void removeLike(String postKey, String commentKey, TextView tvlike) {
        if (postRef == null)
            postRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postRef.child(postKey).child(DataBaseTablesConstants.COMMENTS).child(commentKey).child(DataBaseTablesConstants.LIKES)
                .child(myKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                tvlike.setTag("0");
                int number;
                try {
                    number = Integer.valueOf(tvlike.getText().toString());
                } catch (Exception e) {
                    number = 0;
                }
                if (number > 0)
                    number--;
                tvlike.setText("" + number);
                tvlike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
            }
        });
    }

    private void sendNotification(String postKey, String userKey, NotificationType notificationType, String token) {
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
//                sendNotification(token, mContext.getResources().getString(R.string.likes_your_comment),
//                        notificationType.name());
                //Context mContext, String token, String body, String CLICK_ACTION
                //    ,String postKey,String  chatKey,String userKey
                NotificationHelper.sendNotification(mContext,token,
                        mContext.getResources().getString(R.string.likes_your_comment), Constant.NOTIFICATION_ACTION,
                        postKey,"", ""+myKey);

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
//            notification.addProperty("title", sharedEdit.getString("getFirstName", "someone"));
//            notification.addProperty("body", body);
//            notification.addProperty("click_action","NOTIFICATION");
//            JsonObject data = new JsonObject();
//            data.addProperty("postKey", postKey);
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

    private void checkLike(String postKey, String commentKey, TextView tvlike) {
        if (postRef == null)
            postRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        tvlike.setText("0");
        postRef.child(postKey).child(DataBaseTablesConstants.COMMENTS).child(commentKey).
                child(DataBaseTablesConstants.LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvlike.setText("" + dataSnapshot.getChildrenCount());
                for (DataSnapshot snapshot :
                        dataSnapshot.getChildren()) {

                    if (dataSnapshot.child(myKey).exists()) {
                        tvlike.setTag("1");
                        tvlike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_blue, 0, 0, 0);
                    } else {
                        tvlike.setTag("0");
                        tvlike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        postRef.child(postKey).child(DataBaseTablesConstants.COMMENTS).child(commentKey).
//                child(DataBaseTablesConstants.LIKES).child(myKey)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            tvlike.setTag("1");
//                            tvlike.setImageResource(R.drawable.ic_like_blue);
//                        } else {
//                            tvlike.setTag("0");
//                            tvlike.setImageResource(R.drawable.ic_like_black);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, View view_active, String userKey, int position) {
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
                            Glide.with(mContext).load(usersModel.getImage()).placeholder(R.drawable.profile_img).into(civ_profile_img);
                        } catch (Exception e) {
                            civ_profile_img.setImageResource(R.drawable.profile_img);
                        }
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                        commentsList.get(position).usertoken = (usersModel.getUserToken());

                        Calendar lastTime = Calendar.getInstance();
                        lastTime.setTimeInMillis(usersModel.getLastOnlineTime());
                        Calendar now = Calendar.getInstance();

                        if (now.getTimeInMillis() - lastTime.getTimeInMillis() < (60 * 1000))
                            view_active.setBackgroundResource(R.drawable.dot_active);
                        else
                            view_active.setBackgroundResource(R.drawable.dot_inactive);

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
    public int getItemCount() {
        if (commentsList == null)
            return 0;
        return commentsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.view_active)
        View view_active;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tv_comment)
        TextView tv_comment;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.iv_like)
        TextView iv_like;
        @BindView(R.id.richLinkView)
        RichLinkViewTelegram richLinkView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
