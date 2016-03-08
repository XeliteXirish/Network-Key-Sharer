package com.XeliteXirish.NetworkKeySharer.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.XeliteXirish.NetworkKeySharer.R;
import com.XeliteXirish.NetworkKeySharer.adapters.NetworkAdapter;
import com.XeliteXirish.NetworkKeySharer.model.AuthType;
import com.XeliteXirish.NetworkKeySharer.model.Network;

import java.util.List;

public class AddNetworkDialog extends Dialog implements View.OnClickListener{

    public Context context;
    public NetworkAdapter networkAdapter;
    public ContextMenuRecyclerView recyclerView;
    public List<Network> networkList;

    public EditText editTextSSID;
    public Spinner spinnerSecurity;
    public EditText editTextPassword;

    public Button addNetworkButton;
    public Button cancelButton;

    public String[] securityTypes = {AuthType.OPEN.toString(), AuthType.WEP.toString(), AuthType.WPA_EAP.toString(), AuthType.WPA_PSK.toString(), AuthType.WPA2_EAP.toString(), AuthType.WPA2_PSK.toString()};

    public AddNetworkDialog(Context context, List<Network> networkList, NetworkAdapter networkAdapter, ContextMenuRecyclerView recyclerView) {
        super(context);

        this.context = context;
        this.networkAdapter = networkAdapter;
        this.recyclerView = recyclerView;
        this.networkList = networkList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_network);

        setTitle(R.string.add_network_dialog_title);

        this.editTextSSID = (EditText) findViewById(R.id.add_network_ssid_value);
        this.spinnerSecurity = (Spinner) findViewById(R.id.add_network_security_type);
        this.editTextPassword = (EditText) findViewById(R.id.add_network_password_value);

        this.addNetworkButton = (Button) findViewById(R.id.add_network_button);
        this.cancelButton = (Button) findViewById(R.id.button_cancel);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, securityTypes);
        this.spinnerSecurity.setAdapter(arrayAdapter);

        this.addNetworkButton.setOnClickListener(this);
        this.cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.add_network_button:
                this.networkList.add(new Network(this.editTextSSID.getText().toString(), getAuthFromName(this.spinnerSecurity.getSelectedItem().toString()), this.editTextPassword.getText().toString(), false));
                networkAdapter.notifyItemInserted(networkList.size() - 1);
                recyclerView.scrollToPosition(networkAdapter.getItemCount() - 1);
                dismiss();
                break;
            case R.id.button_cancel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }

    public AuthType getAuthFromName(String name){
        if(name != null){
            if(name.equals(AuthType.OPEN.toString())) return AuthType.OPEN;
            else if(name.equals(AuthType.WEP.toString())) return AuthType.WEP;
            else if(name.equals(AuthType.WPA_EAP.toString())) return AuthType.WPA_EAP;
            else if(name.equals(AuthType.WPA_PSK.toString())) return AuthType.WPA_PSK;
            else if(name.equals(AuthType.WPA2_EAP.toString())) return AuthType.WPA2_EAP;
            else if(name.equals(AuthType.WPA2_PSK.toString())) return AuthType.WPA2_PSK;
            else return null;
        }
        return null;
    }
}
