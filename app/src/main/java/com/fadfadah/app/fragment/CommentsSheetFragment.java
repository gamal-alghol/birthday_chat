package com.fadfadah.app.fragment;

import android.app.Dialog;
import android.app.FragmentManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.adapter.CommentsSheetAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.models.CommentsModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentsSheetFragment extends BottomSheetDialogFragment {
    View view;
    RecyclerView commentsRecyclerView;
    EditText commentsEditText;
    ProgressBar loadCommentsBar;
    ImageView sendCommentImageView;
    String userID;
    CommentsSheetAdapter CommentsSheetAdapter;
    LinearLayoutManager layoutManager;
    ArrayList<CommentsModel> commentsModelArrayList;

    public CommentsSheetFragment(String userID) {
        this.userID = userID;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }


    private void sendComment() {
        if (!commentsEditText.getText().toString().trim().equals("") && commentsEditText.getText() != null) {
            CommentsModel commentsModel = new CommentsModel();
            commentsModel.setComment(commentsEditText.getText().toString());
            commentsModel.setCommentedByUserKey(FirebaseAuth.getInstance().getUid());
            commentsModel.setCommentTimeInMillis(Helper.getCurrentTimeMilliSecond());
            DatabaseReference postReference = FirebaseDatabase.getInstance().getReference().child(DataBaseTablesConstants.STATUS).child(userID).child("comments");
            postReference.push().setValue(commentsModel);
            commentsEditText.setText("");
            commentsRecyclerView.scrollToPosition(commentsModelArrayList.size()- 1);

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.comments_sheet, container, false);
        commentsRecyclerView = view.findViewById(R.id.comments);
        commentsEditText = view.findViewById(R.id.edit_comment);
        sendCommentImageView = view.findViewById(R.id.send_comment_img);
        loadCommentsBar=view.findViewById(R.id.load_Comments_Bar);
       /* commentsRecyclerView.setDrawingCacheEnabled(true);
        commentsRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);*/

        layoutManager = new LinearLayoutManager(getContext());
        commentsRecyclerView.setLayoutManager(layoutManager);
        /* commentsRecyclerView.setItemAnimator(new DefaultItemAnimator());*/
        commentsModelArrayList = new ArrayList<>();
        CommentsSheetAdapter = new CommentsSheetAdapter(commentsModelArrayList, getContext(), getFragmentManager());
        commentsRecyclerView.setAdapter(CommentsSheetAdapter);

        getAllComments();

        sendCommentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });

        return view;
    }

    private void getAllComments() {
        Log.d("ttt", userID);
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS)
                .child(userID).child("comments");
        postReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
          //      for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CommentsModel commentsModel = dataSnapshot.getValue(CommentsModel.class);
                    Log.d("ttt", commentsModel.getComment());
                    commentsModelArrayList.add(commentsModel);
                    CommentsSheetAdapter.notifyDataSetChanged();
              //  }
                loadCommentsBar.setVisibility(View.GONE);
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

    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.comments_sheet, null, false);
        dialog.setContentView(rootView);
       /* ConstraintLayout bottomSheet =dialog.getWindow().findViewById(R.id.bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.top_round_white);*/
    }


}
