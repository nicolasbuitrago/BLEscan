package com.example.blescan.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.blescan.R;
import com.example.blescan.ble.LogBLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Log extends Fragment {

    LogBLE log;

    public Log() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        log = LogBLE.getInstance();
        ListView list = view.findViewById(R.id.list_log);
        ArrayAdapter<String> logs = new ArrayAdapter<>(view.getContext(),android.R.layout.simple_list_item_1,log.getLogs());
        list.setAdapter(logs);

        return view;
    }

}
