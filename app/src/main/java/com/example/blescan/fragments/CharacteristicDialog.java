package com.example.blescan.fragments;


import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.blescan.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CharacteristicDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacteristicDialog extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CHARACTERISTIC = "characteristic";

    // TODO: Rename and change types of parameters
    private BluetoothGattCharacteristic charactersitic;

    // Use this instance of the interface to deliver action events
    CharacteristicDialogListener listener;

    public CharacteristicDialog() {
        // Required empty public constructor
    }

    public static CharacteristicDialog newInstance(BluetoothGattCharacteristic characteristic) {
        CharacteristicDialog fragment = new CharacteristicDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHARACTERISTIC, characteristic);
        fragment.setArguments(args);
        return fragment;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (CharacteristicDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("CharacteristicDialog must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        final EditText newValue = requireActivity().findViewById(R.id.dialog_characteristic_new_value);
        TextView value = requireActivity().findViewById(R.id.dialog_characteristic_value);
        byte[] v  = charactersitic.getValue();
        if(v!=null){
            value.setText(new String(v));
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_characteristic_edit, null))
                // Add action buttons
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        listener.modifyCharacteristic(charactersitic,newValue.getText().toString().getBytes());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //LoginDialogFragment.this.getDialog().cancel();
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            charactersitic = getArguments().getParcelable(ARG_CHARACTERISTIC);
        }
    }
/*


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_characteristic_dialog, container, false);
    }*/

    public interface CharacteristicDialogListener {
        void modifyCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data);
    }

}
