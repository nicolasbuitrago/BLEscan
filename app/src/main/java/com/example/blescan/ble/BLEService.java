package com.example.blescan.ble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

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

    public static String EXTRA_DEVICES= "com.example.blescan.ble.BLEService.extra.EXTRA_DEVICES";
    public static String EXTRA_ADDRESS= "com.example.blescan.ble.BLEService.extra.EXTRA_ADDRESS";
    public static String EXTRA_SERVICES= "com.example.blescan.ble.BLEService.extra.EXTRA_SERVICES";
    public static String EXTRA_CHARACTERISTICS= "com.example.blescan.ble.BLEService.extra.EXTRA_CHARACTERISTICS";

    private static final int ID_SERVICE = 1337;

    private  BLEManager bleManager;
    private BroadcastManager broadcastManager;
    private LogBLE log;

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

        // Create the Foreground Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //.setLargeIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_text))
                .build();

        startForeground(ID_SERVICE, notification);
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
        String channelName = "BLEscan channel";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
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

        super.onDestroy();
    }

    @Override
    public void scanStartedSuccessfully() {
        
    }

    @Override
    public void scanStoped() {

    }

    @Override
    public void scanFailed(int error) {

    }

    @Override
    public void newDeviceDetected() {
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_DEVICES,bleManager.scanResults);
        this.broadcastManager.sendBroadcast(TYPE_NEW_DEVICE,args);
    }

    @Override
    public void connectedGATT(String address) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ADDRESS,address);
        this.broadcastManager.sendBroadcast(TYPE_CONNECTED_GATT,args);
    }

    @Override
    public void disconnectedGATT() {
        this.broadcastManager.sendBroadcast(TYPE_DISCONNECTED_GATT,null);
    }

    @Override
    public void discoveredServices(ArrayList<BluetoothGattService> services) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_SERVICES,services);
        this.broadcastManager.sendBroadcast(TYPE_DISCOVERED_SERVICES,args);
    }

    @Override
    public void log(String tag, String msg) {
        this.log.add(tag,msg);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, String type, Bundle args) {
        if(TYPE_SCAN_DEVICES.equals(type)){
            this.bleManager.scanDevices();
        }else if(TYPE_STOP_SCAN.equals(type)){
            this.bleManager.stopScan();
        } else if(TYPE_CONNECT_GATT.equals(type)){
            String address = args.getString(EXTRA_ADDRESS);
            this.bleManager.connectToGATTServer(this.bleManager.getByAddress(address));
        } else if (TYPE_SEND_CHARACTERISTICS.equals(type)){
            //ArrayList<BluetoothGattCharacteristic> characteristics = args.getParcelableArrayList(EXTRA_CHARACTERISTICS);
            this.broadcastManager.sendBroadcast(TYPE_SHOW_CHARACTERISTICS, args);
        } else if(TYPE_DISCOVER_SERVICES.equals(type)){
            this.bleManager.discoverServices();
        }
    }

    @Override
    public void ErrorAtBroadcastManager(Exception error) {
        this.log.add(TAG,"ErrorAtBroadcastManager. "+error.getMessage());
    }
}
