package com.example.blescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.example.blescan.adapters.BluetoothListAdapter;
import com.example.blescan.ble.BLEManager;
import com.example.blescan.ble.IBLEManagerCaller;
import com.example.blescan.ble.BLEService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements IBLEManagerCaller {

    public BLEManager bleManager;
    private MainActivity mainActivity;

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
                if(bleManager!=null){
                    bleManager.scanDevices();
                }
            }
        });
        bleManager=new BLEManager(this,this);
        if(!bleManager.isBluetoothOn()){
            bleManager.enableBluetoothDevice(this, 1001);
        }else{
            bleManager.requestLocationPermissions(this,1002);
        }
        mainActivity=this;

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
        if (requestCode == 1002) {
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
                        .setMessage("Camera and Location permissions must be granted in order to execute the app")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
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

            if(requestCode==1001){
                if(resultCode!= Activity.RESULT_OK){

                }else{
                    bleManager.requestLocationPermissions(this,1002);

                }
            }


        }catch (Exception error){

        }
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try{
                    ListView listView=(ListView)findViewById(R.id.devices_list_id);
                    BluetoothListAdapter adapter=new BluetoothListAdapter(getApplicationContext(),bleManager.scanResults,mainActivity);
                    listView.setAdapter(adapter);
                }catch (Exception error){

                }

            }
        });

    }

}
