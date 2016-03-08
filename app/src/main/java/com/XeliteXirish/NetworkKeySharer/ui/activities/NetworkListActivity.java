package com.XeliteXirish.NetworkKeySharer.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.XeliteXirish.NetworkKeySharer.AnalyticsTrackers;
import com.XeliteXirish.NetworkKeySharer.R;
import com.XeliteXirish.NetworkKeySharer.adapters.NetworkAdapter;
import com.XeliteXirish.NetworkKeySharer.db.KeysDataSource;
import com.XeliteXirish.NetworkKeySharer.model.AuthType;
import com.XeliteXirish.NetworkKeySharer.model.Network;
import com.XeliteXirish.NetworkKeySharer.ui.AboutDialog;
import com.XeliteXirish.NetworkKeySharer.ui.AddNetworkDialog;
import com.XeliteXirish.NetworkKeySharer.ui.ContextMenuRecyclerView;
import com.XeliteXirish.NetworkKeySharer.ui.DividerItemDecoration;
import com.XeliteXirish.NetworkKeySharer.utils.WpaSupplicantParser;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import eu.chainfire.libsuperuser.Shell;

public class NetworkListActivity extends AppCompatActivity {
    private static final String TAG = NetworkListActivity.class.getSimpleName();

    private static final String FILE_WIFI_SUPPLICANT = "/data/misc/wifi/wpa_supplicant.conf";

    private static final int PASSWORD_REQUEST = 1;
    private static final String KEY_NETWORK_ID = "network_id";
    private static final String PREF_KEY_HAS_READ_NO_ROOT_DIALOG = "has_read_no_root_dialog";

    private List<Network> networks;
    private NetworkAdapter networkAdapter;
    private ContextMenuRecyclerView rvWifiNetworks;
    private WifiManager wifiManager;
    private boolean isDeviceRooted = false;
    private int networkIdToUpdate = -1; // index of item to update in networks list
    private BroadcastReceiver wifiStateChangeBroadcastReceiver;
    private boolean waitingForWifiToTurnOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        Tracker tracker = AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
        tracker.setScreenName("Network List Activity");
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        /* Enable Wi-Fi if disabled */
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            waitingForWifiToTurnOn = true;
            initializeWifiStateChangeListener();
        }

        setupWifiNetworksList();
    }

    private void setupWifiNetworksList() {
        networks = new ArrayList<>();

        rvWifiNetworks = (ContextMenuRecyclerView) findViewById(R.id.rvWifiNetwork);

        networkAdapter = new NetworkAdapter(this, networks);
        rvWifiNetworks.setAdapter(networkAdapter);
        // Set layout manager to position the items
        rvWifiNetworks.setLayoutManager(new LinearLayoutManager(this));

        // Separator
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST);
        rvWifiNetworks.addItemDecoration(itemDecoration);
        rvWifiNetworks.setHasFixedSize(true);
        rvWifiNetworks.setItemAnimator(new DefaultItemAnimator());
        registerForContextMenu(rvWifiNetworks);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addWifiNetwork();
            }
        });

        if (!waitingForWifiToTurnOn) {
            (new WifiListTask()).execute();
        }
    }

    void initializeWifiStateChangeListener() {
        wifiStateChangeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    final boolean isConnected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);

                    if (isConnected) {
                        if (waitingForWifiToTurnOn) {
                            (new WifiListTask()).execute();
                        }
                    }
                }
            }
        };
    }

    private void addWifiNetwork() {

        AddNetworkDialog addNetworkDialog = new AddNetworkDialog(this, networks, networkAdapter, rvWifiNetworks);
        addNetworkDialog.show();
    }

    @Override
    protected void onResume() {
        if (waitingForWifiToTurnOn) {
            IntentFilter wifiStateIntentFilter = new IntentFilter();
            wifiStateIntentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
            registerReceiver(wifiStateChangeBroadcastReceiver, wifiStateIntentFilter);
        }

        if (networkIdToUpdate > -1) {
            String key = KeysDataSource.getInstance().getWifiKey(
                    networks.get(networkIdToUpdate).getSsid(),
                    networks.get(networkIdToUpdate).getAuthType());
            if (key == null) {
                Log.d(TAG, "onResume: key is null");
            } else {
                networks.get(networkIdToUpdate).setKey(key);
                networkAdapter.notifyItemChanged(networkIdToUpdate);
            }
            networkIdToUpdate = -1;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (waitingForWifiToTurnOn) {
            unregisterReceiver(wifiStateChangeBroadcastReceiver);
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_network_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_qr_reader:
                Intent qrReaderIntent = new Intent(this, QrCodeReaderActivity.class);
                startActivity(qrReaderIntent);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                final AlertDialog aboutDialog = new AboutDialog(this);
                aboutDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int itemPosition = ((ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo).position;

        menu.setHeaderTitle(networks.get(itemPosition).getSsid());
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.context_menu, menu);

        boolean canViewPasword = networks.get(itemPosition).isPasswordProtected() && !networks.get(itemPosition).getKey().isEmpty();
        boolean canClearPassword = canViewPasword && isDeviceRooted;

        MenuItem viewPasswordMenuItem = menu.findItem(R.id.context_menu_wifi_list_view_password);
        viewPasswordMenuItem.setEnabled(canViewPasword);

        MenuItem clearPasswordMenuItem = menu.findItem(R.id.context_menu_wifi_list_clear_password);
        clearPasswordMenuItem.setEnabled(canClearPassword);
        //clearPasswordMenuItem.setVisible(!isDeviceRooted);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int itemPosition = ((ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo()).position;
        switch (item.getItemId()) {
            case (R.id.context_menu_wifi_list_view_password):
                final AlertDialog viewPasswordDialog = new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.wifilist_dialog_view_password))
                        .setView(R.layout.dialog_view_password)
                        .setPositiveButton(R.string.action_close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                viewPasswordDialog.show();

                /* Set SSID, security and password values */
                TextView ssidTextView = (TextView) viewPasswordDialog.findViewById(R.id.ssid_value);
                TextView authTypeTextView = (TextView) viewPasswordDialog.findViewById(R.id.auth_type_value);
                TextView passwordTextView = (TextView) viewPasswordDialog.findViewById(R.id.password_value);
                ssidTextView.setText(networks.get(itemPosition).getSsid());
                authTypeTextView.setText(networks.get(itemPosition).getAuthType().toString());
                passwordTextView.setText(networks.get(itemPosition).getKey());
                passwordTextView.setTextIsSelectable(true);
                return true;
            case (R.id.context_menu_wifi_list_clear_password):
                if(!isDeviceRooted) {
                    removeSavedWifiKey(itemPosition);
                    Toast.makeText(this, "Network Key Removed", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Network Key Removed", Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void removeSavedWifiKey(int position) {
        /* Reset key in local Wi-Fi list */
        networks.get(position).setKey("");

        /* Notify adapter that the Wi-Fi network has changed */
        networkAdapter.notifyItemChanged(position);

        /* Remove key from saved keys database */
        Network network = networks.get(position);
        String ssid = network.getSsid();
        AuthType authType = network.getAuthType();
        if (KeysDataSource.getInstance().removeWifiKey(ssid, authType) == 0) {
            Log.e(TAG, "No key was removed from database");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == PASSWORD_REQUEST) {
            if (resultCode == RESULT_OK) {
                networkIdToUpdate = data.getIntExtra(KEY_NETWORK_ID, -1);
            }
        }
    }

    private class WifiListTask extends AsyncTask<Void, Void, List<Network>> {

        @Override
        protected List<Network> doInBackground(Void... params) {

            List<Network> wifiManagerNetworks = new ArrayList<>();
            List<WifiConfiguration> savedWifiConfigs = wifiManager.getConfiguredNetworks();
            if (waitingForWifiToTurnOn) {
                wifiManager.setWifiEnabled(false);
                waitingForWifiToTurnOn = false;
                unregisterReceiver(wifiStateChangeBroadcastReceiver);
            }

            /* Populate Network list from WifiManager */
            if (savedWifiConfigs != null) {
                for (WifiConfiguration wifiConfig : savedWifiConfigs) {
                    Network newNetwork = Network.fromWifiConfiguration(wifiConfig);
                    if (!newNetwork.getSsid().isEmpty()) {
                        wifiManagerNetworks.add(newNetwork);
                    }
                }
            }

            /* Get passwords from wpa_supplicant if root is available */
            if (Shell.SU.available()) {
                isDeviceRooted = true;

                List<String> result = Shell.SU.run("cat " + FILE_WIFI_SUPPLICANT);
                String strRes = "";
                for (String line : result) {
                    strRes += line + "\n";
                    //Log.d(TAG, line);
                }
                List<Network> wpaSupplicantNetworks = WpaSupplicantParser.parse(strRes);

                for (Network wifiManagerNetwork : wifiManagerNetworks) {
                    if (wifiManagerNetwork.getAuthType() != AuthType.OPEN) {
                        for (Network wpaSupplicantNetwork : wpaSupplicantNetworks) {
                            if (wifiManagerNetwork.getSsid().equals(wpaSupplicantNetwork.getSsid())) {
                                wifiManagerNetwork.setKey(wpaSupplicantNetwork.getKey());
                                break;
                            }
                        }
                    }
                }
            }

            Collections.sort(wifiManagerNetworks, new Comparator<Network>() {
                @Override
                public int compare(Network w1, Network w2) {
                    return w1.getSsid().toLowerCase().compareTo(w2.getSsid().toLowerCase());
                }
            });

            return wifiManagerNetworks;
        }

        @Override
        protected void onPostExecute(List<Network> wifiManagerNetworks) {
            for (Network network : wifiManagerNetworks) {
                /* TODO: EAP networks are not yet supported */
                if (network.getAuthType() != AuthType.WPA_EAP && network.getAuthType() != AuthType.WPA2_EAP) {
                    networks.add(network);
                    networkAdapter.notifyItemInserted(networkAdapter.getItemCount() - 1);
                }
            }
            if (!isDeviceRooted) {
                setSavedKeysToWifiNetworks();

                boolean hasReadNoRootDialog = PreferenceManager
                        .getDefaultSharedPreferences(NetworkListActivity.this)
                        .getBoolean(PREF_KEY_HAS_READ_NO_ROOT_DIALOG, false);
                if (!hasReadNoRootDialog) {
                    new AlertDialog.Builder(NetworkListActivity.this)
                            .setTitle(getString(R.string.wifilist_dialog_noroot_title))
                            .setMessage(getString(R.string.wifilist_dialog_noroot_msg))
                            .setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    PreferenceManager.getDefaultSharedPreferences(NetworkListActivity.this)
                                            .edit()
                                            .putBoolean(PREF_KEY_HAS_READ_NO_ROOT_DIALOG, true)
                                            .apply();
                                    dialogInterface.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
        }
    }

    private void setSavedKeysToWifiNetworks() {
        List<Network> networksWithKey = KeysDataSource.getInstance().getSavedWifiWithKeys();

        for (int i = 0; i < networks.size(); i++) {
            for (int j = 0; j < networksWithKey.size(); j++) {
                if (networks.get(i).getSsid().equals(networksWithKey.get(j).getSsid())
                        && networks.get(i).getAuthType() == networksWithKey.get(j).getAuthType()) {
                    if (networks.get(i).needsPassword()) {
                        networks.get(i).setKey(networksWithKey.get(j).getKey());
                        networkAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }
}
