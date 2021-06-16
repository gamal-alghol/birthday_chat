package com.fadfadah.app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fadfadah.app.retrofit.MessageResponse;
import com.fadfadah.app.retrofit.RetrofitClient;
import com.fadfadah.app.retrofit.RetrofitServices;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {
    public static void sendNotification(Context mContext, String token, String body, String CLICK_ACTION
    ,String postKey,String  chatKey,String userKey) {
        SharedPreferences sharedEdit = mContext.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        JsonObject map = new JsonObject();
        try {
            map.addProperty("to", token);
            map.addProperty("priority", "high");
            JsonObject notification = new JsonObject();
            notification.addProperty("title", sharedEdit.getString("getFirstName", "someone"));
            notification.addProperty("body", body);
            notification.addProperty("click_action", CLICK_ACTION);
            JsonObject data = new JsonObject();
            data.addProperty(Constant.POST_KEY, postKey);
            data.addProperty(Constant.CHAT_KEY, chatKey);
            data.addProperty(Constant.USER_KEY, userKey);
            map.add("notification", notification);
            map.add("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RetrofitClient.inialize().create(RetrofitServices.class).sendNotification(map).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                Log.d("sendNotification", "onResponse: " + response.body().success);
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.d("sendNotification", "onFailure: " + t.getMessage());
            }
        });
    }
}
