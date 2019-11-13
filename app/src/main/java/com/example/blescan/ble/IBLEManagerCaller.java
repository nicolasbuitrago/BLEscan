package com.example.blescan.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;

public interface IBLEManagerCaller {

    void scanStartedSuccessfully();
    void scanStopped();
    void scanFailed(int error);
    void newDeviceDetected();
    void connectedGATT(String address);
    void disconnectedGATT();
    void discoveredServices(ArrayList<BluetoothGattService> services);
    void characteristicChanged(BluetoothGattCharacteristic characteristic);
    void showCharacteristic(BluetoothGattCharacteristic characteristic);
    void log(String tag, String msg);
    void error(String tag, String msg);
    void errorUI(String msg);

}
