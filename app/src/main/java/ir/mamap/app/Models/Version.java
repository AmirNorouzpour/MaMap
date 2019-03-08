package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

public class Version {
    @SerializedName("AppVersionName")
    private String appVersionName;
    @SerializedName("IsNecessary")
    private boolean isNecessary;
    @SerializedName("Link")
    private String link;
    @SerializedName("Changes")
    private String changes;
    @SerializedName("Message")
    private String mssage;
    @SerializedName("PackageName")
    private String packageName;

    public boolean isNecessary() {
        return isNecessary;
    }

    public void setNecessary(boolean necessary) {
        isNecessary = necessary;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    public String getAppVersionName() {
        return appVersionName;
    }

    public void setAppVersionName(String appVersionName) {
        this.appVersionName = appVersionName;
    }

    public boolean getIsNecessary() {
        return isNecessary;
    }

    public void setIsNecessary(boolean isNecessary) {
        this.isNecessary = isNecessary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public String getMssage() {
        return mssage;
    }

    public void setMssage(String mssage) {
        this.mssage = mssage;
    }
}
