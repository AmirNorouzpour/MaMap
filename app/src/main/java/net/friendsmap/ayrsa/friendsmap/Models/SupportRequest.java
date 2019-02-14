package net.friendsmap.ayrsa.friendsmap.Models;

import com.google.gson.annotations.SerializedName;

public class SupportRequest {
    @SerializedName("Ip")
    private String Ip;
    @SerializedName("UserId")
    private int userId;
    @SerializedName("AppVersion")
    private String appVersion;
    @SerializedName("AppPlatform")
    private int appPlatform;
    @SerializedName("Device")
    private String device;
    @SerializedName("Message")
    private String message;
    @SerializedName("Email")
    private String email;
    @SerializedName("RequestType")
    private int requestType;

    public void setIp(String ip) {
        Ip = ip;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setAppPlatform(int appPlatform) {
        this.appPlatform = appPlatform;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }


}
