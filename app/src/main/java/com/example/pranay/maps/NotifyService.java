package com.example.pranay.maps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class NotifyService extends Service {
    private SharedPreferences sp;
    private SharedPreferences.Editor edt;
    private static int loc_count;
    private PendingIntent pendingIntent;
    LocationManager lm;
    public NotifyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in air plan mode it will be null
        if(netInfo != null && netInfo.isConnected())
            stopSelf();
            sp = getSharedPreferences("markers", MODE_PRIVATE);
        edt = sp.edit();
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        setProximity();
        return super.onStartCommand(intent, flags, startId);
    }

    private void setProximity() {
        loc_count = Integer.parseInt(sp.getString("loc_count", "0"));
        Log.i("Setting points", " " + loc_count);
        if(loc_count!=0){
            String lat = "";
            String lng = "";
            String rad = "";
            for(int i=0;i<loc_count;i++){
                lat = sp.getString("latitude" + i, "0");
                lng = sp.getString("longitude"+i,"0");
                rad = sp.getString("radius"+i, "0");
                Log.i("Setting points", lat + " " + lng + ", " + loc_count);
                Intent in = new Intent("notify");
                in.putExtra("lat", lat);
                in.putExtra("lng", lng);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, i, in, 0);
                lm.addProximityAlert(Double.parseDouble(lat), Double.parseDouble(lng), Float.parseFloat(rad), -1, pendingIntent);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
