package com.fadfadah.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.SearchResultAdapter;
import com.fadfadah.app.adapter.UsersAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.models.UsersModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SearchResultFragment extends Fragment {


    @BindView(R.id.recyclerView_search_result)
    RecyclerView recyclerView_search_result;
    private Context mContext;
    private ArrayList<String> keysList = new ArrayList<>();
    private String year, month, day;

    @OnClick(R.id.iv_back)
    void back() {
        if (listener != null)
            listener.selectFragment(SelectedFragment.SEARCH_DATE);
    }

    FragmentSelectionListener listener;

    public SearchResultFragment(FragmentSelectionListener listener) {
        this.listener = listener;
    }

    public SearchResultFragment() {
    }

    @OnClick(R.id.tv_search)
    void search() {
        if (listener != null)
            listener.selectFragment(SelectedFragment.SEARCH_DATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_result, container, false);
        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView_search_result.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView_search_result.setHasFixedSize(true);
    }

    public void setDate(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;
        if (mContext != null)
            load();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mContext != null)
            load();
    }

    private void load() {
        keysList.clear();
        recyclerView_search_result.setAdapter(new UsersAdapter(mContext, null));
        Log.d(TAG, "load: "+year+" "+month+" "+day);
        DatabaseReference searchRef = FirebaseDatabase.getInstance().
                getReference(DataBaseTablesConstants.SEARCH);
        if (year ==null) {
            SharedPreferences sharedPreferences =
                    mContext.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            year= String.valueOf(sharedPreferences.getInt("getYear", 1990));
            month= String.valueOf(sharedPreferences.getInt("getMonth", 1));
            day= String.valueOf(sharedPreferences.getInt("getDay", 1));

            searchRef.child(year).child(month).child(day)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snap : dataSnapshot.getChildren()
                            ) {
                                keysList.add(snap.getKey());
                            }
                            Log.d(TAG, "onDataChange: "+keysList.size());
                            recyclerView_search_result.setAdapter(new SearchResultAdapter(mContext, keysList));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {


            Query queryRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS)
                    .orderByChild("year").equalTo(Integer.parseInt(year));
//                    .equalTo(sharedPreferences.getInt("getYear", 1990));
//                .orderByChild("lastName").startAt(query).endAt(query);

//        FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS).ord
            Log.d("TAGTAG", "load: year");
            ArrayList<UsersModel> usersModels = new ArrayList<>();

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: "+dataSnapshot.getChildrenCount());
                    for (DataSnapshot snap :
                            dataSnapshot.getChildren()) {
                        UsersModel usersModel = snap.getValue(UsersModel.class);
                        usersModels.add(usersModel);
                    }
                    Log.d(TAG, "onDataChange: "+usersModels.size());
                    recyclerView_search_result.setAdapter(new UsersAdapter(mContext, usersModels));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: "+databaseError.getMessage());
                }
            });
        }
    }

    private static final String TAG = "SearchResultFragment";
}