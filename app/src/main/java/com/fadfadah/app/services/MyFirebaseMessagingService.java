package com.fadfadah.app.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fadfadah.app.R;
import com.fadfadah.app.activity.HomeActivity;
import com.fadfadah.app.helper.Constant;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static String chatForUserId = "";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
//data={"title":"mohamed mohamed","body":"تست","type":"COMMENT"}
            try {

                if (remoteMessage.getNotification().getClickAction().equalsIgnoreCase("CHAT")) {
                    if (!remoteMessage.getData().get(Constant.USER_KEY).equalsIgnoreCase(chatForUserId))
                        sendNotification(remoteMessage.getNotification().getTitle(),
                                remoteMessage.getNotification().getBody()
                                , remoteMessage.getData().get(Constant.USER_KEY),
                                remoteMessage.getData().get(Constant.CHAT_KEY),"");
                } else if (remoteMessage.getNotification().getClickAction().equalsIgnoreCase("FRIEND")) {
                    sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),
                             remoteMessage.getData().get(Constant.USER_KEY), "","");
                }  else if (remoteMessage.getNotification().getClickAction().equalsIgnoreCase("NOTIFICATION")) {
                    sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),
                            remoteMessage.getData().get(Constant.USER_KEY)
                            ,"", remoteMessage.getData().get(Constant.POST_KEY));
                }

//
//
//                else if (remoteMessage.getData().get("type").toLowerCase().contains("comment"))
//                    sendNotification(remoteMessage.getData().get("title") + " " + getResources().getString(R.string.commented_on_your_post),
//                            remoteMessage.getData().get("body"), remoteMessage.getData().get("type"),
//                            remoteMessage.getData().get("id"), "");
//                else if (remoteMessage.getData().get("type").toLowerCase().contains("like_comment"))
//                    sendNotification(remoteMessage.getData().get("title"),
//                            getResources().getString(R.string.likes_your_comment), remoteMessage.getData().get("type")
//                            , remoteMessage.getData().get("id"), "");
//                else if (remoteMessage.getData().get("type").toLowerCase().contains("like"))
//                    sendNotification(remoteMessage.getData().get("title"),
//                            getResources().getString(R.string.likes_your_post), remoteMessage.getData().get("type"),
//                            remoteMessage.getData().get("id"), "");
//                else if (remoteMessage.getData().get("type").toLowerCase().contains("friend"))
//                    sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"),
//                            remoteMessage.getData().get("type")
//                            , remoteMessage.getData().get("id"), "");

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onMessageReceived: " + e.getMessage());
//                sendNotification(remoteMessage.getData().get("title"),
//                        remoteMessage.getData().get("body"), "CHAT", "", "");

            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
//            sendNotification("dddd");

        }

    }


    private void sendNotification(String title, String body,
                                  String userID, String chat,String postKey) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constant.USER_KEY, userID);
        intent.putExtra(Constant.CHAT_KEY, chat);
        intent.putExtra(Constant.POST_KEY, postKey);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);


        String channelId = "default_notification_channel_id";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.profile_img)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}