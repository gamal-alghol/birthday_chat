package com.fadfadah.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fadfadah.app.R;
import com.fadfadah.app.callback.DataBaseTablesConstants;
import com.fadfadah.app.models.PostsModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

import static com.fadfadah.app.helper.Helper.getCurrentTimeMilliSecond;

public class UploadPostServices extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("uploading status")
                .setSmallIcon(R.drawable.profile_img)
                .build();
        startForeground(1, notification);


        Uri file = Uri.parse(intent.getStringExtra("uri"));
        String text = intent.getStringExtra("text");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("videos/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

//
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                // ...
//
//
//            }
//        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()) {
//
//                 } else {
//                    // Handle failures
//                    // ...
//                }
//            }
//        });
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                addPost(text, null, task.getResult().toString());
                if (task.isSuccessful()) {
                    Toast.makeText(UploadPostServices.this, "sucess", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(UploadPostServices.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failures
                    // ...
                }
            }
        });

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    private void addPost(String text, String imageUrl, String videoUrl) {
        if (text.isEmpty() && imageUrl == null && videoUrl == null)
            return;
//        String postKey, String userKey, String imageUrl, String postContent,
//                String videoUrl, long postTimeInMillis,int postPrivacy
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.POSTS);
        String postKey = postReference.push().getKey();
        PostsModel postsModel =
                new PostsModel(postKey, String.valueOf(FirebaseAuth.getInstance().getUid()), imageUrl,
                        text, videoUrl,
                        getCurrentTimeMilliSecond(),
                        0);
        postReference.child(postKey).setValue(postsModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                stopSelf();
            }
        });

    }


}
