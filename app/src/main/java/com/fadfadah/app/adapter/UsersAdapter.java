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
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    ArrayList<UsersModel> users;
    //    DatabaseReference notificationRef = null;
    DatabaseReference userRef = null;
    private Context mContext;

    public UsersAdapter(Context mContext, ArrayList<UsersModel> users) {
        this.mContext = mContext;
        this.users = users;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       try {
           Glide.with(mContext).load(users.get(position).getImage())
                   .placeholder(R.drawable.profile_img).into(holder.civ_profile_img);
       } catch (Exception e) {
           e.printStackTrace();
       }

        holder.tv_username.setText(users.get(position).getFirstName() + " " + users.get(position).getLastName());
       holder.tv_userDate.setText(users.get(position).getDay() + " / " + users.get(position).getMonth() + " / " + users.get(position).getYear());
        if (position % 2 == 0) {
            holder.view2.setVisibility(View.GONE);
            holder.view1.setVisibility(View.VISIBLE);
        } else {
            holder.view1.setVisibility(View.GONE);
            holder.view2.setVisibility(View.VISIBLE);
        }


    }


    @Override
    public int getItemCount() {
        if (users == null)
            return 0;
        return users.size();
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
             tv_userDate=itemView.findViewById(R.id.date_user);
             itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     Intent intent = new Intent(mContext, AddFriendActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     intent.putExtra(Constant.USER_KEY, users.get(getAdapterPosition()).getUserKey());
                     mContext.startActivity(intent);
                 }
             });
         }
    }
}
