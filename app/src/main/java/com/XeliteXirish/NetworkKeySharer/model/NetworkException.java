package com.XeliteXirish.NetworkKeySharer.model;

public class NetworkException extends Exception {

    public static final int WEP_KEY_LENGTH_ERROR = 0x0001;
    public static final int WPA_KEY_LENGTH_ERROR = 0x0002;

    private int errorCode;

    public NetworkException(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
