package net.friendsmap.ayrsa.friendsmap.Models;


public class DeviceInformation {
    private String Token;
    private String Ip;
    private String DeviceName;
    private String Os;
    private String InstalledAppVersion;
    private String DeviceId;

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    public String getDeviceName() {
        return DeviceName;
    }

    public void setDeviceName(String deviceName) {
        DeviceName = deviceName;
    }

    public String getOs() {
        return Os;
    }

    public void setOs(String os) {
        Os = os;
    }

    public String getInstalledAppVersion() {
        return InstalledAppVersion;
    }

    public void setInstalledAppVersion(String installedAppVersion) {
        InstalledAppVersion = installedAppVersion;
    }

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        DeviceId = deviceId;
    }


}