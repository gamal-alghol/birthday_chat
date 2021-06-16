package com.fadfadah.app.models;

import com.fadfadah.app.callback.NotificationType;

public class NotificationsModel {
    public String notificationKey = "";
    // 0 not seen , 1 seen
    public int notificationSeen = 0;
    public String notificationByUserKey = "";
    // 0 like , 1 comment
    public String postKey = "";
    public NotificationType type;
    public long notifcationTimeinMilli;

    public NotificationsModel(String notificationKey, int notificationSeen, String notificationByUserKey,
                              String postKey, NotificationType type, long notifcationTimeinMilli) {
        this.notificationKey = notificationKey;
        this.notificationSeen = notificationSeen;
        this.notificationByUserKey = notificationByUserKey;
        this.postKey = postKey;
        this.type = type;
        this.notifcationTimeinMilli = notifcationTimeinMilli;
    }

    public NotificationsModel() {
    }

    public String getNotificationKey() {
        if (notificationKey == null)
            return "";
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public int getNotificationSeen() {
        return notificationSeen;
    }

    public void setNotificationSeen(int notificationSeen) {
        this.notificationSeen = notificationSeen;
    }

    public String getNotificationByUserKey() {
        if (notificationByUserKey == null)
            return "";
        return notificationByUserKey;
    }

    public void setNotificationByUserKey(String notificationByUserKey) {
        this.notificationByUserKey = notificationByUserKey;
    }

    public String getPostKey() {
        if (postKey == null)
            return "";
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public long getNotifcationTimeinMilli() {
        return notifcationTimeinMilli;
    }

    public void setNotifcationTimeinMilli(long notifcationTimeinMilli) {
        this.notifcationTimeinMilli = notifcationTimeinMilli;
    }

    public boolean isValid() {
        return !(notificationByUserKey == null || notificationKey == null || type == null || postKey == null ||
                notificationByUserKey.isEmpty() || notificationKey.isEmpty() || postKey == null);
    }
}

