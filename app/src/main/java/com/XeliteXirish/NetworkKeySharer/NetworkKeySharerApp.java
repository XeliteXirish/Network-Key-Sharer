package com.XeliteXirish.NetworkKeySharer;

import android.app.Application;

import org.wordpress.passcodelock.AppLockManager;

import com.XeliteXirish.NetworkKeySharer.db.KeysDataSource;
import com.XeliteXirish.NetworkKeySharer.ui.activities.ConfirmConnectToNetworkActivity;

public class NetworkKeySharerApp extends Application {

    public static final boolean ENABLE_ADS = true;

    @Override
    public void onCreate() {
        super.onCreate();

        AppLockManager.getInstance().enableDefaultAppLockIfAvailable(this);

        if (AppLockManager.getInstance().isAppLockFeatureEnabled()) {

            AppLockManager.getInstance().getCurrentAppLock().setDisabledActivities(
                    new String[]{ ConfirmConnectToNetworkActivity.class.getCanonicalName() });
        }
        AnalyticsTrackers.initialize(this);
        KeysDataSource.init(this);
    }
}
