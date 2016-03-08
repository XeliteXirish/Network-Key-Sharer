package com.XeliteXirish.NetworkKeySharer.db;

import android.provider.BaseColumns;

/**
 * WifiKeys database definition
 */
public final class KeysContract {

    /* Empty constructor to prevent accidentally instantiating the contract class */
    public KeysContract() {}

    /* Inner class that defines the table contents */
    public static abstract class WifiKeys implements BaseColumns {
        public static final String TABLE_NAME = "wifi_keys";
        public static final String COLUMN_NAME_SSID = "ssid";
        public static final String COLUMN_NAME_AUTH_TYPE = "auth_type";
        public static final String COLUMN_NAME_KEY = "key";
    }

}
