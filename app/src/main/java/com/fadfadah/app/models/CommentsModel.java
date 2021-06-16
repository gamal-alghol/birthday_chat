package com.fadfadah.app.models;

public class CommentsModel {
   private String commentKey = "";
    private String comment = "";
    private long commentTimeInMillis;
    private String commentedByUserKey = "";
  public String usertoken = "";

    public CommentsModel( String comment, long commentTimeInMillis, String commentedByUserKey) {

        this.comment = comment;
        this.commentTimeInMillis = commentTimeInMillis;
        this.commentedByUserKey = commentedByUserKey;
    }


    public CommentsModel(String commentKey, String comment, long commentTimeInMillis, String commentedByUserKey) {
        this.commentKey = commentKey;
        this.comment = comment;
        this.commentTimeInMillis = commentTimeInMillis;
        this.commentedByUserKey = commentedByUserKey;
    }

    public CommentsModel() {
    }

    public long getCommentTimeInMillis() {
        return commentTimeInMillis;
    }

    public void setCommentTimeInMillis(long commentTimeInMillis) {
        this.commentTimeInMillis = commentTimeInMillis;
    }

    public String getCommentKey() {
        if (commentKey == null)
            return "";
        return commentKey;
    }

    public void setCommentKey(String commentKey) {
        this.commentKey = commentKey;
    }

    public String getComment() {
        if (comment == null)
            return "";
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentedByUserKey() {
        if (commentedByUserKey == null)
            return "";
        return commentedByUserKey;
    }

    public void setCommentedByUserKey(String commentedByUserKey) {
        this.commentedByUserKey = commentedByUserKey;
    }


}
