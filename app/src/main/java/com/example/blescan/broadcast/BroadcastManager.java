package com.example.blescan.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class BroadcastManager extends BroadcastReceiver {

    protected Context context;
    private String channel;
    protected IBroadcastManagerCaller caller;

    public BroadcastManager(Context context,
                            String channel,
                            IBroadcastManagerCaller caller) {
        this.context = context;
        this.channel = channel;
        this.caller = caller;
        initializeBroadcast();
    }

    public void initializeBroadcast(){
        try{
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(channel);
            context.registerReceiver(this,intentFilter);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }

    public void unRegister(){
        try{
            context.unregisterReceiver(this);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle payload=intent.getExtras().getBundle("payload");
        String type=intent.getExtras().getString("type");
        caller.MessageReceivedThroughBroadcastManager(this.channel,type,payload);
    }

    public void sendBroadcast(String type, Bundle args){
        try{
            Intent intentToBeSent=new Intent();
            intentToBeSent.setAction(channel);
            intentToBeSent.putExtra("payload",args);
            intentToBeSent.putExtra("type",type);
            context.sendBroadcast(intentToBeSent);
        }catch (Exception error){
            caller.ErrorAtBroadcastManager(error);
        }
    }
}
