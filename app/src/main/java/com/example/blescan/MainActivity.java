package com.example.blescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.example.blescan.adapters.CharacteristicListAdapter;
import com.example.blescan.adapters.ServiceListAdapter;
import com.example.blescan.ble.BLEManager;
import com.example.blescan.ble.BLEService;
import com.example.blescan.broadcast.BroadcastManager;
import com.example.blescan.broadcast.IBroadcastManagerCaller;
import com.example.blescan.fragments.CharacteristicFragment;
import com.example.blescan.fragments.DeviceList;
import com.example.blescan.fragments.Log;
import com.example.blescan.fragments.ServicesList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
    private FragmentManager fragmentManager;
    private Fragment fragment;
    private DeviceList devicesFragment;
    private ServicesList servicesList;
    private CharacteristicFragment characteristicFragment;
    private Log logFragment;
    private String address;
    private ArrayList<ScanResult> oldScans;
    private BluetoothListAdapter adapter;

    private static final String TAG_DEVICES ="devices";
    private static final String TAG_SERVICES ="services";
    private static final String TAG_CHARACTERISTIC ="characteristics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            Intent intent = new Intent(getApplicationContext(), BLEService.class);
            startForegroundService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }

        fragmentManager = getSupportFragmentManager();

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcastBLE.sendBroadcast(BLEService.TYPE_SCAN_DEVICES,null);
            }
        });*/

        //FLOATING MENU EVENTS
        //START SCAN EVENT
        com.getbase.floatingactionbutton.FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Starting LE scan", Snackbar.LENGTH_LONG).show();
                broadcastBLE.sendBroadcast(BLEService.TYPE_SCAN_DEVICES,null);
            }
        });

        //STOP SCAN EVENT
        com.getbase.floatingactionbutton.FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Stopping LE scan", Snackbar.LENGTH_LONG).show();
                broadcastBLE.sendBroadcast(BLEService.TYPE_STOP_SCAN,null);
            }

        });

        //CONECT EVENT, CONNECT TO THE LAST SELECTED ITEM
        com.getbase.floatingactionbutton.FloatingActionButton fab3 = findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ScDevice selectedItem = (ScDevice) listView.getItemAtPosition(anterior);
                Snackbar.make(view, "Connecting to ", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
//                Toast.makeText(getApplicationContext(),"Connecting to: " + selectedItem.Name, Toast.LENGTH_LONG).show();
//                if(connect()){
//                    //log
//                }
            }
        });

        //DISCONECTION EVENT
        com.getbase.floatingactionbutton.FloatingActionButton fab4 = findViewById(R.id.fab4);
        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcastBLE.sendBroadcast(BLEService.TYPE_DISCONNECT_DEVICE,null);
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
        //characteristicFragment =
        fragmentTransaction.add(R.id.fragment_container, devicesFragment, TAG_DEVICES);
        fragmentTransaction.commit();
        fragment = devicesFragment;

        mainActivity=this;
        initializeBroadcastManager();
        initializeBluetoothReceiver();

        oldScans = new ArrayList<>();


        this.broadcastBLE.sendBroadcast(BLEService.TYPE_GET_CONNECTION,null);
        try {
            Intent intent = new Intent(getApplicationContext(), BLEService.class);
            startService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
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
                BLEManager.RequestBluetoothDeviceEnable(this);
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
        }else if (id == R.id.action_log){
            try {
                logFragment = new Log();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(fragment);
                transaction.add(R.id.fragment_container, logFragment);
                transaction.commit();
                //fragment = log;
                transaction.addToBackStack("l");

            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        } else if(id == R.id.action_start_service){
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
                }else{
                    finish();
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

    public void showCharacteristics(BluetoothGattService service) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(servicesList);
        transaction.add(R.id.fragment_container,characteristicFragment,TAG_CHARACTERISTIC);
        fragment = characteristicFragment;
        transaction.commit();
        transaction.addToBackStack("ch");

        //setFragment(characteristicFragment);
        final ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>(service.getCharacteristics());
        Bundle args = new Bundle();
        args.putParcelableArrayList(BLEService.EXTRA_CHARACTERISTICS,characteristics);
        this.broadcastBLE.sendBroadcast(BLEService.TYPE_SEND_CHARACTERISTICS,args);
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
                            if(adapter==null){
                                ListView listView = (ListView) findViewById(R.id.devices_list_id);
                                adapter = new BluetoothListAdapter(getApplicationContext(), oldScans, mainActivity);
                                listView.setAdapter(adapter);
                            }
                            oldScans.clear();
                            oldScans.addAll(scanResults);
                            adapter.notifyDataSetChanged();
                        }catch (Exception error){

                        }
                    }
                });
            } else if(BLEService.TYPE_CONNECTED_GATT.equals(type)){
                Toast.makeText(this,"Connected",Toast.LENGTH_LONG).show();
                String address = args.getString(BLEService.EXTRA_ADDRESS);
                this.address=address;
                connectedGatt();

                //this.broadcastBLE.sendBroadcast(BLEService.TYPE_DISCOVER_SERVICES,null);
            } else if(BLEService.TYPE_DISCONNECTED_GATT.equals(type)){
                Toast.makeText(this,"Disconnected",Toast.LENGTH_LONG).show();
                if(fragment instanceof CharacteristicFragment){
                    fragmentManager.beginTransaction().remove(characteristicFragment).commit();
                }
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.show(devicesFragment);
                transaction.remove(servicesList);
                fragment = devicesFragment;

                transaction.commit();
                address=null;
            } else if (BLEService.TYPE_DISCOVERED_SERVICES.equals(type)){
                final ArrayList<BluetoothGattService> services = args.getParcelableArrayList(BLEService.EXTRA_SERVICES);
                String a = args.getString(BLEService.EXTRA_ADDRESS);
                if(a!=null) {
                    this.address = a;
                    this.characteristicFragment = CharacteristicFragment.newInstance(a);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ListView listView = (ListView) findViewById(R.id.services_list_id);
                                ServiceListAdapter adapter = new ServiceListAdapter(getApplicationContext(), services, mainActivity);
                                listView.setAdapter(adapter);
                            } catch (Exception error) {

                            }
                        }
                    });
                }
            } else if(BLEService.TYPE_SHOW_CHARACTERISTICS.equals(type)){
                final ArrayList<BluetoothGattCharacteristic> characteristics = args.getParcelableArrayList(BLEService.EXTRA_CHARACTERISTICS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            ListView listView=(ListView)findViewById(R.id.characteristic_list_id);
                            CharacteristicListAdapter adapter=new CharacteristicListAdapter(getApplicationContext(),characteristics, mainActivity);
                            listView.setAdapter(adapter);
                        }catch (Exception error){
                            error.printStackTrace();
                        }
                    }
                });
            } else if(BLEService.TYPE_CHARACTERISTIC_CHANGED.equals(type)){
                final BluetoothGattCharacteristic characteristic = args.getParcelable(BLEService.EXTRA_CHARACTERISTIC);
                String value = args.getString(BLEService.EXTRA_VALUE);
                AlertDialog.Builder builder=new AlertDialog.Builder(this)
                        .setTitle("Characteristic changed")
                        .setMessage("Value of characteristic with UUID: "+characteristic.getUuid().toString()
                                +" changed. Value = "+value)
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
            }else if (BLEService.TYPE_SHOW_CHARACTERISTIC.equals(type)){
                BluetoothGattCharacteristic characteristic = args.getParcelable(BLEService.EXTRA_CHARACTERISTIC);
                String value = args.getString(BLEService.EXTRA_VALUE);
                showCharacteristicValue(characteristic,value);
            }else if (BLEService.TYPE_RESPONSE_CONNECTION.equals(type)){
                this.address = args.getString(BLEService.EXTRA_ADDRESS);
                if(this.address!=null) {
                    connectedGatt();
                    this.broadcastBLE.sendBroadcast(BLEService.TYPE_DISCOVER_SERVICES,null);
                }
            }
            else if (BLEService.TYPE_SUCCESS.equals(type)){
                String msg = args.getString(BLEService.EXTRA_MESSAGE,"msg");
                alert("Success",msg);
            } else if (BLEService.TYPE_ERROR.equals(type)){
                String msg = args.getString(BLEService.EXTRA_MESSAGE,"msg");
                alert("Error",msg);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(logFragment!=null){
            fragmentManager.beginTransaction().remove(logFragment).show(fragment).commit();
            logFragment=null;
        }else if(fragment instanceof ServicesList){
            fragmentManager.beginTransaction().remove(servicesList).show(devicesFragment).commit();
            fragment = devicesFragment;
        } else if(fragment instanceof CharacteristicFragment){
            fragmentManager.beginTransaction().remove(characteristicFragment).show(servicesList).commit();
            fragment= servicesList;
        }
        super.onBackPressed();
    }

    private void connectedGatt(){
        this.servicesList = ServicesList.newInstance(address);
//        setFragment(servicesList);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(devicesFragment);
        transaction.add(R.id.fragment_container,servicesList,TAG_SERVICES);
        fragment = servicesList;
        transaction.commit();
        transaction.addToBackStack("b1");
    }

    public void characteristicAction(final BluetoothGattCharacteristic characteristic){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("Characteristic Action")
                .setMessage("You want Read or write this characteristic?")
                .setPositiveButton("Read", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(BLEManager.isCharacteristicReadable(characteristic)) {
                            readCharacteristic(characteristic);
                        } else{
                            alert("Error", "Access denied.  Reading permissions required");
                        }
                        dialog.cancel();
                    }
                }).setNeutralButton("Write", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(BLEManager.isCharacteristicWriteable(characteristic)) {
                            changeCharacteristicValue(characteristic);
                        }else{
                            alert("Error", "Access denied. Writing permissions required");
                        }
                        dialogInterface.cancel();
                    }
                });
        builder.show();
    }

    public void showCharacteristicValue(BluetoothGattCharacteristic characteristic, String value){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle("Characteristic Value")
                .setMessage(value)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    public void changeCharacteristicValue(final BluetoothGattCharacteristic characteristic){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Write characteristic");
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String v = input.getText().toString();
                //byte[] data = BLEManager.hexStringToByteArray(v);
                modifyCharacteristic(characteristic,v.getBytes());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void alert(String title, String msg){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    /*private void setFragment(Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }*/

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

    public void modifyCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        Bundle args = new Bundle();
        args.putByteArray(BLEService.EXTRA_VALUE,data);
        args.putParcelable(BLEService.EXTRA_CHARACTERISTIC,characteristic);
        this.broadcastBLE.sendBroadcast(BLEService.TYPE_WRITE_CHARACTERISTIC,args);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        Bundle args = new Bundle();
        args.putParcelable(BLEService.EXTRA_CHARACTERISTIC,characteristic);
        this.broadcastBLE.sendBroadcast(BLEService.TYPE_READ_CHARACTERISTIC,args);
    }
}
