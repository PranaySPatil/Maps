package com.example.pranay.maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CatchGeoTrans extends BroadcastReceiver {
    public CatchGeoTrans() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent("GeofenceService"));
    }
}
