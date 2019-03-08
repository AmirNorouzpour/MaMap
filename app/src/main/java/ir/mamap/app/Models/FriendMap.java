package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FriendMap implements Serializable {
    @SerializedName("NickName")
    private String nickName;
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;
    @SerializedName("Speed")
    private double speed;
    @SerializedName("Seen")
    private String seen;
    private FriendRequestStatus Status;
    private Boolean IsBlocked;
    @SerializedName("Id")
    private int userId;
    @SerializedName("FileName")
    private String fileName;
    @SerializedName("NotAvailable")
    private boolean notAvailable;

    public double getSpeed() {
        return speed;
    }

    public boolean isNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(boolean notAvailable) {
        this.notAvailable = notAvailable;
    }

    public String getFileName() {
        return fileName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }



    public String getSeen() {
        return seen;
    }

    public FriendRequestStatus getStatus() {
        return Status;
    }

    public void setStatus(FriendRequestStatus status) {
        Status = status;
    }

    public Boolean getBlocked() {
        return IsBlocked;
    }

    public void setBlocked(Boolean blocked) {
        IsBlocked = blocked;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public FriendMap(String nickName, double latitude, double longitude) {
        this.nickName = nickName;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getNickName() {
        return nickName;
    }

}
