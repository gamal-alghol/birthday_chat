package com.fadfadah.app.models;

public class SingleMessageObject {
    private String senderID;
    private String text;
    private int status;
    private long timeStamp;
    private String messageKey;
    private String audioUrl;
    private String imageUrl;

    public SingleMessageObject(String senderID, String text, int status, long timeStamp, String messageKey, String audioUrl, String imageUrl) {
        this.senderID = senderID;
        this.text = text;
        this.status = status;
        this.timeStamp = timeStamp;
        this.messageKey = messageKey;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public SingleMessageObject() {
    }

    public String getSenderID() {
        if (senderID == null)
            return "";
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        if (text == null)
            return "";
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
