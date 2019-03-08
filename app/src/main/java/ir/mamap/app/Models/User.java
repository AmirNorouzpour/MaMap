package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    @SerializedName("Mobile")
    private String mobile;
    @SerializedName("NickName")
    private String nickName;
    @SerializedName("Friend")
    private FriendMap friend;
    @SerializedName("Id")
    private int userId;
    @SerializedName("UserName")
    private String username;
    @SerializedName("Password")
    private String password;
    @SerializedName("NotAvailable")
    private boolean notAvailable;
    @SerializedName("Email")
    private String email;
    @SerializedName("ExpireDateTimeStr")
    private String expDateText;


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
    public String getExpDateText() {
        return expDateText;
    }
    public String getEmail() {
        return email;
    }
    public boolean isNotAvailable() {
        return notAvailable;
    }


}