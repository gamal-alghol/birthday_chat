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
import com.fadfadah.app.helper.Helper;
import com.fadfadah.app.models.StoriesModel;
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

public class UploadStatusServices extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    int filterPosition;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(getString(R.string.uploading_status))
                .setSmallIcon(R.drawable.logo)
                .setSound(null)
                .build();
        startForeground(1, notification);

        Uri file = Uri.parse(intent.getStringExtra("uri"));
         filterPosition=intent.getIntExtra("position",0);


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference riversRef = storageRef.child("status/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);


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

                if (task.isSuccessful()) {
                    addStatus(task.getResult().toString(), file.getLastPathSegment());
                } else {
                    Toast.makeText(UploadStatusServices.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    private void addStatus(String videoUrl, String name) {
        if (videoUrl == null)
            return;

        //String storyKey, String fromID, String video, long timeStamp
        DatabaseReference postReference = FirebaseDatabase.getInstance().getReference(DataBaseTablesConstants.STATUS);
        String statusKey = postReference.push().getKey();
        StoriesModel status =
                new StoriesModel(statusKey, String.valueOf(FirebaseAuth.getInstance().getUid()),
                        videoUrl, name, Helper.getCurrentTimeMilliSecond(),filterPosition);
        

        postReference.child(String.valueOf(FirebaseAuth.getInstance().getUid())).child("statusUser").child(statusKey)
                .setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent intent1 = new Intent("fgsdgdfgg");
                sendBroadcast(intent1);
                stopSelf();

            }
        });

    }



}
