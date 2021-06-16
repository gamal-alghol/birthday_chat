package com.fadfadah.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    ArrayList<String> keysList;
    DatabaseReference userRef = null;
    private Context mContext;

    public SearchResultAdapter(Context mContext, ArrayList<String> keysList) {
        this.mContext = mContext;
        this.keysList = keysList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        loadUser(holder.civ_profile_img, holder.tv_username, keysList.get(position));
        if (position % 2 == 0) {
            holder.view2.setVisibility(View.GONE);
            holder.view1.setVisibility(View.VISIBLE);
        } else {
            holder.view1.setVisibility(View.GONE);
            holder.view2.setVisibility(View.VISIBLE);
        }


    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, String userKey) {
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
                        Log.d("ttt",usersModel.getFirstName());
                        tv_username.setText(usersModel.getFirstName() + "/// " + usersModel.getLastName());
                        Log.d("ttt",usersModel.toString());



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
        if (keysList == null)
            return 0;
        return keysList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.tv_username)

        TextView tv_username;

        @BindView(R.id.view1)
        View view1;
        @BindView(R.id.view2)
        View view2;
        TextView tv_userDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, AddFriendActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Constant.USER_KEY, keysList.get(getAdapterPosition()));
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
