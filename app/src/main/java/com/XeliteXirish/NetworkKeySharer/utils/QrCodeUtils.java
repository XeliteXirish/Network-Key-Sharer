package com.XeliteXirish.NetworkKeySharer.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.XeliteXirish.NetworkKeySharer.model.AuthType;
import com.XeliteXirish.NetworkKeySharer.model.Network;

/**
 * Utility class containing functions to generate QR codes
 */
public class QrCodeUtils {

    /**
     * Generate a QR code containing the given Wi-Fi configuration
     *
     * @param width the width of the QR code
     * @param network the Wi-Fi configuration
     * @return a bitmap representing the QR code
     * @throws WriterException if the Wi-Fi configuration cannot be represented in the QR code
     */
    public static Bitmap generateWifiQrCode(int width, Network network) throws WriterException {
        int height = width;
        com.google.zxing.Writer writer = new QRCodeWriter();
        String wifiString = getWifiString(network);

        BitMatrix bitMatrix = writer.encode(wifiString, BarcodeFormat.QR_CODE, width, height);
        Bitmap imageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                imageBitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }

        return imageBitmap;
    }

    /**
     * Generate a Wi-Fi configuration string formatted as follows (proposed by ZXing):
     *
     *     WIFI:T:WPA;S:mynetwork;P:mypass;;
     *
     * See: https://github.com/zxing/zxing/wiki/Barcode-Contents#wifi-network-config-android
     *
     * @param network the Wi-Fi configuration to encode
     * @return the generated string encoding the Wi-Fi configuration
     */
    private static String getWifiString(Network network) {
        String ssid = network.getSsid();
        AuthType authType = network.getAuthType();
        String key = network.getKey();
        boolean isHidden = network.isHidden();

        StringBuilder output = new StringBuilder(100);
        output.append("WIFI:");
        output.append("T:");
        if (authType == AuthType.OPEN) {
            output.append("nopass");
        } else if (authType == AuthType.WEP) {
            output.append("WEP");
        } else {
            output.append("WPA");
        }
        output.append(";");
        maybeAppend(output, "P:", key);
        output.append("S:").append(ssid).append(';');
        if (isHidden) {
            maybeAppend(output, "H:", "true");
        }
        output.append(';');

        return output.toString();
    }

    private static void maybeAppend(StringBuilder output, String prefix, String value) {
        if (value != null && !value.isEmpty()) {
            output.append(prefix).append(value).append(';');
        }
    }
}
