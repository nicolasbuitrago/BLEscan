package com.example.blescan.adapters;


import android.bluetooth.le.ScanResult;
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

import java.util.ArrayList;

public class BluetoothListAdapter extends ArrayAdapter<ScanResult> {
    private final Context context;
    private MainActivity mainActivity;
    private ArrayList<ScanResult> scanResultList;

    public BluetoothListAdapter(@NonNull Context context, ArrayList<ScanResult> scanResultList, MainActivity mainActivity) {
        super(context, R.layout.device_list_item,scanResultList);
        this.context = context;
        this.mainActivity=mainActivity;
        this.scanResultList = scanResultList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        View rowView= inflater.inflate(R.layout.device_list_item, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.device_list_item_text_view);
        txtTitle.setText(scanResultList.get(position).getDevice().getAddress()+"");

        String deviceName=scanResultList.get(position).getDevice().getName();
        TextView deviceNameTxtView = (TextView) rowView.findViewById(R.id.device_list_item_text_view2);
        deviceNameTxtView.setText(deviceName);
        String signal = scanResultList.get(position).getRssi()+"";
        TextView signalTxtView = (TextView) rowView.findViewById(R.id.device_list_item_text_view3);
        signalTxtView.setText(signal);

        /*txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address=((TextView) view.findViewById(R.id.device_list_item_text_view)).getText()+"";
                Toast.makeText(context,"selected address: "+address,Toast.LENGTH_LONG).show();
                mainActivity.connectGATT(address);
                //mainActivity.bleManager.connectToGATTServer(mainActivity.bleManager.getByAddress(address));
            }
        });*/

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String address=((TextView) view.findViewById(R.id.device_list_item_text_view)).getText()+"";
                Toast.makeText(context,"selected address: "+address,Toast.LENGTH_LONG).show();
                mainActivity.connectGATT(address);
                return false;
            }
        });

        return rowView;
    }
}