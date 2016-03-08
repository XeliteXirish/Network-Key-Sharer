package com.XeliteXirish.NetworkKeySharer.utils;

import java.util.ArrayList;
import java.util.List;

import com.XeliteXirish.NetworkKeySharer.model.AuthType;
import com.XeliteXirish.NetworkKeySharer.model.Network;

public class WpaSupplicantParser {

    private static final String TAG = WpaSupplicantParser.class.getSimpleName();

    public static List<Network> parse(String networkString) {
        List<Network> networks = new ArrayList<>();
        String[] networkSections = networkString.split("network=");
        for (int i = 1; i < networkSections.length; i++) {
            String networkSection = networkSections[i];
            String name = networkName(networkSection);
            AuthType authType = networkType(networkSection);
            String password = networkPassword(networkSection);
            networks.add(new Network(name, authType, password, false));
        }

        return networks;
    }

    private static String networkName(String networkSection) {
        if (hasToken(networkSection, "ssid")) {
            return parseToken(networkSection, "ssid");
        }

        return "";
    }

    private static AuthType networkType(String networkSection) {
        if (hasToken(networkSection, "wep_key0")) {
            return AuthType.WEP;
        } else if (hasToken(networkSection, "psk")) {
            return AuthType.WPA2_PSK;
        } else if (hasToken(networkSection, "key_mgmt")
                && parseToken(networkSection, "key_mgmt").startsWith("WPA-EAP")) {
            return AuthType.WPA2_EAP;
        } else {
            return AuthType.OPEN;
        }
    }

    private static String networkPassword(String networkSection) {
        switch (networkType(networkSection)) {
            case WPA_PSK:
            case WPA2_PSK:
                return parseToken(networkSection, "psk");
            case WEP:
                return parseToken(networkSection, "wep_key0");
            default:
                return "";
        }
    }

    private static boolean hasToken(String networkSection, String tokenName) {
        return tokenLines(networkSection, tokenName).size() > 0;
    }

    private static List<String> tokenLines(String networkSection, String tokenName) {
        List<String> lines = new ArrayList<>();
        String[] tokenLines = networkSection.split("\n");
        for (String line : tokenLines) {
            if (line.trim().startsWith(tokenName)) {
                lines.add(line.trim());
            }
        }

        return lines;
    }

    private static String parseToken(String networkSection, String tokenName) {
        if (hasToken(networkSection, tokenName)) {
            List<String> tokenLines = tokenLines(networkSection, tokenName);
            return tokenLines.get(0).split("=")[1].replace("\"", "");
        }

        return "";
    }
}
