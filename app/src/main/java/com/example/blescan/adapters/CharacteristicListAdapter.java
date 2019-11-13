package com.example.blescan.adapters;


import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.blescan.MainActivity;
import com.example.blescan.R;
import com.example.blescan.ble.BLEManager;

import java.util.ArrayList;

public class CharacteristicListAdapter extends ArrayAdapter<BluetoothGattCharacteristic> {
    private final Context context;
    private MainActivity mainActivity;
    private ArrayList<BluetoothGattCharacteristic> characteristics;

    public CharacteristicListAdapter(@NonNull Context context, ArrayList<BluetoothGattCharacteristic> characteristics, MainActivity mainActivity) {
        super(context, R.layout.characteristic_list_item, characteristics);
        this.context = context;
        this.mainActivity=mainActivity;
        this.characteristics = characteristics;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        View rowView= inflater.inflate(R.layout.characteristic_list_item, null, true);

        final BluetoothGattCharacteristic characteristic = characteristics.get(position);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.characteristic_list_item_text_view);
        txtTitle.setText(characteristic.getUuid().toString());

        txtTitle = (TextView) rowView.findViewById(R.id.characteristic_list_item_text_view2);
        String perm = getPermissions(characteristic)+" ";
        txtTitle.setText(perm);

        txtTitle = (TextView) rowView.findViewById(R.id.characteristic_list_item_text_view3);
        perm = "";
        for (BluetoothGattDescriptor descriptor:characteristic.getDescriptors()) {
            perm += descriptor.getUuid().toString()+"\n";
        }
        txtTitle.setText(perm);

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String uuid=characteristic.getUuid().toString();
                Toast.makeText(context,"selected UUID: "+uuid,Toast.LENGTH_LONG).show();
                mainActivity.characteristicAction(characteristic);
                return false;
            }
        });

        return rowView;
    }

    private String getPermissions(BluetoothGattCharacteristic characteristic){
        boolean r = BLEManager.isCharacteristicReadable(characteristic),
                w = BLEManager.isCharacteristicWriteable(characteristic),
                n = BLEManager.isCharacteristicNotifiable(characteristic);
        if(r && w && n){
            return "R/W/N";
        }else if(r && w){
            return "R/W";
        }else if(r && n){
            return "R/N";
        }else if(w && n){
            return "W/N";
        }else if(r){
            return "R";
        }else if(w){
            return "W";
        }else if(n){
            return "N";
        }else{
            return "-";
        }
    }
}