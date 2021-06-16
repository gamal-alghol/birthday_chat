package com.fadfadah.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.NotificationType;
import com.fadfadah.app.models.NotificationsModel;
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

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<NotificationsModel> notificationList;
    private FragmentSelectionListener selectionListener;

    public NotificationsAdapter(Context mContext,
                                ArrayList<NotificationsModel> notificationList,
                                FragmentSelectionListener selectionListener) {
        this.mContext = mContext;
        this.selectionListener = selectionListener;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!notificationList.get(position).isValid()) {
            deleteNotifications(notificationList.get(position).getNotificationKey());
            holder.tv_username.setText("--");
            holder.tv_notification_date.setText("--");
            holder.tv_notification_desc.setText("--");
            holder.civ_profile_img.setBackgroundResource(R.drawable.profile_img);
            return;
        }
        loadUser(holder.civ_profile_img, holder.tv_username, holder.view_active, notificationList.get(position).getNotificationByUserKey());


        if (notificationList.get(position).getType() != null) {
            if (notificationList.get(position).getType().equals(NotificationType.COMMENT))
                holder.tv_notification_desc.setText(mContext.getResources().getString(R.string.comments_by));
            else
                holder.tv_notification_desc.setText(mContext.getResources().getString(R.string.liked_by));
        } else {
            holder.tv_notification_desc.setText("---");
        }
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(notificationList.get(position).getNotifcationTimeinMilli());
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

        if (notificationList.get(position).getNotificationSeen() == 1)
            holder.root_notification_layout.setBackgroundColor(Color.WHITE);
        else
            holder.root_notification_layout.setBackgroundColor(mContext.getResources().getColor(R.color.not_seen_color));


    }

    private void deleteNotifications(String notificationKey) {
        if (notificationRef == null)
            notificationRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.NOTIFICATION).
                    child(String.valueOf(FirebaseAuth.getInstance().getUid()));
        notificationRef.child(notificationKey).removeValue();
//        notificationList.remove(position);
    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, View view_active, String userKey) {
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

    DatabaseReference notificationRef = null;
    DatabaseReference userRef = null;

    private void setSeen(String notificationKey) {
        if (notificationRef == null)
            notificationRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.NOTIFICATION).
                    child(String.valueOf(FirebaseAuth.getInstance().getUid()));
        notificationRef.child(notificationKey)
                .child("notificationSeen").setValue(1);

    }

    @Override
    public int getItemCount() {
        if (notificationList == null)
            return 0;
        return notificationList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.view_active)
        View view_active;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tv_notification_date)
        TextView tv_notification_date;
        @BindView(R.id.tv_notification_desc)
        TextView tv_notification_desc;
        @BindView(R.id.root_notification_layout)
        ConstraintLayout root_notification_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSeen(notificationList.get(getAdapterPosition()).getNotificationKey());
                    if (selectionListener != null)
                        selectionListener.selectViewComments(notificationList.get(getAdapterPosition()).getPostKey(),
                                notificationList.get(getAdapterPosition()).getNotificationByUserKey());
                }
            });
        }
    }
}
