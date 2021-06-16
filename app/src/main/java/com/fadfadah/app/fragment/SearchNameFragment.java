package com.fadfadah.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.UsersAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchNameFragment extends Fragment {

    public SearchNameFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.search_view)
    SearchView search_view;
    @BindView(R.id.recyclerView_friends)
    RecyclerView recyclerView_friends;
    private Context mContext;
    private ArrayList<UsersModel> usersModels = new ArrayList<>();
    UsersAdapter usersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search_name, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView_friends.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_friends.setHasFixedSize(true);

        search_view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void search(String query) {
        Query queryRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS)
                .orderByChild("firstName").startAt(query);
//                .orderByChild("lastName").startAt(query).endAt(query);

//        FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS).ord
        queryRef.limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersModels.clear();
                for (DataSnapshot snap :
                        dataSnapshot.getChildren()) {
                    UsersModel usersModel = snap.getValue(UsersModel.class);
                    usersModels.add(usersModel);
                }
                usersAdapter = new UsersAdapter(mContext, usersModels);
                recyclerView_friends.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}