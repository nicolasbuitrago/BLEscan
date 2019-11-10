package com.example.blescan.adapters;


import android.bluetooth.BluetoothGattCharacteristic;
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

        TextView txtTitle = (TextView) rowView.findViewById(R.id.characteristic_list_item_text_view);
        txtTitle.setText(characteristics.get(position).getUuid().toString());

        return rowView;
    }
}