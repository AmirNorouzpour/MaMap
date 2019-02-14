package net.friendsmap.ayrsa.friendsmap.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("Mobile")
    private String mobile;
    @SerializedName("NickName")
    private String nickName;
    @SerializedName("Friend")
    private FriendMap friend;
    public FriendMap getFriend() {
        return friend;
    }
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @SerializedName("Id")
    private int userId;
    @SerializedName("UserName")
    private String username;

    @SerializedName("Password")
    private String password;

    public String getEmail() {
        return email;
    }
    @SerializedName("NotAvailable")
    private boolean notAvailable;

    public boolean isNotAvailable() {
        return notAvailable;
    }

    @SerializedName("Email")
    private String email;

    public String getExpDateText() {
        return expDateText;
    }

    public void setExpDateText(String expDateText) {
        this.expDateText = expDateText;
    }

    @SerializedName("ExpireDateTimeStr")
    private String expDateText;

}