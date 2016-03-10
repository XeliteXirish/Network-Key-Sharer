package com.XeliteXirish.NetworkKeySharer.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.XeliteXirish.NetworkKeySharer.R;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import java.util.List;

public class QrCodeReaderActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener{

    public QRCodeReaderView qrCodeReaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_reader);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        this.qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrReaderView);
        this.qrCodeReaderView.setOnQRCodeReadListener(this);

        //removeHome("EULXCKQY");
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
        showConfirmationBox(getDataFromQr(encoded));
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

    public void showConfirmationBox(final String[] details){
        String networkSSID = details[1];
        String networkPassword = details[2];
        String networkAuth = details[0];

        AlertDialog connectNetworkDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_connection_title_connect_to_network))
                .setView(R.layout.dialog_connect_to_network)
                .setPositiveButton(R.string.connected_to_wifi, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connectToWifi(details);
                    }
                })
                .setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        if(connectNetworkDialog.isShowing()) {

        }else{
            connectNetworkDialog.show();
        }

        TextView textViewSSID = (TextView) connectNetworkDialog.findViewById(R.id.textViewSSID);
        TextView textViewPassword = (TextView) connectNetworkDialog.findViewById(R.id.textViewPassword);
        TextView textViewAuth = (TextView) connectNetworkDialog.findViewById(R.id.textViewAuth);

        textViewSSID.setText(networkSSID);
        textViewPassword.setText(networkPassword);
        textViewAuth.setText(networkAuth);
    }

    public void connectToWifi(String[] details){ //TODO popups
        Toast.makeText(this, "Starting to connect", Toast.LENGTH_SHORT).show();

        String networkAuth = details[0];
        String networkSSID = details[1];
        String networkPassword = details[2];

        boolean duplicate = false;

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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

        for(int x = 0; x < wifiManager.getConfiguredNetworks().size(); x++){
            if(wifiManager.getConfiguredNetworks().get(x).SSID.equalsIgnoreCase("\"" + networkSSID + "\"")){
                Toast.makeText(this, "Network already added", Toast.LENGTH_SHORT).show();
                duplicate = true;
                return;
            }
        }
        if(!duplicate) {
            wifiManager.addNetwork(wifiConfig);
        }

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

    public void removeHome(String ssid){
        String newSSID = "\"" + ssid + "\"";
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networks = wifiManager.getConfiguredNetworks();
        for(int x = 0; x < networks.size(); x++){
            if(networks.get(x).SSID.equalsIgnoreCase(newSSID)){
                wifiManager.removeNetwork(networks.get(x).networkId);
            }
        }
    }
}
