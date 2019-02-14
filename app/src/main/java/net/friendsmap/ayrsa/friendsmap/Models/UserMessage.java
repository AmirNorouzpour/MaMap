package net.friendsmap.ayrsa.friendsmap.Models;

import com.google.gson.annotations.SerializedName;

public class UserMessage {
    @SerializedName("Id")
    private int id;
    @SerializedName("Tag")
    private int tag;
    @SerializedName("Seen")
    private boolean seen;
    @SerializedName("Body")
    private String body;
    @SerializedName("Title")
    private String title;
    @SerializedName("InsertDateTime")
    private String insertDateTime;
    @SerializedName("SenderUserId")
    private int senderUserId;
    @SerializedName("HideButtons")
    private boolean hideButtons;

    public boolean getHideButtons() {
        return hideButtons;
    }


    public int getId() {
        return id;
    }

    public int getTag() {
        return tag;
    }

    public String getExpireDateTime() {
        return expireDateTime;
    }

    public void setExpireDateTime(String expireDateTime) {
        this.expireDateTime = expireDateTime;
    }

    @SerializedName("ExpireDateTime")
    private String expireDateTime;

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInsertDateTime() {
        return insertDateTime;
    }

    public void setInsertDateTime(String insertDateTime) {
        this.insertDateTime = insertDateTime;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(int senderUserId) {
        this.senderUserId = senderUserId;
    }
}
