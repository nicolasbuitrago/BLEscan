package com.example.blescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.blescan.adapters.BluetoothListAdapter;
import com.example.blescan.ble.BLEManager;
import com.example.blescan.ble.IBLEManagerCaller;
import com.example.blescan.ble.BLEService;
import com.example.blescan.broadcast.BroadcastManager;
import com.example.blescan.broadcast.IBroadcastManagerCaller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IBroadcastManagerCaller {

    private MainActivity mainActivity;
    private BroadcastManager broadcastBLE;
    private boolean bluetoothEnabled;

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

        bluetoothEnabled = BLEManager.RequestBluetoothDeviceEnable(this);

        mainActivity=this;
        initializeBroadcastManager();
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
                    BLEManager.requestLocationPermissions(this,getApplicationContext());
                }
            }
        }catch (Exception error){

        }
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
            }
        }
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
        super.onDestroy();
    }
}
