package com.fadfadah.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.activity.AddFriendActivity;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.NotificationHelper;
import com.fadfadah.app.models.FriendsModel;
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    ArrayList<FriendsModel> friendsList;
    //    DatabaseReference notificationRef = null;
    DatabaseReference userRef = null;
    private Context mContext;
    private String myKey = "";

    public FriendRequestAdapter(Context mContext, ArrayList<FriendsModel> friendsList) {
        this.mContext = mContext;
        this.friendsList = friendsList;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!friendsList.get(position).isValid()) {
            cancelFriendRequest(friendsList.get(position).getUserKey());
            holder.civ_profile_img.setBackgroundResource(R.drawable.profile_img);
            holder.tv_username.setText("--");
            holder.tv_confirm.setVisibility(View.GONE);
            holder.tv_cancel.setVisibility(View.GONE);
            holder.tv_notification_date.setText("--");
            holder.tv_notification_desc.setText("--");

            return;
        }
        loadUser(holder.civ_profile_img, holder.tv_username, friendsList.get(position).getUserKey(), position);

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(friendsList.get(position).getRequestTimeMillisecond());
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.WEEK_OF_MONTH) - time.get(Calendar.WEEK_OF_MONTH) > 0)
            holder.tv_notification_date.setText((now.get(Calendar.WEEK_OF_MONTH) - time.get(Calendar.WEEK_OF_MONTH))
                    + " " + mContext.getResources().getString(R.string.weeks_ago));
        else if (now.get(Calendar.DAY_OF_MONTH) - time.get(Calendar.DAY_OF_MONTH) > 0)
            holder.tv_notification_date.setText((now.get(Calendar.DAY_OF_MONTH) - time.get(Calendar.DAY_OF_MONTH))
                    + " " + mContext.getResources().getString(R.string.days_ago));
        else if (now.get(Calendar.HOUR_OF_DAY) - time.get(Calendar.HOUR_OF_DAY) > 0)
            holder.tv_notification_date.setText((now.get(Calendar.HOUR_OF_DAY) - time.get(Calendar.HOUR_OF_DAY))
                    + " " + mContext.getResources().getString(R.string.hours_ago));
        else if (now.get(Calendar.MINUTE) - time.get(Calendar.MINUTE) >= 0)
            holder.tv_notification_date.setText((now.get(Calendar.MINUTE) - time.get(Calendar.MINUTE))
                    + " " + mContext.getResources().getString(R.string.minutes_ago));
        else
            holder.tv_notification_date.setText(mContext.getResources().getString(R.string.more_month));


    }

    private void cancelFriendRequest(String key) {
        DatabaseReference addFriendRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.FRIENDS_REQUEST);
        addFriendRef.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child(key).removeValue();
    }

    private void acceptFriendRequest(String toKey, String token) {
        DatabaseReference addFriendRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.FRIENDS);
        addFriendRef.child(toKey).child(String.valueOf(FirebaseAuth.getInstance().getUid())).setValue(getCurrentTimeMilliSecond());
        addFriendRef.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child(toKey).setValue(getCurrentTimeMilliSecond());
        cancelFriendRequest(toKey);
//        sendNotification(token, mContext.getResources().getString(R.string.accept_your_request),
//                SelectedFragment.FRIEND.name());
        NotificationHelper.sendNotification(mContext, token,
                mContext.getResources().getString(R.string.accept_your_request), Constant.FRIEND_ACTION,
                "", "", myKey);
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
//            notification.addProperty("click_action","FRIEND");
//            JsonObject data = new JsonObject();
//            data.addProperty("user key", myKey);
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

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, String userKey, int position) {
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
                        friendsList.get(position).setUserToken(usersModel.getUserToken());

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
        if (friendsList == null)
            return 0;
        return friendsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tv_notification_date)
        TextView tv_notification_date;
        @BindView(R.id.tv_notification_desc)
        TextView tv_notification_desc;
        @BindView(R.id.tv_confirm)
        TextView tv_confirm;
        @BindView(R.id.tv_cancel)
        TextView tv_cancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            civ_profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AddFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.USER_KEY, friendsList.get(getAdapterPosition()).
                            getUserKey());
                    mContext.startActivity(intent);
                }
            });
            tv_username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    civ_profile_img.performClick();
                }
            });
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelFriendRequest(friendsList.get(getAdapterPosition()).getUserKey());
                }
            });
            tv_confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acceptFriendRequest(friendsList.get(getAdapterPosition()).getUserKey(),
                            friendsList.get(getAdapterPosition()).getUserToken());
                }
            });
        }
    }
}
