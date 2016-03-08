package com.XeliteXirish.NetworkKeySharer.model;

import android.net.wifi.WifiConfiguration;

import java.io.Serializable;

public class Network implements Serializable {
    private String ssid;
    private String key;
    private AuthType authType;
    private boolean isHidden;

    public Network(String ssid, AuthType authType, String key, boolean isHidden) {
        this.ssid = ssid;
        this.key = key;
        this.authType = authType;
        this.isHidden = isHidden;
    }

    public String getSsid() {
        return ssid;
    }

    public String getKey() {
        return key;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public boolean isPasswordProtected() {
        return authType == AuthType.WPA_PSK
                || authType == AuthType.WPA2_PSK
                || authType == AuthType.WEP
                || !key.isEmpty();
    }

    public boolean needsPassword() {
        return isPasswordProtected() && key.isEmpty();
    }

    public static Network fromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        String ssid = getSsidFromWifiConfiguration(wifiConfiguration);
        AuthType authType = getSecurityFromWifiConfiguration(wifiConfiguration);
        String key = "";
        boolean isHidden = wifiConfiguration.hiddenSSID;

        return new Network(ssid, authType, key, isHidden);
    }

    private static String getSsidFromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        String ssid = wifiConfiguration.SSID;
        if (ssid != null) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                return ssid.substring(1, ssid.length() - 1);
            } else {
                return ssid;
            }
        }
        return "";
    }

    private static AuthType getSecurityFromWifiConfiguration(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            if (wifiConfiguration.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                return AuthType.WPA2_PSK;
            } else {
                return AuthType.WPA_PSK;
            }
        }
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            if (wifiConfiguration.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
                return AuthType.WPA2_EAP;
            } else {
                return AuthType.WPA_EAP;
            }
        }
        return (wifiConfiguration.wepKeys[0] != null) ? AuthType.WEP : AuthType.OPEN;
    }

    @Override
    public String toString() {
        return "Network{" +
                ", ssid='" + ssid + '\'' +
                ", key='" + key + '\'' +
                ", authType=" + authType +
                ", isHidden=" + isHidden +
                '}';
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static boolean isValidKeyLength(AuthType authType, String key)
            throws NetworkException {
        int keyLength = key.length();

        if (authType == AuthType.WEP) {
            if (keyLength != 5 && keyLength != 13) {
                throw new NetworkException(NetworkException.WEP_KEY_LENGTH_ERROR);
            }
        } else { // WPA
            if ((keyLength >= 5 && keyLength < 8) || keyLength > 63) {
                throw new NetworkException(NetworkException.WPA_KEY_LENGTH_ERROR);
            }
        }

        return true;
    }
}
