package ir.mamap.app.Models;

import com.google.gson.annotations.SerializedName;

public class SystemLog {
    @SerializedName("AppVersion")
    private String appVersion;
    @SerializedName("Ip")
    private String ip;
    @SerializedName("Exception")
    private String exception;
    @SerializedName("Logger")
    private String logger;
    @SerializedName("Callsite")
    private String callSite;

    public void setCallSite(String callSite) {
        this.callSite = callSite;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }
}
