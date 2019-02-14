package net.friendsmap.ayrsa.friendsmap.Models;


import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ClientDataNonGeneric {
    @SerializedName("Msg")
    private String Msg;
    @SerializedName("EntityId")
    private int EntityId;
    @SerializedName("Type")
    private OutType Type;

    public void setTag(Object tag) {
        Tag = tag;
    }

    @SerializedName("Tag")
    private Object Tag;
    @SerializedName("TagList")
    private ArrayList<Object> TagList;

    public ArrayList<Object> getTagList() {
        return TagList;
    }

    public void setTagList(ArrayList<Object> tagList) {
        TagList = tagList;
    }

    public String getMsg() {
        return Msg;
    }

    public int getEntityId() {
        return EntityId;
    }

    public OutType getOutType() {
        return Type;
    }

    public Object getTag() {
        return Tag;
    }

}