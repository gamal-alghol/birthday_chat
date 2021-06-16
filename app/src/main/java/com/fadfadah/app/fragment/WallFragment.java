package com.fadfadah.app.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.fadfadah.app.activity.AddNewPostActivity;
import com.fadfadah.app.adapter.WallsAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.models.PostsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WallFragment extends Fragment {

    @BindView(R.id.recyclerView_walls)
    RecyclerView recyclerView_walls;
    @BindView(R.id.tv_chat)
    TextView tvChat;
    private Context mContext;
    private WallsAdapter adapter;
    private ArrayList<PostsModel> postsList = new ArrayList<>();

    public WallFragment() {
        // Required empty public constructor
    }

    private FragmentSelectionListener selectionListener;

    public WallFragment(FragmentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wall, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new WallsAdapter(mContext, postsList, selectionListener);
        recyclerView_walls.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_walls.setHasFixedSize(true);
        recyclerView_walls.setAdapter(adapter);
        recyclerView_walls.setItemViewCacheSize(50);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);
        getPosts();

        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter("com.fadfadah.app.ACTION_UPDATE_LIKES_COMMENTS"));
    }

    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("post");
            adapter.updateItem(key);
        }
    };

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

    private void getPosts() {
        DatabaseReference postReference = FirebaseDatabase.getInstance()
                .getReference(DataBaseTablesConstants.POSTS);

        postReference.limitToLast(60).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PostsModel postsModel = dataSnapshot.getValue(PostsModel.class);
                    if (postsModel.getPostPrivacy() == 1) {
                        postsList.add(0, postsModel);
                        adapter.notifyItemInserted(0);
                    } else
                        addPostifFriend(postsModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        });

    }

    DatabaseReference friendRef = null;

    private void addPostifFriend(PostsModel postsModel) {
        if (("" + FirebaseAuth.getInstance().getUid()).equals(postsModel.getUserKey())) {
            postsList.add(0, postsModel);
            adapter.notifyItemInserted(0);
            return;
        }

        if (friendRef == null)
            friendRef = FirebaseDatabase.getInstance().
                    getReference(DataBaseTablesConstants.FRIENDS);
        friendRef.child("" + FirebaseAuth.getInstance().getUid()).child(postsModel.getUserKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postsList.add(0,postsModel);
                    adapter.notifyItemInserted(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    @OnClick(R.id.tv_add_to_post)
    public void addtoPost() {
        startActivity(new Intent(mContext, AddNewPostActivity.class));
    }
}