package com.fadfadah.app.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.helper.SharedPereferenceClass;
import com.fadfadah.app.models.PostsModel;
import com.fadfadah.app.models.UsersModel;
import com.fadfadah.app.services.UploadPostServices;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddNewPostActivity extends AppCompatActivity {
    @BindView(R.id.civ_profile_img)
    CircleImageView circleImageView;
    @BindView(R.id.tv_username)
    TextView tv_username;
    @BindView(R.id.spinner_privacy)
    Spinner spinner_privacy;
    @BindView(R.id.edt_post_text)
    EditText edt_post_text;
    private DatabaseReference userRef;
    private Uri videoUri;
    @BindView(R.id.ivPost)
    ImageView ivPost;
    private Uri imageUri = null;
    ProgressDialog dialog;
    @BindView(R.id.iv_cancel)
    ImageView iv_cancel;
    @BindView(R.id.iv_photo)
    ImageView iv_photo;
    @BindView(R.id.iv_video)
    ImageView iv_video;

    @OnClick(R.id.iv_cancel)
    void cancel() {
        iv_cancel.setVisibility(View.GONE);
        ivPost.setImageBitmap(null);
        iv_photo.setVisibility(View.VISIBLE);
        iv_video.setVisibility(View.VISIBLE);
        videoUri = null;
        imageUri = null;
    }

    private void selectLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @OnClick(R.id.btn_post)
    void addPost() {
        if (imageUri == null && videoUri == null) {
            addPost(edt_post_text.getText().toString(),
                    null, null);
            edt_post_text.setText("");
        } else if (imageUri != null) {
            uploadImage();
        } else if (videoUri != null) {
            Intent intent1 = new Intent(getApplicationContext(), UploadPostServices.class);
            intent1.putExtra("uri", "" + videoUri);
            intent1.putExtra("text", "" + edt_post_text.getText().toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent1);
            else
                ContextCompat.startForegroundService(getApplicationContext(), intent1);
            finish();
        }
    }

    private void startLoading() {
        dialog = ProgressDialog.show(this, "",
                getResources().getString(R.string
                        .loading_wait), true);
        dialog.setCancelable(false);
    }

    private void stopLoading() {
        dialog.dismiss();
        dialog = null;
    }

    private void uploadImage() {
        startLoading();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("images/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(imageUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    stopLoading();
                    task.getException().printStackTrace();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    addPost(edt_post_text.getText().toString(), task.getResult().toString(), null);
                    edt_post_text.setText("");
                    stopLoading();
                } else {
                    Toast.makeText(AddNewPostActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...
                    stopLoading();
                }
            }
        });
    }

    public static String[] extractLinks(String text) {
        List<String> links = new ArrayList<String>();
        Matcher m = Patterns.WEB_URL.matcher(text);
        while (m.find()) {
            String url = m.group();
//            Log.d(TAG, "URL extracted: " + url);
            links.add(url);
        }

        return links.toArray(new String[links.size()]);
    }

    private void addPost(String text, String imageUrl, String videoUrl) {
        if (text.isEmpty() && imageUrl == null && videoUrl == null)
            return;
//        String postKey, String userKey, String imageUrl, String postContent,
//                String videoUrl, long postTimeInMillis,int postPrivacy

//        if (text.toLowerCase().contains("youtu")) {
//            String[] link = extractLinks(text);
//            if (link != null && link.length > 0)
//                new YouTubeExtractor(this) {
//                    @Override
//                    public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
//                        if (ytFiles != null) {
//                            int itag = 22;
//                            String downloadUrl = ytFiles.get(itag).getUrl();
//                            uploadContent(text, imageUrl, downloadUrl);
//                        }
//                    }
//                }.extract(link[0], true, true);
//            else uploadContent(text, imageUrl, videoUrl);
//        } else
        uploadContent(text, imageUrl, videoUrl);

    }

    private void uploadContent(String text, String imageUrl, String videoUrl) {
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        String postKey = postReference.push().getKey();
        PostsModel postsModel =
                new PostsModel(postKey, String.valueOf("" + FirebaseAuth.getInstance().getUid()), imageUrl,
                        text, videoUrl,
                        Helper.getCurrentTimeMilliSecond(),
                        spinner_privacy.getSelectedItemPosition());
        postReference.child(postKey).setValue(postsModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });
    }

    boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.allow_access_camera))
                        .setMessage(getResources().getString(R.string.allow_camera_descrip))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("go to settings", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null)));
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return false;
            } else {

                ActivityCompat
                        .requestPermissions(
                                this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                                123);
                return false;
            }
        }
    }


    SharedPereferenceClass sharedPereferenceClass = new SharedPereferenceClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectLanguage(sharedPereferenceClass.getLng(getApplicationContext()));
        setContentView(R.layout.activity_add_new_post);
        ButterKnife.bind(this);

        spinner_privacy.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_expandable_list_item_1, getResources().getStringArray(R.array.privacyitems)));
        loadUser(circleImageView, tv_username, String.valueOf(FirebaseAuth.getInstance().getUid()));
    }

    private void loadUser(CircleImageView civ_profile_img, TextView tv_username, String userKey) {
        if (userKey == null)
            return;
        if (userRef == null)
            userRef = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.USERS);
        userRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {
                    UsersModel usersModel = dataSnapshot.getValue(UsersModel.class);
                    if (usersModel != null) {
                        assert usersModel.getImage() != null;
                        assert !usersModel.getImage().isEmpty();
                        try {
                            Glide.with(getApplicationContext()).load(usersModel.getImage())
                                    .placeholder(R.drawable.profile_img).into(civ_profile_img);
                        } catch (Exception e) {
                            civ_profile_img.setImageResource(R.drawable.profile_img);
                        }
                        tv_username.setText(usersModel.getFirstName() + " " + usersModel.getLastName());
                        if (usersModel.getPrivacy() == 1)
                            spinner_privacy.setSelection(1);
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

    @OnClick(R.id.iv_video)
    void pickVideo() {
//        SandriosCamera
//                .with()
//                .setShowPicker(true)
//                .setVideoFileSize(20)
//                .setMediaAction(CameraConfiguration.MEDIA_ACTION_VIDEO)
//                .enableImageCropping(true)
//                .launchCamera(this);
        if (checkPermission()) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                File mediaFile = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");

                videoUri = Uri.fromFile(mediaFile);

                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, Environment.getExternalStorageDirectory().getPath() + "videocapture_example.mp4");
                startActivityForResult(intent, 200);
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_camera), Toast.LENGTH_LONG).show();
            }
        }
//        @Override
//        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
//            super.onActivityResult(requestCode, resultCode, data);
//            if (resultCode == Activity.RESULT_OK
//                    && requestCode == SandriosCamera.RESULT_CODE
//                    && data != null) {
//                if (data.getSerializableExtra(SandriosCamera.MEDIA) instanceof Media) {
//                    Media media = (Media) data.getSerializableExtra(SandriosCamera.MEDIA);
//
//                    Log.e("File", "" + media.getPath());
//                    Log.e("Type", "" + media.getType());
//                    Toast.makeText(getApplicationContext(), "Media captured.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }

    }

    @OnClick(R.id.iv_photo)
    void pickImage() {
//        SandriosCamera
//                .with()
//                .setShowPicker(true)
//                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
//                .enableImageCropping(true)
//                .launchCamera(this);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_pic)), 214);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 214) {
            if (resultCode == RESULT_OK) {
                iv_cancel.setVisibility(View.VISIBLE);
                imageUri = data.getData();
                ivPost.setImageURI(data.getData());
                iv_video.setVisibility(View.GONE);
                videoUri = null;
            }
        } else if (requestCode == 200) {
            if (resultCode == RESULT_OK) {
                imageUri = null;
                iv_photo.setVisibility(View.GONE);
//                ivPost.setImageURI(data.getData());
                try {
                    Glide.with(this).load(data.getData()).into(ivPost);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                ivPost.setImageResource(R.drawable.video_thumbnail);
                iv_cancel.setVisibility(View.VISIBLE);
                Log.d("TAGTAG", "onActivityResult: " + data.getData());
                videoUri = data.getData();
            }
        }
    }
}