package com.example.blescan.ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BLEManager extends ScanCallback {
    BLEManagerCallerInterface caller;
    Context context;

    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    public List<ScanResult> scanResults=new ArrayList<>();

    public BLEManager(BLEManagerCallerInterface caller, Context context) {
        this.caller = caller;
        this.context = context;
        initializeBluetoothManager();
    }



    public void initializeBluetoothManager(){
        try{
            bluetoothManager=(BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            this.bluetoothAdapter=bluetoothManager.getAdapter();
        }catch (Exception error){

        }
    }

    public boolean isBluetoothOn(){
        try{
            return bluetoothManager.getAdapter().isEnabled();
        }catch (Exception error){

        }
        return false;
    }

    public void requestLocationPermissions(final Activity activity,int REQUEST_CODE){
        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                boolean gps_enabled = false;
                boolean network_enabled = false;

                LocationManager locationManager=(LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                try {
                    gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception ex) {}

                try {
                    network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch(Exception ex) {}

                if(!((gps_enabled)||(network_enabled))){

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage("In order to BLE connection be successful please proceed to enable the GPS")
                            .setTitle("Settings");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);

                        }
                    });

                    builder.create().show();
                }
            }
            if (ContextCompat.checkSelfPermission(this.context.getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                activity.requestPermissions( new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE);

            }
        }catch (Exception error){

        }

    }

    public void enableBluetoothDevice(Activity activity,int REQUEST_ENABLE_BT){
        try{
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }catch (Exception error){

        }
    }



    public void scanDevices(){
        try{
            scanResults.clear();
            bluetoothLeScanner=bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(this);
            caller.scanStartedSuccessfully();
        }catch (Exception error){

        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if(!isResultAlreadyAtList(result)) {
            scanResults.add(result);
        }
        caller.newDeviceDetected();

    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {

    }

    @Override
    public void onScanFailed(int errorCode) {
        caller.scanFailed(errorCode);
    }

    public boolean isResultAlreadyAtList(ScanResult newResult){
        for (ScanResult current : scanResults){
            if(current.getDevice().getAddress().equals(newResult.getDevice().getAddress())){
                return true;
            }
        }
        return false;
    }

    public BluetoothDevice getByAddress(String targetAddress){
        for(ScanResult current : scanResults){
            if(current!=null){
                if(current.getDevice().getAddress().equals(targetAddress)){
                    return current.getDevice();
                }
            }
        }
        return null;
    }

    public void connectToGATTServer(BluetoothDevice device){
        try{
            device.connectGatt(this.context, false, new BluetoothGattCallback() {
                @Override
                public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyUpdate(gatt, txPhy, rxPhy, status);
                }

                @Override
                public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                    super.onPhyRead(gatt, txPhy, rxPhy, status);
                }

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if(newState==BluetoothGatt.STATE_CONNECTED){
                        gatt.discoverServices();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);

                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                }

                @Override
                public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                    super.onReliableWriteCompleted(gatt, status);
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    super.onReadRemoteRssi(gatt, rssi, status);
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                }
            });
        }catch (Exception error){

        }
    }

}
