package com.XeliteXirish.NetworkKeySharer.model;

public enum AuthType {
    OPEN("Open"),
    WEP("WEP"),
    WPA_PSK("WPA PSK"),
    WPA_EAP("WPA EAP"),
    WPA2_EAP("WPA2 EAP"),
    WPA2_PSK("WPA2 PSK");

    private final String printableName;

    AuthType(String printableName) {
        this.printableName = printableName;
    }

    @Override
    public String toString() {
        return printableName;
    }
}
