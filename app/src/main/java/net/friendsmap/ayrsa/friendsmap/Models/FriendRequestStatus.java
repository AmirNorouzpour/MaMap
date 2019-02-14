package net.friendsmap.ayrsa.friendsmap.Models;

import com.google.gson.annotations.SerializedName;

public enum FriendRequestStatus {
    @SerializedName("0")
    Pending(0),
    @SerializedName("1")
    Accepted(1),
    @SerializedName("2")
    Deleted(2);

    FriendRequestStatus(int friendRequestStatus) {
        this.friendRequestStatus = friendRequestStatus;
    }

    private final int friendRequestStatus;

}


