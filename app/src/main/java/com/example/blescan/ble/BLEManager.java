package com.example.blescan.ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.example.blescan.R;

import java.util.ArrayList;
import java.util.List;

public class BLEManager extends ScanCallback {

    private IBLEManagerCaller caller;
    private Context context;
//    private LogBLE log;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    public ArrayList<ScanResult> scanResults;

    public static String TAG = "BLEManager";
    public static int REQUEST_BLUETOOTH_PERMISSION_NEEDED = 1027;
    public static int REQUEST_LOCATION_PERMISSION_NEEDED = 1028;
    private BluetoothGatt lastBluetoothGatt;

    public BLEManager(IBLEManagerCaller caller, Context context) {
        this.caller = caller;
        this.context = context;
        this.scanResults=new ArrayList<>();
//        this.log = LogBLE.getInstance();
        initializeBluetoothManager();
    }

    private void initializeBluetoothManager(){
        try{
            bluetoothManager=(BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            this.bluetoothAdapter=bluetoothManager.getAdapter();
        }catch (Exception error){
            caller.error(TAG,"initialize. "+error.getMessage());
        }
    }

    public boolean isBluetoothOn(){
        try{
            return bluetoothManager.getAdapter().isEnabled();
        }catch (Exception error){
            caller.error(TAG,"isBluetoothOn. "+error.getMessage());
        }
        return false;
    }


    public static boolean CheckIfBLEIsSupportedOrNot(Context context){
        try {
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }catch (Exception error){
            LogBLE.getInstance().error(TAG, "CheckIfBLEIsSupportedOrNot. "+error.getMessage());
        }
        return false;
    }

    public static boolean RequestBluetoothDeviceEnable(final Activity activity){
        try{
            BluetoothManager bluetoothManager=(BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter=bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                AlertDialog.Builder builder=new AlertDialog.Builder(activity)
                        .setTitle("Bluetooth")
                        .setMessage("The bluetooth device must be enabled in order to connect the device")
                        //.setIcon(R.mipmap.ic_launcher_round)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                activity.startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH_PERMISSION_NEEDED);
                            }
                        });
                builder.show();

            }else {
                return true;
            }
        }catch (Exception error){
            LogBLE.getInstance().error(TAG,"RequestBluetoothDeviceEnable. "+error.getMessage());
        }
        return false;
    }

    public static void requestLocationPermissions(final Activity activity, final Context context){
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
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                activity.requestPermissions( new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION_NEEDED);

            }
        }catch (Exception error){
            LogBLE.getInstance().error(TAG,"requestLocationPermissions. "+error.getMessage());
        }

    }

    public void scanDevices(){
        try{
            scanResults.clear();
            bluetoothLeScanner=bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(this);
            caller.scanStartedSuccessfully();
        }catch (Exception error){
            caller.error(TAG,"scan devices. "+error.getMessage());
        }
    }

    public void stopScan(){
        try{
            bluetoothLeScanner=bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(this);
        }catch (Exception error){
            caller.error(TAG,"stopScan. "+error.getMessage());
        }
    }

    String getAddress(){
        if(lastBluetoothGatt!=null){
            return lastBluetoothGatt.getDevice().getAddress();
        }
        return null;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        if(result != null) {
            if (!isResultAlreadyAtList(result)) {
                scanResults.add(result);
            }
            caller.newDeviceDetected();
        }
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {

    }

    @Override
    public void onScanFailed(int errorCode) {
        caller.scanFailed(errorCode);
    }

    private boolean isResultAlreadyAtList(ScanResult newResult){
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult current = scanResults.get(i);
            if(current.getDevice().getAddress().equals(newResult.getDevice().getAddress())){
                scanResults.remove(i);
                scanResults.add(i,newResult);
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

    public void discoverServices() {
        this.lastBluetoothGatt.discoverServices();
    }

    public void connectToGATTServer(BluetoothDevice device){
        try{
            this.lastBluetoothGatt =  device.connectGatt(this.context, false, new BluetoothGattCallback() {
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
                        lastBluetoothGatt=gatt;
                        caller.connectedGATT(gatt.getDevice().getAddress());
                    }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                        lastBluetoothGatt = null;
                        caller.disconnectedGATT();
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        ArrayList<BluetoothGattService> services = new ArrayList<>(gatt.getServices());
                        searchAndSetAllNotifyAbleCharacteristics();
                        caller.discoveredServices(services);
                    } else {
                        caller.error(TAG, "onServicesDiscovered received: " + status);
                    }

                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        caller.showCharacteristic(characteristic);
                    }else{
                        caller.errorUI("Error reading.");
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        caller.showCharacteristic(characteristic);
                    }else{
                        caller.errorUI("Error writing.");
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    caller.characteristicChanged(characteristic);
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
            },BluetoothDevice.TRANSPORT_LE);
        }catch (Exception error){
            caller.error(TAG,LogBLE.ERROR+error.getMessage());
        }
    }

    void disconnect(){
        if(lastBluetoothGatt!=null) {
            this.lastBluetoothGatt.disconnect();
        }else{
            caller.errorUI("Connection doesn't exist");
        }
    }

    public static boolean isCharacteristicWriteable(BluetoothGattCharacteristic characteristic) {
        return (characteristic.getProperties() &
                (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static boolean isCharacteristicReadable(BluetoothGattCharacteristic characteristic) {
        return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    public static boolean isCharacteristicNotifiable(BluetoothGattCharacteristic characteristic) {
        return ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0);
    }

    private void searchAndSetAllNotifyAbleCharacteristics() {
        try {
            if(lastBluetoothGatt!=null){
                for(BluetoothGattService currentService: lastBluetoothGatt.getServices()){
                    if(currentService!=null){
                        for(BluetoothGattCharacteristic currentCharacteristic:currentService.getCharacteristics()){
                            if(currentCharacteristic!=null){
                                if(isCharacteristicNotifiable(currentCharacteristic)){
                                    lastBluetoothGatt.setCharacteristicNotification(currentCharacteristic, true);
                                    for(BluetoothGattDescriptor currentDescriptor:currentCharacteristic.getDescriptors()){
                                        if(currentDescriptor!=null){
                                            try {
                                                currentDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                                lastBluetoothGatt.writeDescriptor(currentDescriptor);
                                            }catch (Exception internalError){
                                                /*for (BluetoothHelperCallerInterface current:callers
                                                ) {
                                                    current.bluetoothHelperErrorThrown(internalError);
                                                }*/
                                                caller.error(TAG,"searchAndSetAllNotifyAbleCharacteristics. "+internalError.getMessage());
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                caller.log(TAG, "searchAndSetAllNotifyAbleCharacteristics.");
            }
        } catch (Exception error){
            /*for (BluetoothHelperCallerInterface current:callers
            ) {
                current.bluetoothHelperErrorThrown(error);
            }*/
            caller.error(TAG,"searchAndSetAllNotifyAbleCharacteristics. "+error.getMessage());
        }

    }

    public static String byteArrayToHexString(byte[] data) {
        String s = "";
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            s = new String(data) + "\n" +
                    stringBuilder.toString();
        }
        return s;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic){
        try{
            if(characteristic==null) return false;

            return lastBluetoothGatt.readCharacteristic(getCharacteristic(characteristic));
        }catch (Exception error){
            /*for (BluetoothHelperCallerInterface current:callers
            ) {
                current.bluetoothHelperErrorThrown(error);
            }*/
            caller.error(TAG,"readCharacteristic. "+error.getMessage());
        }
        return false;
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic,byte[] data){
        try{
            if(characteristic==null) return false;
            BluetoothGattCharacteristic ch = getCharacteristic(characteristic);
            ch.setValue(data);
            return lastBluetoothGatt.writeCharacteristic(ch);
        }catch (Exception error){

            /*for (BluetoothHelperCallerInterface current:callers
            ) {
                current.bluetoothHelperErrorThrown(error);
            }*/
            caller.error(TAG,"writeCharacteristic. "+error.getMessage());
        }
        return false;
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGattCharacteristic c){
        for (BluetoothGattService service : this.lastBluetoothGatt.getServices()) {
//            if(c.getService().getUuid().equals(service.getUuid())) {
                for (BluetoothGattCharacteristic ch : service.getCharacteristics()) {
                    if (ch.getUuid().equals(c.getUuid())) {
                        return ch;
                    }
                }
//            }
        }
        return null;
    }

    /*public void processCommand() {
        if (commandQueue.size() == 0) {
            return;
        }
        BluetoothGattCharacteristic currentCharacteristic = commandQueue.poll();
        lastBluetoothGatt.setCharacteristicNotification(currentCharacteristic, true);
        currentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        BluetoothGattDescriptor descriptor = currentCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            lastBluetoothGatt.writeDescriptor(descriptor);
        }
    }*/
}
