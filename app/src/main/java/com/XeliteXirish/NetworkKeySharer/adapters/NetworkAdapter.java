package com.XeliteXirish.NetworkKeySharer.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import com.XeliteXirish.NetworkKeySharer.R;
import com.XeliteXirish.NetworkKeySharer.NetworkKeySharerApp;
import com.XeliteXirish.NetworkKeySharer.model.Network;
import com.XeliteXirish.NetworkKeySharer.ui.activities.NetworkActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class NetworkAdapter extends RecyclerView.Adapter<NetworkAdapter.ViewHolder> {

    private static final String KEY_WIFI_NETWORK = "wifi_network";
    private static final String KEY_NETWORK_ID = "network_id";
    private static final int PASSWORD_REQUEST = 1;

    private Context context;
    private List<Network> networks;

    public NetworkAdapter(Context context, List<Network> networks) {
        this.context = context;
        this.networks = networks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View wifiNetworkView = inflater.inflate(R.layout.item_network, parent, false);

        return new ViewHolder(wifiNetworkView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Network network = networks.get(position);

        TextView ssidTextView = holder.ssidTextView;
        ssidTextView.setText(network.getSsid());

        TextView authTypeTextView = holder.authTypeTextView;
        authTypeTextView.setText(network.getAuthType().toString());

        ImageView keyImageView = holder.keyImageView;
        if (network.isPasswordProtected()) {
            if (network.needsPassword()) {
                keyImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_key_missing));
            } else {
                keyImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_key));
            }
        }

        holder.itemView.setLongClickable(true);
    }

    @Override
    public int getItemCount() {
        return networks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public InterstitialAd interstitialAd;

        public TextView ssidTextView;
        public TextView authTypeTextView;
        public ImageView keyImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            if (NetworkKeySharerApp.ENABLE_ADS) {
                interstitialAd = new InterstitialAd(context);
                interstitialAd.setAdUnitId("ca-app-pub-9817254026781393/8859919666");
                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        continueOnClick();
                    }
                });
                requestNewInterstitial();
            }
            ssidTextView = (TextView) itemView.findViewById(R.id.wifi_ssid);
            authTypeTextView = (TextView) itemView.findViewById(R.id.wifi_auth_type);
            keyImageView = (ImageView) itemView.findViewById(R.id.wifi_key_icon);

            ssidTextView.setTextColor(Color.BLUE);
        }

        @Override
        public void onClick(View view) {
            Random random = new Random();
            if (NetworkKeySharerApp.ENABLE_ADS) {
                if (interstitialAd.isLoaded()) {
                    if(random.nextInt(5) == 0) {
                        interstitialAd.show();
                    }else{
                        interstitialAd.show();
                    }
                }else{
                    continueOnClick();
                }
            }else {
                continueOnClick();
            }
        }

        public void continueOnClick(){
            Network network = networks.get(getLayoutPosition());
            Intent wifiIntent = new Intent(context, NetworkActivity.class);
            wifiIntent.putExtra(KEY_WIFI_NETWORK, network);

            if (network.needsPassword()) {
                wifiIntent.putExtra(KEY_NETWORK_ID, getLayoutPosition());
                ((Activity) context).startActivityForResult(wifiIntent, PASSWORD_REQUEST);
            } else {
                context.startActivity(wifiIntent);
            }
        }

        public void requestNewInterstitial(){
            AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
            interstitialAd.loadAd(adRequest);
        }
    }
}
