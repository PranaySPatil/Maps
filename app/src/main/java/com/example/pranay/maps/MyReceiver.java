package com.example.pranay.maps;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }
    String notificationTitle;
    String notificationContent;
    String tickerMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        boolean proximity_entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
        double lat = intent.getDoubleExtra("lat", 0);

        double lng = intent.getDoubleExtra("lng", 0);

        String strLocation = Double.toString(lat)+","+Double.toString(lng);

        if(proximity_entering){
            Toast.makeText(context,"Entering the region"  , Toast.LENGTH_LONG).show();
            notificationTitle = "Proximity - Entry";
            notificationContent = "Entered the region:" + strLocation;
            tickerMessage = "Entered the region:" + strLocation;
        }else{
            Toast.makeText(context,"Exiting the region"  ,Toast.LENGTH_LONG).show();
            notificationTitle = "Proximity - Exit";
            notificationContent = "Exited the region:" + strLocation;
            tickerMessage = "Exited the region:" + strLocation;
        }
       /* Notification noti = new Notification.Builder(context)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setTicker(tickerMessage)
                .setSmallIcon(R.drawable.ic_plusone_standard_off_client)
                .build();*/
        Notification noti = new Notification.Builder(context)
                .setContentTitle(notificationTitle)
                .setContentText("I am here")
                .setSmallIcon(R.drawable.ic_plusone_standard_off_client)
                .build();
        NotificationManager nm = (NotificationManager)(context).getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) (Math.random()*100), noti);

    }
}
