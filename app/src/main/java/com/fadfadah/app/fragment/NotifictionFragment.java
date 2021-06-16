package com.fadfadah.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.FriendRequestAdapter;
import com.fadfadah.app.adapter.NotificationsAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.models.FriendsModel;
import com.fadfadah.app.models.NotificationsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotifictionFragment extends Fragment {


    private Context mContext;
    @BindView(R.id.recyclerView_notifications)
    RecyclerView recyclerView_notifications;
    @BindView(R.id.recyclerView_friends)
    RecyclerView recyclerView_friends;
    @BindView(R.id.tv_more_friends)
    TextView tv_more_friends;
    @BindView(R.id.tv_more_notifications)
    TextView tv_more_notifications;

    private NotificationsAdapter adapterNotification;
    private FriendRequestAdapter adapterFriendRequest;

    private FragmentSelectionListener selectionListener;
    private ArrayList<NotificationsModel> notificationList = new ArrayList<>();
    private static final String TAG = "NotifictionFragment";
    private ArrayList<FriendsModel> requestList = new ArrayList<>();
    private int limitNotifications = 5;
    private int limitFriends = 50;
    DatabaseReference notificationReference;
    private ChildEventListener notificationListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            try {
                NotificationsModel model = dataSnapshot.getValue(NotificationsModel.class);
                notificationList.add(0, model);
                if (recyclerView_notifications.getChildCount() == 0) {
                    adapterNotification = new NotificationsAdapter(mContext, notificationList, selectionListener);
                    recyclerView_notifications.setAdapter(adapterNotification);
                } else
                    adapterNotification.notifyDataSetChanged();
                limitNotifications = recyclerView_notifications.getChildCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "onChildAdded: ");
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private ChildEventListener friendListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            try {
                FriendsModel friendsModel = new FriendsModel();
                friendsModel.setUserKey(dataSnapshot.getKey());
                friendsModel.setRequestTimeMillisecond(dataSnapshot.getValue(Long.class));
                requestList.add(0, friendsModel);
                if (recyclerView_friends.getChildCount() == 0) {
                    adapterFriendRequest = new FriendRequestAdapter(mContext, requestList);
                    recyclerView_friends.setAdapter(adapterFriendRequest);
                } else
                    adapterFriendRequest.notifyDataSetChanged();
                limitFriends = recyclerView_friends.getChildCount();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            FriendsModel friendsModel = new FriendsModel();
            friendsModel.setUserKey(dataSnapshot.getKey());
            for (int i = 0; i < requestList.size(); i++) {
                if (requestList.get(i).getUserKey().equals(friendsModel.getUserKey())) {
                    requestList.remove(i);
                    adapterFriendRequest.notifyItemRemoved(i);
                    break;
                }
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    public NotifictionFragment(FragmentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifiction, container, false);
      //  ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    public NotifictionFragment() {
    }
    @OnClick(R.id.tv_chat)
    public void onViewClicked() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.CHAT);
    }
    @OnClick(R.id.tv_stories)
    void openStories() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.STORIES);
    }
    @OnClick(R.id.tv_search)
    void gotoSearch() {
        if (selectionListener != null)
            selectionListener.search();
//            selectionListener.selectFragment(SelectedFragment.SEARCH);
    }
    @OnClick(R.id.tv_notification)
    void gotoNotification() {
        if (selectionListener != null)
            selectionListener.selectFragment(SelectedFragment.NOTIFICATION);
    }
    @OnClick(R.id.menu)
    void menu() {
        if (selectionListener != null)
            selectionListener.openDrawer();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView_notifications=view.findViewById(R.id.recyclerView_notifications);
        recyclerView_friends=view.findViewById(R.id.recyclerView_friends);
        tv_more_friends=view.findViewById(R.id.tv_more_friends);
        adapterNotification = new NotificationsAdapter(mContext, notificationList, selectionListener);
        recyclerView_notifications.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_notifications.setHasFixedSize(true);
//        recyclerView_notifications.setAdapter(adapterNotification);

        adapterFriendRequest = new FriendRequestAdapter(mContext, requestList);
        recyclerView_friends.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_friends.setHasFixedSize(true);
        recyclerView_friends.setAdapter(adapterFriendRequest);


    }

    @Override
    public void onStart() {
        super.onStart();
        limitNotifications = 5;
        limitFriends = 50;
    }



    @OnClick(R.id.tv_more_notifications)
    void moreNotificaion() {
        limitNotifications += 10;
//        getNotifications(limitNotifications);
    }

    @OnClick(R.id.tv_more_friends)
    void moreFriends() {
        limitFriends += 10;
        getFriendRequest(limitFriends);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView_friends != null) {
            getFriendRequest(limitFriends);
//            getNotifications(limitNotifications);
        }

    }

    DatabaseReference reqReference;

    private void getFriendRequest(int limit) {
        if (limit < 1)
            limit = 50;
        requestList.clear();
        if (reqReference == null)
            reqReference = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS_REQUEST).child(String.valueOf(FirebaseAuth.getInstance().getUid()));
        reqReference.removeEventListener(friendListener);
        reqReference.limitToLast(limit).addChildEventListener(friendListener);
    }

//    private void getNotifications(int limit) {
//        if (limit < 1)
//            limit = 5;
//        notificationList.clear();
//        if (notificationReference == null)
//            notificationReference = FirebaseDatabase.getInstance().
//                    getReference(DataBaseTablesConstants.NOTIFICATION).
//                    child(String.valueOf(FirebaseAuth.getInstance().getUid()));
//        notificationReference.removeEventListener(notificationListener);
//        notificationReference.limitToLast(limit).addChildEventListener(notificationListener);
//
//    }
}