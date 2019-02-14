package net.friendsmap.ayrsa.friendsmap.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserDataMenu implements Serializable {
    @SerializedName("Ttile")
    private String title;
    @SerializedName("Value")
    private String value;
    @SerializedName("Editable")
    private Boolean editable;
    @SerializedName("Valid")
    private Boolean valid;
    @SerializedName("DataType")
    private int dataType;


    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getDataType() {
        return dataType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }
}
