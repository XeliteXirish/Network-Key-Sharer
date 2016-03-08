package com.XeliteXirish.NetworkKeySharer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper class that manages database operations (creation/upgrade/deletion)
 */
public class KeysDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "wifi_keys_db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + KeysContract.WifiKeys.TABLE_NAME + " (" +
                    KeysContract.WifiKeys._ID + " INTEGER PRIMARY KEY," +
                    KeysContract.WifiKeys.COLUMN_NAME_SSID + TEXT_TYPE + COMMA_SEP +
                    KeysContract.WifiKeys.COLUMN_NAME_AUTH_TYPE + TEXT_TYPE + COMMA_SEP +
                    KeysContract.WifiKeys.COLUMN_NAME_KEY + TEXT_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + KeysContract.WifiKeys.TABLE_NAME;

    public KeysDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
