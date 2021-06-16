package com.fadfadah.app.models;

public class PostsModel {
    private String postKey = "";
    private String userKey = "";
    private String imageUrl = null;
    private String postContent = null;
    private String videoUrl = null;
    private long postTimeInMillis;

    // 0 is private , 1 is public
    private int postPrivacy = 0;
    public String userToken = null;

    public PostsModel() {
    }

    public PostsModel(String postKey, String userKey, String imageUrl, String postContent,
                      String videoUrl, long postTimeInMillis, int postPrivacy) {
        this.postKey = postKey;
        this.userKey = userKey;
        this.imageUrl = imageUrl;
        this.postContent = postContent;
        this.videoUrl = videoUrl;
        this.postTimeInMillis = postTimeInMillis;

        this.postPrivacy = postPrivacy;
    }


    public String getPostKey() {
        if (postKey == null)
            return "";
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getUserKey() {
        if (userKey == null)
            return "";
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostContent() {
        if (postContent == null)
            return "";
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getVideoUrl() {

        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public long getPostTimeInMillis() {
        return postTimeInMillis;
    }

    public void setPostTimeInMillis(long postTimeInMillis) {
        this.postTimeInMillis = postTimeInMillis;
    }

    public int getPostPrivacy() {
        return postPrivacy;
    }

    public void setPostPrivacy(int postPrivacy) {
        this.postPrivacy = postPrivacy;
    }

    public boolean isValid() {
        return !(
                userKey == null || userKey.isEmpty() || postKey == null || postKey.isEmpty()
        );
    }
}
