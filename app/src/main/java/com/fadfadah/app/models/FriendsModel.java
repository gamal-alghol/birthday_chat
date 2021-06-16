package com.fadfadah.app.models;

public class FriendsModel {
    private String userKey = "";
    private long requestTimeMillisecond;
    private String userToken = "";

    public String getUserToken() {
        if (userToken == null)
            return "";
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public FriendsModel(String userKey, long requestTimeMillisecond) {
        this.userKey = userKey;
        this.requestTimeMillisecond = requestTimeMillisecond;
    }

    public FriendsModel() {
    }

    public String getUserKey() {
        if (userKey == null)
            return "";
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public long getRequestTimeMillisecond() {
        return requestTimeMillisecond;
    }

    public void setRequestTimeMillisecond(long requestTimeMillisecond) {
        this.requestTimeMillisecond = requestTimeMillisecond;
    }

    public boolean isValid() {
        return !(userKey == null || userKey.isEmpty());
    }
}
