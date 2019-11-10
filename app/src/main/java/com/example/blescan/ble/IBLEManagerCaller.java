package com.example.blescan.ble;

import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;

public interface IBLEManagerCaller {

    void scanStartedSuccessfully();
    void scanStoped();
    void scanFailed(int error);
    void newDeviceDetected();
    void connectedGATT(String address);
    void disconnectedGATT();
    void discoveredServices(ArrayList<BluetoothGattService> services);
    void log(String tag, String msg);

}
