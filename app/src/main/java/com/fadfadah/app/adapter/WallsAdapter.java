package com.fadfadah.app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.activity.AddFriendActivity;
import com.fadfadah.app.activity.PostDetailActivity;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.NotificationType;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.helper.NotificationHelper;
import com.fadfadah.app.models.NotificationsModel;
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

public class WallsAdapter extends RecyclerView.Adapter<WallsAdapter.ViewHolder> {
    DatabaseReference userRef = null;
    DatabaseReference postReference = null;
    DatabaseReference notificationReference = null;
    private Context mContext;
    private ArrayList<PostsModel> postsList;
    private FragmentSelectionListener selectionListener;
    private String myKey;

    public WallsAdapter(Context mContext, ArrayList<PostsModel> postsList, FragmentSelectionListener selectionListener) {
        this.mContext = mContext;
        this.postsList = postsList;
        this.selectionListener = selectionListener;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_posts, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!postsList.get(position).isValid()) {
            deletePost(postsList.get(position).getPostKey(), -1);
            holder.tv_post_content.setText("");
            holder.tv_username.setText("");
            holder.tv_likes.setText("");
            holder.tv_comments.setText("");
            holder.tv_time.setText("");
            holder.iv_delete.setVisibility(View.GONE);
            holder.iv_post.setVisibility(View.GONE);
            holder.view_re_play.setVisibility(View.GONE);
            holder.civ_profile_img.setBackgroundResource(R.drawable.profile_img);
            return;
        }

        loadUser(holder.civ_profile_img, holder.tv_username, holder.view_active, postsList.get(position).getUserKey(), position);
        getNumberOfComments(holder.tv_comments, postsList.get(position).getPostKey());
        getNumberOfLikes(holder.tv_likes, postsList.get(position).getPostKey());

        String[] link = extractLinks(postsList.get(position).getPostContent());

        if (postsList.get(position).getPostContent() == null ||
                postsList.get(position).getPostContent().isEmpty())
            holder.tv_post_content.setText("");
        else {

            if (link != null && link.length > 0) {
                try {

            {
                        holder.richLinkView.setLink(link[0].contains("https://") ? link[0]
                                : "https://" + link[0], new ViewListener() {

                            @Override
                            public void onSuccess(boolean status) {

                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }
                } catch (Exception e) {

                }
                if (link[0].length() == postsList.get(position).getPostContent().length())
                    holder.tv_post_content.setText("");
                else
                    holder.tv_post_content.setText(postsList.get(position).getPostContent()
                            .replaceAll(link[0].toString(), ""));

            } else {
                holder.richLinkView.removeAllViews();
                holder.tv_post_content.setText(postsList.get(position).getPostContent());
            }
        }

        if ((postsList.get(position).getImageUrl() == null && postsList.get(position).getVideoUrl() == null)) {
            holder.iv_post.setVisibility(View.GONE);
            holder.view_re_play.setVisibility(View.GONE);
        } else if (postsList.get(position).getImageUrl() != null &&
                !postsList.get(position).getImageUrl().isEmpty()) {
            holder.iv_post.setVisibility(View.VISIBLE);
            holder.view_re_play.setVisibility(View.GONE);
            try {
                Glide.with(mContext).load(postsList.get(position).getImageUrl()).into(holder.iv_post);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if ((postsList.get(position).getVideoUrl() != null) &&
                !postsList.get(position).getVideoUrl().isEmpty()) {

            try {
                Glide.with(mContext).load(postsList.get(position).getVideoUrl())
                        .into(holder.iv_post);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.view_re_play.setVisibility(View.VISIBLE);
            holder.iv_post.setVisibility(View.VISIBLE);

        }


        if (myKey.equals(postsList.get(position).getUserKey()))
            holder.iv_delete.setVisibility(View.VISIBLE);
        else holder.iv_delete.setVisibility(View.INVISIBLE);

        holder.tv_time.setText(Helper.calcTime(postsList.get(position).getPostTimeInMillis(), mContext));
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectionListener != null)
//                    selectionListener.selectViewComments(postsList.get(position).getPostKey(),
//                            postsList.get(position).getUserKey());
//            }
//        });
    }

    private void deletePost(String postKey, int position) {
        FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS).child(postKey).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (position != -1)
                                postsList.remove(position);
                            notifyItemRemoved(position);
                        }
                    }
                });
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
                        try {
                            Glide.with(mContext).load(usersModel.getImage()).placeholder(R.drawable.profile_img).into(civ_profile_img);
                        } catch (Exception e) {
                            civ_profile_img.setImageResource(R.drawable.profile_img);
                        }
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                        Calendar lastTime = Calendar.getInstance();
                        lastTime.setTimeInMillis(usersModel.getLastOnlineTime());
                        Calendar now = Calendar.getInstance();
                        postsList.get(position).userToken = (usersModel.getUserToken());
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

    private void sendNotification(String postKey, String userKey, NotificationType
            notificationType, String token
    ) {
        if (userKey.equals(myKey))
            return;
        if (notificationReference == null)
            notificationReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.NOTIFICATION);
        String key = notificationReference.push().getKey();

        NotificationsModel model = new NotificationsModel(key, 0, String.valueOf(myKey)
                , postKey, notificationType, getCurrentTimeMilliSecond());
///if my key don't send
        notificationReference.child(userKey).child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ///send here
                //Context mContext, String token, String body, String CLICK_ACTION
                //    ,String postKey,String  chatKey,String userKey
                NotificationHelper.sendNotification(mContext,token,
                        mContext.getResources().getString(R.string.likes_your_post), Constant.NOTIFICATION_ACTION,
                        postKey,"", userKey);
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

//    private void sendNotification(String token, String body, String type, String postKey, String postOwnerKey) {
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
//            }
//
//            @Override
//            public void onFailure(Call<MessageResponse> call, Throwable t) {
//            }
//        });
//    }

    private void addOrRemoveLike(TextView tv_likes, int position) {


        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        if (tv_likes.getTag() == null || tv_likes.getTag() == "0") {
            postReference.child(postsList.get(position).getPostKey()).child(DataBaseTablesConstants.LIKES).child(String.valueOf(myKey)).setValue("1")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            getNumberOfLikes(tv_likes, postsList.get(position).getPostKey());
                            sendNotification(postsList.get(position).getPostKey(), postsList.get(position).getUserKey(),
                                    NotificationType.LIKE, "" + postsList.get(position).userToken);

                        }
                    });
        } else {
            postReference.child(postsList.get(position).getPostKey()).child(DataBaseTablesConstants.LIKES).child(String.valueOf(myKey)).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            getNumberOfLikes(tv_likes, postsList.get(position).getPostKey());
                        }
                    });
        }
    }

    private void getNumberOfComments(TextView tv_comments, String postKey) {
        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postReference.child(postKey).child(DataBaseTablesConstants.COMMENTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_comments.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getNumberOfLikes(TextView textView, String postKey) {
        if (postReference == null)
            postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        postReference.child(postKey).child(DataBaseTablesConstants.LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                if (dataSnapshot.child(String.valueOf(myKey)).exists()) {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_fill_red_24, 0, 0, 0);
                    textView.setTag("1");
                } else {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_favorite_red_24, 0, 0, 0);
                    textView.setTag("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (postsList == null)
            return 0;
        return postsList.size();
    }

    public void updateItem(String key) {
        if (postsList==null || key==null )
            return;
        for (int i = 0; i < postsList.size(); i++) {
            if (postsList.get(i).getPostKey().equalsIgnoreCase(key)){
                notifyItemChanged(i);
                break;
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.tv_comments)
        TextView tv_comments;
        @BindView(R.id.tv_likes)
        TextView tv_likes;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.iv_post)
        ImageView iv_post;
        @BindView(R.id.tv_post_content)
        TextView tv_post_content;
        @BindView(R.id.view_re_play)
        FrameLayout view_re_play;
        @BindView(R.id.view_active)
        View view_active;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.iv_delete)
        ImageView iv_delete;
        @BindView(R.id.richLinkView)
        RichLinkViewTelegram richLinkView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            iv_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
// Pass data object in the bundle and populate details activity.
                    intent.putExtra(PostDetailActivity.IMAGE, postsList.get(getAdapterPosition()).getImageUrl());
                    intent.putExtra(PostDetailActivity.TEXT, postsList.get(getAdapterPosition()).getPostContent());
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) mContext, (View) iv_post, "transition");
                    mContext.startActivity(intent, options.toBundle());
                }
            });
            view_re_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PostDetailActivity.class);
// Pass data object in the bundle and populate details activity.
                    intent.putExtra(PostDetailActivity.VIDEO, postsList.get(getAdapterPosition()).getVideoUrl());
                    intent.putExtra(PostDetailActivity.TEXT, postsList.get(getAdapterPosition()).getPostContent());
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) mContext, (View) iv_post, "transition");
                    mContext.startActivity(intent, options.toBundle());
                }
            });

            tv_likes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addOrRemoveLike(tv_likes, getAdapterPosition());
                }
            });

            tv_comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectionListener != null)
                        selectionListener.selectViewComments(postsList.get(getAdapterPosition()).getPostKey(),
                                postsList.get(getAdapterPosition()).getUserKey());
                }
            });
            civ_profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AddFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.USER_KEY, postsList.get(getAdapterPosition()).getUserKey());
                    mContext.startActivity(intent);
                }
            });
            tv_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AddFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.USER_KEY, postsList.get(getAdapterPosition()).getUserKey());
                    mContext.startActivity(intent);
                }
            });
            iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getResources().getString(R.string.delete_post))
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(mContext.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deletePost(postsList.get(getAdapterPosition()).getPostKey(), getAdapterPosition());

                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                }
            });
        }
    }

}
