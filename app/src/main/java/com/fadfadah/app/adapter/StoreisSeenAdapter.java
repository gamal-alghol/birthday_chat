package com.fadfadah.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.models.UsersModel;
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

public class StoreisSeenAdapter extends RecyclerView.Adapter<StoreisSeenAdapter.ViewHolder> {
    ArrayList<String> keys;
    DatabaseReference userRef = null;
    private Context mContext;

    public StoreisSeenAdapter(Context mContext, ArrayList<String> keys) {
        this.mContext = mContext;
        this.keys = keys;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        loadUser(holder.civ_profile_img, holder.view_active, keys.get(position));


    }

    private void loadUser(CircleImageView civ_profile_img, View view_active, String userKey
    ) {
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
                        Calendar lastTime = Calendar.getInstance();
                        lastTime.setTimeInMillis(usersModel.getLastOnlineTime());
                        Calendar now = Calendar.getInstance();
                        now.setTimeInMillis(Helper.getCurrentTimeMilliSecond());
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
        if (keys == null)
            return 0;
        return keys.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.view_active)
        View view_active;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}
