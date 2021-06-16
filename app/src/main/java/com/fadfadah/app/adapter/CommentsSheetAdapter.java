package com.fadfadah.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.models.CommentsModel;
import com.fadfadah.app.models.UsersModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class CommentsSheetAdapter extends RecyclerView.Adapter<CommentsSheetAdapter.ViewHolder> {
    private Context context;
    Calendar now, time;
    DatabaseReference userRef;
    FragmentManager fragmentManager;
    private ArrayList<CommentsModel> commentsArrayList;

    public CommentsSheetAdapter(ArrayList<CommentsModel> commentsArrayList, Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.commentsArrayList = commentsArrayList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public CommentsSheetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comments, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommentsModel commentsModel = commentsArrayList.get(position);

        holder.bind(commentsModel);
    }

    @Override
    public int getItemCount() {
        return commentsArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, comment, time_tv;
        CircleImageView userImage;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_username);
            comment = itemView.findViewById(R.id.tv_comment);
            time_tv = itemView.findViewById(R.id.tv_time);
            userImage = itemView.findViewById(R.id.civ_profile_img);


        }

        public void bind(CommentsModel commentsModel) {
            time = Calendar.getInstance();
            time.setTimeInMillis(commentsModel.getCommentTimeInMillis());
            now = Calendar.getInstance();
            loadUser(name, commentsModel.getCommentedByUserKey());
            getImageUser(userImage, commentsModel.getCommentedByUserKey());
            getTime(time_tv);
            comment.setText(commentsModel.getComment());
        }

    }

    private void getImageUser(CircleImageView userImage, String userKey) {
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
                            Glide.with(context.getApplicationContext()).load(usersModel.getImage())
                                    .placeholder(R.drawable.profile_img).into(userImage);
                        } catch (Exception e) {
                            userImage.setImageResource(R.drawable.profile_img);
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

    private void getTime(TextView time_tv) {
        if (now.getTimeInMillis() - time.getTimeInMillis() >= 59 * 1000 * 60) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            time_tv.setText((calendar.getTimeInMillis() / (59 * 1000 * 60)) + " " + context.getResources().getString(R.string.hours_ago));
        } else if (now.getTimeInMillis() - time.getTimeInMillis() >= 1000 * 60) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            time_tv.setText(calendar.get(Calendar.MINUTE) + " " + context.getResources().getString(R.string.minutes_ago));
        } else
            time_tv.setText(R.string.just_now);
    }

    private void loadUser(TextView tv_username, String userKey) {
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
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
