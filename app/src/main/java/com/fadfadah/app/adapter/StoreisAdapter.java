package com.fadfadah.app.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.activity.StoryDetailActivity;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.fragment.CommentsSheetFragment;
import com.fadfadah.app.models.CommentsModel;
import com.fadfadah.app.models.StoriesModel;
import com.fadfadah.app.models.StoriesOwnerModel;
import com.fadfadah.app.models.UsersModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.schwaab.avvylib.AvatarView;

public class StoreisAdapter extends RecyclerView.Adapter<StoreisAdapter.ViewHolder> {
    ArrayList<StoriesOwnerModel> storiesList;
    ArrayList<CommentsModel> numCommentsArray;
    DatabaseReference userRef = null;
    private Context mContext;
    private String myKey = "";
    private FragmentManager fragmentManager;
    public StoreisAdapter(Context mContext, ArrayList<StoriesOwnerModel> storiesList, FragmentManager fragmentManager) {
        this.mContext = mContext;
        this.storiesList = storiesList;
        this.fragmentManager=fragmentManager;
        myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stories, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        numCommentsArray=new ArrayList<>();
        loadUser(holder.tv_username, storiesList.get(position).getUserKey(),holder.civ_my_status);


        try {
            Glide.with(mContext).load(storiesList.get(position).lastVideo()).into(holder.civ_my_status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.civ_my_status.setNumberOfArches(storiesList.get(position).numberOfStories());
        holder.civ_my_status.setText("" + storiesList.get(position).numberOfStories());


        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(storiesList.get(position).lastTime());
        Calendar now = Calendar.getInstance();
//        if (now.get(Calendar.WEEK_OF_MONTH) - time.get(Calendar.WEEK_OF_MONTH) > 0)
//            holder.tv_time.setText((now.get(Calendar.WEEK_OF_MONTH) - time.get(Calendar.WEEK_OF_MONTH))
//                    + " " + mContext.getResources().getString(R.string.weeks_ago));
//        else

        checkAndDelete(storiesList.get(position).getStoriesModel(), position);
//        if (now.getTimeInMillis() - time.getTimeInMillis() >= (1000 * 60 * 60 * 24))
//            holder.tv_time.setText("delete");
//        else


        if (now.getTimeInMillis() - time.getTimeInMillis() >= 59 * 1000 * 60) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            holder.tv_time.setText((calendar.getTimeInMillis() / (59 * 1000 * 60)) + " " + mContext.getResources().getString(R.string.hours_ago));
        } else if (now.getTimeInMillis() - time.getTimeInMillis() >= 1000 * 60) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(now.getTimeInMillis() - time.getTimeInMillis());
            holder.tv_time.setText(calendar.get(Calendar.MINUTE) + " " + mContext.getResources().getString(R.string.minutes_ago));
        } else{
            holder.tv_time.setText(R.string.just_now);}
getNumComments(storiesList.get(position).getUserKey(),holder.numComments);
getNumLiks(storiesList.get(position).getUserKey(),holder.numLikes);
getIsLike(storiesList.get(position).getUserKey(),holder.like);


        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommentsSheetFragment bottomSheetFilter=new CommentsSheetFragment(storiesList.get(position).getUserKey());
               bottomSheetFilter.show(fragmentManager,bottomSheetFilter.getTag());

            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLike(storiesList.get(position).getUserKey());
            }
        });


    }

    private void getNumLiks(String userKey, TextView numLike) {
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                .child(userKey).child("like");
    //    Log.d("ttt",userKey);
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 numLike.setText((int)dataSnapshot.getChildrenCount() +" "+mContext.getResources().getString(R.string.liked));

                //  numLike.setText((int)dataSnapshot.getChildrenCount() + R.string.liked);
              //  Log.d("ttt",(int)dataSnapshot.getChildrenCount()+"////like");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getIsLike(String userKey,TextView like) {

        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                .child(userKey).child("like");
        Log.d("ttt",userKey+"/guserkey");

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnap:dataSnapshot.getChildren()){
                    Log.d("ttt",dataSnap.getKey()+"/getKey()");
                    if(dataSnap.getKey().equals(FirebaseAuth.getInstance().getUid())){

                        like.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_like_1),null,null,null);
                        like.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                        break;
                    }else {
                        like.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_like),null,null,null);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void setLike(String userKey) {
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                .child(userKey).child("like");

        HashMap <String, Integer> likeModle=new HashMap<>();
        likeModle.put(FirebaseAuth.getInstance().getUid(),1);
        postReference.child(FirebaseAuth.getInstance().getUid()).setValue(likeModle);
notifyDataSetChanged();
    }
    private void getNumComments(String userKey,TextView num_tv) {
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                .child(userKey).child("comments");
       // Log.d("ttt",userKey);
        postReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                num_tv.setText((int)dataSnapshot.getChildrenCount() +" "+ mContext.getResources().getString(R.string.comments));
              //  Log.d("ttt",(int)dataSnapshot.getChildrenCount()+"////");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       /* .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    CommentsModel commentsModel=snapshot.getValue(CommentsModel.class);
                    Log.d("ttt",commentsModel.getComment());
                    numCommentsArray.add(commentsModel);
                }
                num_tv.setText(numCommentsArray.size() +R.string.comments);
                Log.d("ttt",numCommentsArray.size()+"");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/
    }

    private void checkAndDelete(List<StoriesModel> storiesModel, int position) {
        if (storiesModel == null) return;
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < storiesModel.size(); i++) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(storiesModel.get(i).getTimeStamp());
            if (now.getTimeInMillis() - time.getTimeInMillis() >= (3*24*60*60*1000))

                try {
                    delete(storiesModel.get(i).getFromID(), storiesModel.get(i).getStoryKey(),
                            storiesModel.get(i).getVideoName());
                    storiesList.get(position).getStoriesModel().remove(i);
                    notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    DatabaseReference story;

    private void delete(String fromId, String storyKey, String videoName) {
        if (story == null)
            story = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);
        story.child(fromId).removeValue();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("status/" + videoName);
        riversRef.delete();
    }
    private void loadUser(TextView tv_username, String userKey,AvatarView civ_my_status) {
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                         civ_my_status.setAnimating(false);

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

//    private void setSeen(String notificationKey) {
//        if (notificationRef == null)
//            notificationRef = FirebaseDatabase.getInstance().
//                    getReference(DataBaseTablesConstants.NOTIFICATION).child( String.valueOf(FirebaseAuth.getInstance().getUid()));
//        notificationRef.child(notificationKey)
//                .child("notificationSeen").setValue(1);
//
//    }

    @Override
    public int getItemCount() {
        if (storiesList == null)
            return 0;
        return storiesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.civ_my_status)
        AvatarView civ_my_status;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tv_time)
        TextView tv_time;
TextView comments,numComments,like,numLikes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            comments=itemView.findViewById(R.id.comment);
            like=itemView.findViewById(R.id.like);
            numComments=itemView.findViewById(R.id.num_comments);
            numLikes=itemView.findViewById(R.id.num_liks);
            civ_my_status.setTotalArchesDegreeArea(70);
            civ_my_status.setAnimating(true);

            civ_my_status.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if ( storiesList.get(getAdapterPosition()).getUserKey().equals(myKey)) {
                        new AlertDialog.Builder(mContext)
                                .setMessage(mContext.getResources().getString(R.string.delete_story))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(mContext.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        if (story == null)
                                            story = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);

                                        story.child(myKey).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            storiesList.remove(getAdapterPosition());
                                                            notifyItemRemoved(getAdapterPosition());
                                                        }
                                                    }
                                                });

                                    }
                                })
                                .setNegativeButton(android.R.string.no, null).show();
                    }
                    return false;
                }
            });
            civ_my_status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, StoryDetailActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(StoryDetailActivity.USER_KEY, storiesList.get(getAdapterPosition()).getUserKey());
                    intent.putExtra(StoryDetailActivity.VIDEO, (Serializable) storiesList.get(getAdapterPosition()).getStoriesModel());

                    mContext.startActivity(intent);
                }
            });
        }
    }
}
