package com.fadfadah.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.activity.ChatActivity;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Constant;
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.models.ChatInboxModel;
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatInboxAdapter extends RecyclerView.Adapter<ChatInboxAdapter.Filesviewholder> implements Filterable {
    private Context context;
    private ArrayList<ChatInboxModel> data;
    private ArrayList<ChatInboxModel> messagesList;
    private DatabaseReference userRef;
    private String myKey;
    private DatabaseReference inboxRef;

    public ChatInboxAdapter(Context context, ArrayList<ChatInboxModel> datam) {
        this.context = context;
        this.data = datam;
        this.messagesList = datam;
        this.myKey = "" + FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public Filesviewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_inbox_item, viewGroup, false);
        return new Filesviewholder(view);
    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, View view_active, String userKey,
                          int position) {
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        try {
                            Glide.with(context).load(usersModel.getImage()).placeholder(R.drawable.profile_img).into(civ_profile_img);
                        } catch (Exception e) {
                            civ_profile_img.setImageResource(R.drawable.profile_img);
                        }
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                        Calendar lastTime = Calendar.getInstance();
                        lastTime.setTimeInMillis(usersModel.getLastOnlineTime());
                        Calendar now = Calendar.getInstance();
                        now.setTimeInMillis(Helper.getCurrentTimeMilliSecond());
                        messagesList.get(position).userToken = (usersModel.getUserToken());
                        messagesList.get(position).userImage = (usersModel.getImage());
                        messagesList.get(position).name = (usersModel.getFirstName() + " " + usersModel.getLastName());


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

    public void onBindViewHolder(@NonNull final Filesviewholder holder, int i) {
        if (!messagesList.get(i).isValid()) {
            deleteInbox(messagesList.get(i).getFromId());
            holder.tv_last_message.setText("--");
            holder.tv_time.setText("--");
            holder.tv_username.setText("--");
            holder.civ_profile_img.setBackgroundResource(R.drawable.profile_img);
            return;
        }
        loadUser(holder.civ_profile_img, holder.tv_username, holder.view_active,
                messagesList.get(i).getFromId(), i);
        holder.tv_time.setText(Helper.calcTime(messagesList.get(i).getTimeStamp(), context));
        holder.tv_last_message.setText(messagesList.get(i).getLastMessage());
        setReceived(messagesList.get(i).getFromId());

    }

    private void deleteInbox(String fromId) {
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
        inboxRef.child(myKey).child(fromId).removeValue();
        inboxRef.child(myKey).child(fromId).removeValue();
    }

    private void setReceived(String fromId) {
        if (inboxRef == null)
            inboxRef = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.INBOX);
        inboxRef.child(fromId).child(myKey).child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists() && dataSnapshot.getValue(Integer.class) == 0)
                        inboxRef.child(fromId).child(myKey).child("status").setValue(1);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = (ArrayList<ChatInboxModel>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ChatInboxModel> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = messagesList;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    protected List<ChatInboxModel> getFilteredResults(String constraint) {
        List<ChatInboxModel> results = new ArrayList<>();

        for (ChatInboxModel item : messagesList) {
            if (("" + item.name).toLowerCase().contains(constraint)) {
                results.add(item);
            }
        }
        return results;
    }

    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        return data.size();
    }


    class Filesviewholder extends RecyclerView.ViewHolder {
        @BindView(R.id.civ_profile_img)
        CircleImageView civ_profile_img;
        @BindView(R.id.tv_username)
        TextView tv_username;
        @BindView(R.id.tv_time)
        TextView tv_time;
        @BindView(R.id.view_active)
        View view_active;
        @BindView(R.id.tv_last_message)
        TextView tv_last_message;

        public Filesviewholder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(ChatActivity.USER_IMAGE, "" + data.get(getAdapterPosition()).userImage);
                        intent.putExtra(Constant.USER_KEY, data.get(getAdapterPosition()).getFromId());
                        intent.putExtra(ChatActivity.USER_NAME, "" + data.get(getAdapterPosition()).name);
                        intent.putExtra(Constant.CHAT_KEY, data.get(getAdapterPosition()).getChatId());
                        intent.putExtra(ChatActivity.USER_TOKEN, "" + data.get(getAdapterPosition()).userToken);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
