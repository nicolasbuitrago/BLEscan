package com.example.blescan.broadcast;

import android.os.Bundle;

public interface IBroadcastManagerCaller {

    void MessageReceivedThroughBroadcastManager(
            String channel, String type, Bundle args);

    void ErrorAtBroadcastManager(Exception error);
}
