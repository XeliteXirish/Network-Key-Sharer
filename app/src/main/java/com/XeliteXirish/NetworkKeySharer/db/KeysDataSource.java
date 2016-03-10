package com.XeliteXirish.NetworkKeySharer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import com.XeliteXirish.NetworkKeySharer.model.AuthType;
import com.XeliteXirish.NetworkKeySharer.model.Network;

public class KeysDataSource {

    private static final String TAG = KeysDataSource.class.getSimpleName();

    private static KeysDataSource instance;
    private KeysDbHelper dbHelper;

    public static void init(Context context) {
        if (instance == null) {
            instance = new KeysDataSource(context);
        }
    }

    public static KeysDataSource getInstance() {
        return instance;
    }

    private KeysDataSource(Context context) {
        dbHelper = new KeysDbHelper(context);
    }

    /**
     * Return the Wi-Fi configurations with their key
     * @return the Wi-Fi configurations with their key
     */
    public List<Network> getSavedWifiWithKeys() {
        List<Network> networks = new ArrayList<>();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                KeysContract.WifiKeys._ID,
                KeysContract.WifiKeys.COLUMN_NAME_SSID,
                KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE,
                KeysContract.WifiKeys.COLUMN_NAME_KEY
        };

        // How you want the results sorted in the resulting Cursor
        //String sortOrder = KeysContract.WifiKeys.COLUMN_NAME_SSID + " DESC";

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                KeysContract.WifiKeys.TABLE_NAME,   // The table to query
                projection,                             // The columns to return
                null,                                   // The columns for the WHERE clause
                null,                                   // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );

        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    String ssid = cursor.getString(1);
                    AuthType authType = AuthType.valueOf(cursor.getString(2));
                    String key = cursor.getString(3);
                    networks.add(new Network(ssid, authType, key, false));

                    cursor.moveToNext();
                }
            }
        } finally {
            cursor.close();
        }

        return networks;
    }

    public String getWifiKey(String ssid, AuthType authType) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                KeysContract.WifiKeys._ID,
                KeysContract.WifiKeys.COLUMN_NAME_SSID,
                KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE,
                KeysContract.WifiKeys.COLUMN_NAME_KEY
        };
        String selection = KeysContract.WifiKeys.COLUMN_NAME_SSID + " = ? AND " + KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE + " = ?";
        String[] selectionArgs = {
                ssid, authType.name()
        };

        // How you want the results sorted in the resulting Cursor
        //String sortOrder = KeysContract.WifiKeys.COLUMN_NAME_SSID + " DESC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                KeysContract.WifiKeys.TABLE_NAME,   // The table to query
                projection,                             // The columns to return
                selection,                              // The columns for the WHERE clause
                selectionArgs,                          // The values for the WHERE clause
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null                                    // The sort order
        );

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(3);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * Insert the new Wi-Fi network containing the key, returning the primary key value of the new row
     * @param network the Wi-Fi configuration to add
     * @return the primary key value of the newly inserted row
     */
    public long insertWifiKey(Network network) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(KeysContract.WifiKeys.COLUMN_NAME_SSID, network.getSsid());
        values.put(KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE, network.getAuthType().name());
        values.put(KeysContract.WifiKeys.COLUMN_NAME_KEY, network.getKey());

        // Insert the new row, returning the primary key value of the new row
        return db.insert(KeysContract.WifiKeys.TABLE_NAME, null, values);
    }

    public int removeWifiKey(String ssid, AuthType authType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String whereClause = KeysContract.WifiKeys.COLUMN_NAME_SSID + " = ? AND " + KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE + " = ?";
        String[] whereArgs = {
                ssid, authType.name()
        };

        return db.delete(KeysContract.WifiKeys.TABLE_NAME, whereClause, whereArgs);
    }
}
