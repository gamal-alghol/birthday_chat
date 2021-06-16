package com.fadfadah.app.models;

public class ChatInboxModel {
    private String chatId;
    private String fromId;
    private String lastMessage;
    private int status;
    private long timeStamp;
    public String name;
    public String userToken;
    public String userImage;

    public ChatInboxModel() {
    }

    public ChatInboxModel(String chatId, String fromId, String lastMessage, int status, long timeStamp) {
        this.chatId = chatId;
        this.fromId = fromId;
        this.lastMessage = lastMessage;
        this.status = status;
        this.timeStamp = timeStamp;
    }




    public String getChatId() {
        if (chatId == null)
            return "";
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getFromId() {
        if (fromId == null)
            return "";
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getLastMessage() {
        if (lastMessage == null)
            return "";
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    public boolean isValid() {
        return !(fromId == null || fromId.isEmpty() || lastMessage == null ||  chatId == null || chatId.isEmpty());
    }
}
