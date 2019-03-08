package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

public class FriendResponse {
    @SerializedName("MapId")
    private int mapId;
    @SerializedName("MsgId")
    private int msgId;
    @SerializedName("Accept")
    private int accept;

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public void setAccept(int accept) {
        this.accept = accept;
    }
}
