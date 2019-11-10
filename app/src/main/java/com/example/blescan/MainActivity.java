package com.example.blescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.example.blescan.adapters.BluetoothListAdapter;
import com.example.blescan.adapters.ServiceListAdapter;
import com.example.blescan.ble.BLEManager;
import com.example.blescan.ble.IBLEManagerCaller;
import com.example.blescan.ble.BLEService;
import com.example.blescan.broadcast.BroadcastManager;
import com.example.blescan.broadcast.IBroadcastManagerCaller;
import com.example.blescan.fragments.DeviceList;
import com.example.blescan.fragments.ServicesList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IBroadcastManagerCaller, DeviceList.OnFragmentInteractionListener {

    private MainActivity mainActivity;
    private BroadcastManager broadcastBLE;
    private BroadcastReceiver bluetoothReceiver;
    private boolean bluetoothEnabled;
    private TextView bluetoothStatusTextView;
    private DeviceList devicesFragment;
    private ServicesList servicesList;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcastBLE.sendBroadcast(BLEService.TYPE_SCAN_DEVICES,null);
            }
        });

        boolean bleSupported = BLEManager.CheckIfBLEIsSupportedOrNot(getApplicationContext());
        if(!bleSupported){
            AlertDialog.Builder builder=new AlertDialog.Builder(this)
                    .setTitle("Bluetooth LE")
                    .setMessage("Bluetooth LE is not supported.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        }

        bluetoothStatusTextView = (TextView) findViewById(R.id.status_bluetooth_text);
        setBluetoothStatus(BLEManager.RequestBluetoothDeviceEnable(this));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        devicesFragment = new DeviceList();
        servicesList = new ServicesList();
        fragmentTransaction.add(R.id.fragment_container, devicesFragment);
        fragmentTransaction.commit();

        mainActivity=this;
        initializeBroadcastManager();
        initializeBluetoothReceiver();
    }

    public void initializeBroadcastManager(){
        try{
            if(broadcastBLE==null){
                broadcastBLE=new BroadcastManager(getApplicationContext(), BLEService.CHANNEL, this);
            }
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    public void initializeBluetoothReceiver(){
        try{
            if(bluetoothReceiver==null){
                bluetoothReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        final String action = intent.getAction();

                        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR);
                            setBluetoothStatus(state);
                        }
                    }
                };
                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(bluetoothReceiver, filter);
            }
        }catch (Exception error){
            Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    private void setBluetoothStatus(boolean status){
        bluetoothEnabled = status;
        if(status){
            bluetoothStatusTextView.setText(R.string.status_online);
            bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.green));
        }else{
            bluetoothStatusTextView.setText(R.string.status_offline);
            bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void setBluetoothStatus(int state){
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                bluetoothEnabled = false;
                bluetoothStatusTextView.setText(R.string.status_offline);
                bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.red));
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                bluetoothStatusTextView.setText(R.string.status_turning_off);
                bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.yellow));
                break;
            case BluetoothAdapter.STATE_ON:
                bluetoothEnabled = true;
                bluetoothStatusTextView.setText(R.string.status_online);
                bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.green));
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                bluetoothStatusTextView.setText(R.string.status_turning_on);
                bluetoothStatusTextView.setTextColor(getResources().getColor(R.color.yellow));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            this.broadcastBLE.sendBroadcast(BLEService.TYPE_STOP_SCAN,null);
            return true;
        }else if(id == R.id.action_start_service){
            try {
                Intent intent = new Intent(getApplicationContext(), BLEService.class);
                startService(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }else if(id == R.id.action_stop_service){
            try {
                Intent intent = new Intent(getApplicationContext(), BLEService.class);
                stopService(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allPermissionsGranted=true;
        if (requestCode == BLEManager.REQUEST_BLUETOOTH_PERMISSION_NEEDED) {
            for (int currentResult:grantResults
            ) {
                if(currentResult!= PackageManager.PERMISSION_GRANTED){
                    allPermissionsGranted=false;
                    break;
                }
            }
            if(!allPermissionsGranted){
                AlertDialog.Builder builder=new AlertDialog.Builder(this)
                        .setTitle("Permissions")
                        .setMessage("Bluetooth and Location permissions must be granted in order to execute the app")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainActivity.finish();
                            }
                        });
                builder.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode==BLEManager.REQUEST_BLUETOOTH_PERMISSION_NEEDED){
                if(resultCode== Activity.RESULT_OK){
                    //setBluetoothStatus(true);
                    BLEManager.requestLocationPermissions(this,getApplicationContext());
                }
            }
        }catch (Exception error){

        }
    }

    public void connectGATT(String address){
        Bundle args = new Bundle();
        args.putString(BLEService.EXTRA_ADDRESS,address);
        this.broadcastBLE.sendBroadcast(BLEService.TYPE_CONNECT_GATT,args);
    }

    @Override
    public void MessageReceivedThroughBroadcastManager(String channel, String type, Bundle args) {
        if(BLEService.CHANNEL.equals(channel)){
            if(BLEService.TYPE_NEW_DEVICE.equals(type)){
                final ArrayList<ScanResult> scanResults = args.getParcelableArrayList(BLEService.EXTRA_DEVICES);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ListView listView=(ListView)findViewById(R.id.devices_list_id);
                            BluetoothListAdapter adapter=new BluetoothListAdapter(getApplicationContext(),scanResults, mainActivity);
                            listView.setAdapter(adapter);
                        }catch (Exception error){

                        }
                    }
                });
            } else if(BLEService.TYPE_CONNECTED_GATT.equals(type)){
                Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
                String address = args.getString(BLEService.EXTRA_ADDRESS);
                setFragment(ServicesList.newInstance(address));
            } else if(BLEService.TYPE_DISCONNECTED_GATT.equals(type)){
                Toast.makeText(this,"Disconnected",Toast.LENGTH_LONG).show();
            } else if (BLEService.TYPE_DISCOVERED_SERVICES.equals(type)){
                final ArrayList<BluetoothGattService> services = args.getParcelableArrayList(BLEService.EXTRA_SERVICES);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ListView listView=(ListView)findViewById(R.id.services_list_id);
                            ServiceListAdapter adapter=new ServiceListAdapter(getApplicationContext(),services, mainActivity);
                            listView.setAdapter(adapter);
                        }catch (Exception error){

                        }
                    }
                });
            }
        }
    }

    private void setFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void ErrorAtBroadcastManager(final Exception error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                androidx.appcompat.app.AlertDialog.Builder builder=
                        new androidx.appcompat.app.AlertDialog.
                                Builder(MainActivity.this);
                builder.setTitle("BM Error")
                        .setMessage(error.getMessage())
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO
                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(broadcastBLE!=null){
            this.broadcastBLE.unRegister();
        }
        unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
