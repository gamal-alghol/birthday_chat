package com.fadfadah.app.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fadfadah.app.R;
import com.fadfadah.app.activity.HomeActivity;
import com.fadfadah.app.activity.Preview_Video_A;
import com.fadfadah.app.adapter.StoreisAdapter;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.callback.FragmentSelectionListener;
import com.fadfadah.app.callback.SelectedFragment;
import com.fadfadah.app.models.StoriesModel;
import com.fadfadah.app.models.StoriesOwnerModel;
import com.fadfadah.app.services.UploadStatusServices;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class StoriesFragment extends Fragment {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1232;
    private static final int REQUEST_RECORD_VIDEO = 632;
    private Context mContext;
    @BindView(R.id.civ_my_status)
    CircleImageView civ_my_status;
    @BindView(R.id.tv_username)
    TextView tv_username;
    @BindView(R.id.recycler_status)
    RecyclerView recycler_status;
    @BindView(R.id.fab_add_story)
    FloatingActionButton fab_add_story ;
    FloatingActionButton  fab_add_story2;
    private Uri videoUri;
    private StoreisAdapter storeisAdapter;
    ArrayList<StoriesOwnerModel> storiesList = new ArrayList<>();
    private static final String TAG = "StoriesFragment";
    private ArrayList<String> keys = new ArrayList<>();
    @BindView(R.id.progressbar)
    ProgressBar progressbar;
    TextView comments;

    public StoriesFragment() {
        // Required empty public constructor
    }

    FragmentSelectionListener selectionListener;

    public StoriesFragment(FragmentSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
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

    private BroadcastReceiver updateStories = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stories, container, false);
//        ButterKnife.bind(this, rootView);
        mContext = getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler_status=view.findViewById(R.id.recycler_status);
        progressbar=view.findViewById(R.id.progressbar);
        fab_add_story=view.findViewById(R.id.fab_add_story);
        fab_add_story2=view.findViewById(R.id.fab_add_story2);

fab_add_story2.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        addStory2();
    }
});
        recycler_status.setLayoutManager(new LinearLayoutManager(mContext));
        recycler_status.setHasFixedSize(true);
        storeisAdapter = new StoreisAdapter(mContext, storiesList,getFragmentManager());
        recycler_status.setItemViewCacheSize(100);
        recycler_status.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recycler_status.setAdapter(storeisAdapter);
          fab_add_story.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        addStory();
    }
});
    }

    @Override
    public void onResume() {
        super.onResume();

                getFriends();
    }

    @Override
    public void onPause() {
        super.onPause();
     /*   if (updateStories != null && mContext != null)
            mContext.unregisterReceiver(updateStories);
    */}


    private void getFriends() {
        if (recycler_status == null)
            return;
        keys.clear();
        storiesList.clear();
        progressbar.setVisibility(View.VISIBLE);
        if (statusRef == null)
            statusRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if (snap.getKey() != null)
                                keys.add(snap.getKey());
                        }
                        getStatusOfFriend(0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressbar.setVisibility(View.GONE);

                    }
                });
    }

    DatabaseReference statusRef;

    private void getStatusOfFriend(int i) {

        if (statusRef == null)
            statusRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);
        if (i >= keys.size()) {
            progressbar.setVisibility(View.GONE);
            storeisAdapter.notifyDataSetChanged();
            return;
        }
        String key = keys.get(i);

        Log.d(TAG, "getStatusOfFriend: " + key);
        StoriesOwnerModel ownerModel = new StoriesOwnerModel();
        ownerModel.setUserKey(key);
        int finalI = i + 1;
        statusRef.child(key).child("statusUser").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<StoriesModel> models = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    StoriesModel model = snap.getValue(StoriesModel.class);
                    if (model != null) {
                        models.add(model);
                        ownerModel.setStoriesModel(models);
                    }
                }
                if (dataSnapshot.getChildrenCount() != 0) {
                    storiesList.add(ownerModel);
                }
                getStatusOfFriend(finalI);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                getStatusOfFriend(finalI);
            }
        });
    }

    @OnClick(R.id.fab_add_story2)
    void addStory2() {
        if (checkFilesPermission()) {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        }
    }

    @OnClick(R.id.fab_add_story)
    void addStory() {
        if (checkPermission()) {
            if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                File mediaFile = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/story.mp4");
                videoUri = Uri.fromFile(mediaFile);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory().getPath() + "story.mp4");
                startActivityForResult(intent, REQUEST_RECORD_VIDEO);
            } else {
                Toast.makeText(mContext, getResources().getString(R.string.no_camera), Toast.LENGTH_LONG).show();
            }
        }
    }

    boolean checkFilesPermission() {
        if (ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(mContext)
                        .setMessage(getResources().getString(R.string.allow_access_files))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(getResources().getString(R.string.goto_settings), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getActivity().getPackageName(), null)));

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return false;
            } else {
                ActivityCompat
                        .requestPermissions(
                                getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                123);
                return false;
            }
        }
    }

    boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                mContext, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(mContext)
                        .setTitle(getResources().getString(R.string.allow_access_camera))
                        .setMessage(getResources().getString(R.string.allow_camera_descrip))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(getResources().getString(R.string.goto_settings), new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getActivity().getPackageName(), null)));

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return false;
            } else {
                ActivityCompat
                        .requestPermissions(
                                getActivity(),
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                                123);
                return false;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECORD_VIDEO || requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.uploading_status), Toast.LENGTH_SHORT).show();
                videoUri = data.getData();
/*
                Intent intent1 = new Intent(mContext, UploadStatusServices.class);
                intent1.putExtra("uri", "" + videoUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    mContext.startForegroundService(intent1);
                else
                    ContextCompat.startForegroundService(mContext, intent1);*/
                Intent intent1 = new Intent(mContext, Preview_Video_A.class);
                intent1.putExtra("uri", "" + videoUri);
                startActivity(intent1);
            }
        }
    }

}