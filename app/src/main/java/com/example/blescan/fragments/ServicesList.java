package com.example.blescan.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.blescan.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ServicesList extends Fragment {

    private static final String ARG_ADDRESS = "address";

    private String address;


    public ServicesList() {
        // Required empty public constructor
    }

    public static ServicesList newInstance(String address) {
        ServicesList fragment = new ServicesList();
        Bundle args = new Bundle();
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString(ARG_ADDRESS,"MAC");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_services_list, container, false);
        TextView addressTextView = view.findViewById(R.id.services_fragment_MAC);
        addressTextView.setText(address);
        return view;
    }



}
