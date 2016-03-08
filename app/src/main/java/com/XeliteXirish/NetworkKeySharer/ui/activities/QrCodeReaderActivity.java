package com.XeliteXirish.NetworkKeySharer.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.XeliteXirish.NetworkKeySharer.R;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import java.util.List;

public class QrCodeReaderActivity extends Activity implements QRCodeReaderView.OnQRCodeReadListener{

    public QRCodeReaderView qrCodeReaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        this.qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrReaderView);
        this.qrCodeReaderView.setOnQRCodeReadListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.getCameraManager().startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.getCameraManager().stopPreview();
    }

    @Override
    public void onQRCodeRead(String encoded, PointF[] points) {
        connectToWifi(getDataFromQr(encoded));
    }

    @Override
    public void cameraNotFound() {

    }

    @Override
    public void QRCodeNotFoundOnCamImage() {

    }

    // WIFI:T:WPA;S:mynetwork;P:mypass;;
    public String[] getDataFromQr(String encoded){
        String[] details = new String[3];

        String[] parts = encoded.split(";");

        //Auth
        String[] authSplit = parts[0].split(":");
        details[0] = authSplit[2];

        //Password
        String[] passwordSplit = parts[1].split(":");
        details[2] = passwordSplit[1];

        //SSID
        String[] ssidSplit = parts[2].split(":");
        details[1] = ssidSplit[1];

        return details;
    }

    public void connectToWifi(String[] details){
        Toast.makeText(this, "Starting to connect", Toast.LENGTH_SHORT).show();

        String networkAuth = details[0];
        String networkSSID = details[1];
        String networkPassword = details[2];

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + networkSSID + "\"";

        if(networkAuth.equalsIgnoreCase("WEP")) {
            wifiConfig.wepKeys[0] = "\"" + networkPassword + "\"";
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        }else if(networkAuth.equalsIgnoreCase("WPA")) {
            wifiConfig.preSharedKey = "\"" + networkPassword + "\"";

        }else if(networkAuth.equalsIgnoreCase("none")) {
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(wifiConfig);

        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration i : list){
            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")){
                Toast.makeText(this, R.string.connecting_to_wifi, Toast.LENGTH_SHORT).show();
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }
    }
}
