package com.fadfadah.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.ChatInboxAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.models.ChatInboxModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatInboxFragment extends Fragment {


    @BindView(R.id.messages_inbox_recyclerview)
    RecyclerView messagesInboxRecyclerview;


    private FragmentSelectionListener selectionListener;

    public ChatInboxFragment(FragmentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    FirebaseUser currentFirebaseUser;

    public ChatInboxFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_inbox, container, false);
//        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        messagesInboxRecyclerview=view.findViewById(R.id.messages_inbox_recyclerview);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        messagesInboxRecyclerview.setLayoutManager(layoutManager);
        messagesInboxRecyclerview.setHasFixedSize(true);
        chatInboxAdapter = new ChatInboxAdapter(getContext(), inboxModelArrayList);
        messagesInboxRecyclerview.setAdapter(chatInboxAdapter);

        prepareInboxmessagesData();
    }

    private ArrayList<ChatInboxModel> inboxModelArrayList = new ArrayList<>();
    ChatInboxAdapter chatInboxAdapter;

    private void prepareInboxmessagesData() {
        inboxModelArrayList.clear();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.INBOX);
        reference.child("" + FirebaseAuth.getInstance().getUid()).orderByChild("timeStamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {


                            inboxModelArrayList.clear();
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                ChatInboxModel chatInboxModel = postSnapshot.getValue(ChatInboxModel.class);
                                inboxModelArrayList.add(0, chatInboxModel);
                            }
                            chatInboxAdapter.notifyDataSetChanged();
                        } catch (Exception r) {
                            r.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }



}
