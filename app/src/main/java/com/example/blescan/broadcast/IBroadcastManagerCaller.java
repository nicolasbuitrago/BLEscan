package com.example.blescan.broadcast;

public interface IBroadcastManagerCaller {

    void MessageReceivedThroughBroadcastManager(
            String channel, String type, String message);

    void ErrorAtBroadcastManager(Exception error);
}
