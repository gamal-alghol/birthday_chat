package com.fadfadah.app.models;

import java.io.Serializable;

public class StoriesModel implements Serializable {
    private String storyKey;
    private String fromID;
    private String video;
    private String videoName;
    private long timeStamp;
    private int filterPosition;

    public StoriesModel() {
    }

    public StoriesModel(String storyKey, String fromID, String video, String videoName, long timeStamp) {
        this.storyKey = storyKey;
        this.fromID = fromID;
        this.video = video;
        this.videoName = videoName;
        this.timeStamp = timeStamp;
    }
    public StoriesModel(String storyKey, String fromID, String video, String videoName, long timeStamp,int filterPosition) {
        this.storyKey = storyKey;
        this.fromID = fromID;
        this.video = video;
        this.videoName = videoName;
        this.timeStamp = timeStamp;
        this.filterPosition=filterPosition;
    }

    public String getVideoName() {
        if (videoName == null)
            return "";
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getStoryKey() {
        if (storyKey == null)
            return "";
        return storyKey;
    }

    public void setStoryKey(String storyKey) {
        this.storyKey = storyKey;
    }

    public String getFromID() {
        if (fromID == null)
            return "";
        return fromID;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public String getVideo() {
        if (video == null)
            return "";
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getFilterPosition() {
        return filterPosition;
    }

    public void setFilterPosition(int filterPosition) {
        this.filterPosition = filterPosition;
    }
}
