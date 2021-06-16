package com.fadfadah.app.models;

import java.util.List;

public class StoriesOwnerModel {
    private String userKey;
    private List<StoriesModel> storiesModel;

    public String lastVideo() {
        if (storiesModel == null || storiesModel.size() == 0)
            return "";
        return storiesModel.get(storiesModel.size() - 1).getVideo();
    }

    public long lastTime() {
        if (storiesModel == null || storiesModel.size() == 0)
            return -1;
        return storiesModel.get(storiesModel.size() - 1).getTimeStamp();
    }

    public int numberOfStories() {
        if (storiesModel == null)
            return 1;
        return storiesModel.size()+1;
    }

    public String getUserKey() {
        if (userKey == null)
            return "";
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public List<StoriesModel> getStoriesModel() {
        return storiesModel;
    }

    public void setStoriesModel(List<StoriesModel> storiesModel) {
        this.storiesModel = storiesModel;
    }
}
