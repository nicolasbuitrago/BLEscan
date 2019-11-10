package com.example.blescan.adapters;


import android.bluetooth.BluetoothGattService;
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

public class ServiceListAdapter extends ArrayAdapter<BluetoothGattService> {
    private final Context context;
    private MainActivity mainActivity;
    private ArrayList<BluetoothGattService> services;

    public ServiceListAdapter(@NonNull Context context, ArrayList<BluetoothGattService> services, MainActivity mainActivity) {
        super(context, R.layout.service_list_item,services);
        this.context = context;
        this.mainActivity=mainActivity;
        this.services = services;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mainActivity.getLayoutInflater();

        View rowView= inflater.inflate(R.layout.service_list_item, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.service_list_item_text_view);
        txtTitle.setText(services.get(position).getUuid().toString());

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String uuid=((TextView) view.findViewById(R.id.service_list_item_text_view)).getText()+"";
                Toast.makeText(context,"selected UUID: "+uuid,Toast.LENGTH_LONG).show();

                return false;
            }
        });

        return rowView;
    }
}