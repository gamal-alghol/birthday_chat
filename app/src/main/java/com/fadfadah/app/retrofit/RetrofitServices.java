package com.fadfadah.app.retrofit;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitServices {

    @Headers({"Content-Type:application/json",
            "Authorization:Key=AAAAyusWD5g:APA91bHdSYSHGJ8UzVan04YM5bRmU6tgh9g27XeIAJqZS8A9Vy1-zbkR1QYFLosvvx298HggKRQ690CBM7sdp_IATkQGKjvG8D6VGF11PlXR8_XRxRIY-RrTJjMiA37lgBIr5tnodqxb"})
    @POST("fcm/send")
    Call<MessageResponse> sendNotification(@Body JsonObject message);

}
