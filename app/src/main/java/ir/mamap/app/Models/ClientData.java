package ir.mamap.app.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClientData<T> {
    @SerializedName("Msg")
    private String Msg;
    @SerializedName("EntityId")
    private int EntityId;
    @SerializedName("Type")
    private OutType Type;
    @Expose
    @SerializedName("Entity")
    private T Entity;

    public String getMsg() {
        return Msg;
    }

    public int getEntityId() {
        return EntityId;
    }

    public OutType getOutType() {
        return Type;
    }
    public T getEntity() {
        return Entity;
    }

}