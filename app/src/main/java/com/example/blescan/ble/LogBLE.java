package com.example.blescan.ble;

import java.util.ArrayList;

public class LogBLE {

    private static LogBLE mInstance;
    private ArrayList<String> log;

    private LogBLE() {
        this.log = new ArrayList<>();
    }

    public static LogBLE getInstance(){
        if(mInstance == null){
            mInstance = new LogBLE();
        }
        return mInstance;
    }

    public void add(String msg){
        this.log.add(msg);
    }
    public void add(String tag, String msg){
        this.log.add(tag+": "+msg);
    }

    public static String ERROR = "Error BLE: ";
    public static String SCAN_ERROR = "Error al hacer escaneo";
    public static String SCAN_OK = "Escaneo completo";
    public static String CONNECTION_ERROR = "Error en la conexión al BLE";
    public static String CONNECTION_OK = "Conexión al BLE establecida";

}
