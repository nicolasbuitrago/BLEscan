package com.example.blescan.ble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.blescan.MainActivity;
import com.example.blescan.R;
import com.example.blescan.broadcast.BroadcastManager;
import com.example.blescan.broadcast.IBroadcastManagerCaller;

import java.util.ArrayList;

public class BLEService extends Service implements IBLEManagerCaller, IBroadcastManagerCaller {

    public static String TAG = BLEService.class.getName();

    public static String CHANNEL = "com.example.blescan.ble.BLEService";

    public static String TYPE_SCAN_DEVICES= "com.example.blescan.ble.BLEService.type.TYPE_SCAN_DEVICES";
    public static String TYPE_STOP_SCAN= "com.example.blescan.ble.BLEService.type.TYPE_STOP_SCAN";
    public static String TYPE_NEW_DEVICE= "com.example.blescan.ble.BLEService.type.TYPE_NEW_DEVICE";
    public static String TYPE_CONNECT_GATT= "com.example.blescan.ble.BLEService.type.TYPE_CONNECT_GATT";
    public static String TYPE_CONNECTED_GATT= "com.example.blescan.ble.BLEService.type.TYPE_CONNECTED_GATT";
    public static String TYPE_DISCONNECTED_GATT= "com.example.blescan.ble.BLEService.type.TYPE_DISCONNECTED_GATT";
    public static String TYPE_DISCOVER_SERVICES= "com.example.blescan.ble.BLEService.type.TYPE_DISCOVER_SERVICES";
    public static String TYPE_DISCOVERED_SERVICES= "com.example.blescan.ble.BLEService.type.TYPE_DISCOVERED_SERVICES";
    public static String TYPE_NEW_NOTIFICATION= "com.example.blescan.ble.BLEService.type.TYPE_NEW_NOTIFICATION";
    public static String TYPE_SEND_CHARACTERISTICS= "com.example.blescan.ble.BLEService.type.TYPE_SEND_CHARACTERISTICS";
    public static String TYPE_SHOW_CHARACTERISTICS= "com.example.blescan.ble.BLEService.type.TYPE_SHOW_CHARACTERISTICS";
    public static String TYPE_CHARACTERISTIC_CHANGED= "com.example.blescan.ble.BLEService.type.TYPE_CHARACTERISTIC_CHANGED";
    public static String TYPE_WRITE_CHARACTERISTIC= "com.example.blescan.ble.BLEService.type.TYPE_WRITE_CHARACTERISTIC";
    public static String TYPE_READ_CHARACTERISTIC= "com.example.blescan.ble.BLEService.type.TYPE_READ_CHARACTERISTIC";
    public static String TYPE_SHOW_CHARACTERISTIC= "com.example.blescan.ble.BLEService.type.TYPE_SHOW_CHARACTERISTIC";
    public static String TYPE_GET_CONNECTION= "com.example.blescan.ble.BLEService.type.TYPE_GET_CONNECTION";
    public static String TYPE_RESPONSE_CONNECTION= "com.example.blescan.ble.BLEService.type.TYPE_RESPONSE_CONNECTION";
    public static String TYPE_DISCONNECT_DEVICE= "com.example.blescan.ble.BLEService.type.TYPE_DISCONNECT_DEVICE";

    public static String TYPE_SUCCESS= "com.example.blescan.ble.BLEService.type.TYPE_SUCCESS";
    public static String TYPE_ERROR= "com.example.blescan.ble.BLEService.type.TYPE_ERROR";

    public static String EXTRA_DEVICES= "com.example.blescan.ble.BLEService.extra.EXTRA_DEVICES";
    public static String EXTRA_ADDRESS= "com.example.blescan.ble.BLEService.extra.EXTRA_ADDRESS";
    public static String EXTRA_SERVICES= "com.example.blescan.ble.BLEService.extra.EXTRA_SERVICES";
    public static String EXTRA_CHARACTERISTICS= "com.example.blescan.ble.BLEService.extra.EXTRA_CHARACTERISTICS";
    public static String EXTRA_CHARACTERISTIC= "com.example.blescan.ble.BLEService.extra.EXTRA_CHARACTERISTIC";
    public static String EXTRA_VALUE= "com.example.blescan.ble.BLEService.extra.EXTRA_VALUE";
    public static String EXTRA_MESSAGE= "com.example.blescan.ble.BLEService.extra.EXTRA_VALUE";

    private static final int ID_SERVICE = 1337;
    private static int ID_NOTIFICATION = 1027;

    private  BLEManager bleManager;
    private BroadcastManager broadcastManager;
    private LogBLE log;
    private String notificationChannel;
    private String channelId;

    public BLEService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // do stuff like register for BroadcastReceiver, etc.
        this.log = LogBLE.getInstance();
        initializeBroadcastManager();
        bleManager = new BLEManager(this,getApplicationContext());

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Create the Foreground Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationChannel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannel);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //.setLargeIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(ID_SERVICE, notification);
        this.log.add(TAG,"Service started");
        this.channelId = createChannel(notificationManager);
    }

    public void initializeBroadcastManager(){
        try{
            if(broadcastManager==null){
                broadcastManager=new BroadcastManager(getApplicationContext(), BLEService.CHANNEL, this);
            }
        }catch (Exception error){
            this.log.add(TAG,"initializeBroadcastManager. ");
        }
    }

    //@RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "ble";
        String channelName = "BLEscan service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        // omitted the LED color
        //channel.setImportance(NotificationManager.IMPORTANCE_MAX);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
    
    //@RequiresApi(Build.VERSION_CODES.O)
    private String createChannel(NotificationManager notificationManager){
        String channelId = "bleChars";
        String channelName = "BLEscan";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        //channel.setImportance(NotificationManager.IMPORTANCE_MAX);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(broadcastManager!=null){
            broadcastManager.unRegister();
        }
        this.log.add(TAG,"Service finished");
        super.onDestroy();
    }

    @Override
    public void scanStartedSuccessfully() {
        
    }

    @Override
    public void scanStopped() {

    }

    @Override
    public void scanFailed(int error) {

    }

    @Override
    public void newDeviceDetected() {
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_DEVICES,bleManager.scanResults);
        this.broadcastManager.sendBroadcast(TYPE_NEW_DEVICE,args);
        this.log.add(TAG,"New device detected.");
    }

    @Override
    public void connectedGATT(String address) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ADDRESS,address);
        this.broadcastManager.sendBroadcast(TYPE_CONNECTED_GATT,args);
        this.log.add(TAG,"Connected to GATT server with address: "+address+".");
    }

    @Override
    public void disconnectedGATT() {
        this.broadcastManager.sendBroadcast(TYPE_DISCONNECTED_GATT,null);
        this.log.add(TAG,"Disconnected GATT server.");
    }

    @Override
    public void discoveredServices(ArrayList<BluetoothGattService> services) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ADDRESS,this.bleManager.getAddress());
        args.putParcelableArrayList(EXTRA_SERVICES,services);
        this.broadcastManager.sendBroadcast(TYPE_DISCOVERED_SERVICES,args);
        this.log.add(TAG,"Discovered services.");
    }

    @Override
    public void characteristicChanged(BluetoothGattCharacteristic characteristic) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                //.setLargeIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("Characteristic with UUID: "+characteristic.getUuid().toString()
                        +" changed.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Characteristic with UUID: "+characteristic.getUuid().toString()
                                +" changed."))
                .setContentIntent(pendingIntent)
                .build();
        notificationManager.notify(ID_NOTIFICATION++,notification);
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_CHARACTERISTIC,characteristic);
        this.broadcastManager.sendBroadcast(TYPE_CHARACTERISTIC_CHANGED,args);
        this.log.add(TAG,"Characteristic with UUID: "+characteristic.getUuid().toString() +" changed.");
    }

    @Override
    public void showCharacteristic(BluetoothGattCharacteristic characteristic) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_CHARACTERISTIC,characteristic);
        this.broadcastManager.sendBroadcast(TYPE_SHOW_CHARACTERISTIC,args);
    }

    @Override
    public void log(String tag, String msg) {
        this.log.add(tag,msg);
    }

    @Override
    public void error(String tag, String msg) {
        this.log.error(tag,msg);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, String type, Bundle args) {
        if(TYPE_SCAN_DEVICES.equals(type)){
            this.bleManager.scanDevices();
            this.log.add(TAG,"Scan started.");
        }else if(TYPE_STOP_SCAN.equals(type)){
            this.bleManager.stopScan();
            this.log.add(TAG,"Scan stopped.");
        } else if(TYPE_CONNECT_GATT.equals(type)){
            String address = args.getString(EXTRA_ADDRESS);
            this.bleManager.connectToGATTServer(this.bleManager.getByAddress(address));
            this.log.add(TAG,"Connecting to GATT server with address: "+address+".");
        } else if (TYPE_SEND_CHARACTERISTICS.equals(type)){
            //ArrayList<BluetoothGattCharacteristic> characteristics = args.getParcelableArrayList(EXTRA_CHARACTERISTICS);
            this.broadcastManager.sendBroadcast(TYPE_SHOW_CHARACTERISTICS, args);
        }else if(TYPE_WRITE_CHARACTERISTIC.equals(type)){
            BluetoothGattCharacteristic characteristic = args.getParcelable(EXTRA_CHARACTERISTIC);
            byte[] data = args.getByteArray(EXTRA_VALUE);
            boolean r = bleManager.writeCharacteristic(characteristic,data);
            if(!r){
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_MESSAGE,"Error writing characteristic.");
                this.broadcastManager.sendBroadcast(TYPE_ERROR,bundle);
                this.log.error(TAG, "Writing the characteristic with UUID: "+characteristic.getUuid().toString() +" changed.");
            }
        }else if (TYPE_READ_CHARACTERISTIC.equals(type)){
            BluetoothGattCharacteristic characteristic = args.getParcelable(EXTRA_CHARACTERISTIC);
            boolean r = bleManager.readCharacteristic(characteristic);
            if(!r){
                Bundle bundle = new Bundle();
                bundle.putString(EXTRA_MESSAGE,"Error reading characteristic.");
                this.broadcastManager.sendBroadcast(TYPE_ERROR,bundle);
                this.log.error(TAG, "Reading the characteristic with UUID: "+characteristic.getUuid().toString() +" changed.");
            }
        } else if(TYPE_DISCOVER_SERVICES.equals(type)){
            this.bleManager.discoverServices();
        } else if(TYPE_GET_CONNECTION.equals(type)){
            String address = this.bleManager.getAddress();
            Bundle b = new Bundle();
            b.putString(EXTRA_ADDRESS,address);
            this.broadcastManager.sendBroadcast(TYPE_RESPONSE_CONNECTION,b);
        } else if (TYPE_DISCONNECT_DEVICE.equals(type)){
            bleManager.disconnect();
        }
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {
        this.log.add(TAG,"ErrorAtBroadcastManager. "+error.getMessage());
    }
}
