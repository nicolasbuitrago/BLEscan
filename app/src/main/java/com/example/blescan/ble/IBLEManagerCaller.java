package com.example.blescan.ble;

public interface IBLEManagerCaller {

    void scanStartedSuccessfully();
    void scanStoped();
    void scanFailed(int error);
    void newDeviceDetected();

}
