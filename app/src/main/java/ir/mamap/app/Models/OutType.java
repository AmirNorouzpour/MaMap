package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

public enum OutType {
    @SerializedName("1")
    Success(1),
    @SerializedName("2")
    Error(2),
    @SerializedName("3")
    Warning(3);

    OutType(int outType) {
        this.outType = outType;
    }

    private final int outType;
}
