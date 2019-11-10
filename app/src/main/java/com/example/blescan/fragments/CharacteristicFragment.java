package com.example.blescan.fragments;


import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.blescan.MainActivity;
import com.example.blescan.R;
import com.example.blescan.adapters.CharacteristicListAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CharacteristicFragment extends Fragment {


    private static final String ARG_ADDRESS = "address";
    private static final String ARG_CHARACTERISTICS = "characteristics";
    private static final String ARG_MAIN= "mainActivity";

    private String address;
    private ArrayList<BluetoothGattCharacteristic> characteristics;
    private MainActivity mainActivity;


    public CharacteristicFragment() {
        // Required empty public constructor
    }

    public static CharacteristicFragment newInstance(String address) {
        CharacteristicFragment fragment = new CharacteristicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, address);
        //args.putParcelableArrayList(ARG_CHARACTERISTICS, characteristics);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString(ARG_ADDRESS,"MAC");
            //characteristics = getArguments().getParcelableArrayList(ARG_CHARACTERISTICS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_characteristic, container, false);
        TextView addressTextView = view.findViewById(R.id.characteristic_fragment_MAC);
        addressTextView.setText(address);
//        ListView listView=(ListView)view.findViewById(R.id.characteristic_list_id);
//        CharacteristicListAdapter adapter=new CharacteristicListAdapter(getContext(),characteristics, mainActivity);
//        listView.setAdapter(adapter);
        return view;
    }

}
