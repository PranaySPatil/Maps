package com.example.pranay.maps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class GeofenceService extends Service implements ResultCallback<Status>, ConnectionCallbacks, OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;
    private HashSet<String> indices;

    public GeofenceService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildGoogleApiClient();
        mGeofenceList = new ArrayList<Geofence>();
        mGeofencePendingIntent = null;
        indices = new HashSet<>();
        SharedPreferences sp = getSharedPreferences("markers", MODE_PRIVATE);
        SharedPreferences.Editor edt = sp.edit();

        Gson gson = new Gson();
        int loc_count = Integer.parseInt(sp.getString("loc_count", "0"));
        indices = (HashSet<String>) sp.getStringSet("indices", null);
        Log.i("Setting points", " " + loc_count);
        if(indices != null && !indices.isEmpty()) {
            Position p;
            for (String x : indices) {
                p = new Position();
                String json = sp.getString("MyObject" + x, "");
                Log.i("Setting points", json + " " + x);
                p = gson.fromJson(json, Position.class);
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(x + "")
                        .setCircularRegion(
                                Double.parseDouble(p.getLatitude()),
                                Double.parseDouble(p.getLongitude()),
                                p.getRadius()
                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(30000)
                        .build());
            }
        }
        else
            stopSelf();
        /*int loc_count = Integer.parseInt(sp.getString("loc_count", "0"));
        Log.i("Setting points", " " + loc_count);
        if(loc_count!=0){
            Gson gson = new Gson();
            Position p;
            for(int i=0;i<loc_count;i++) {
                p = new Position();
                String json = sp.getString("MyObject"+i, "");
                p = gson.fromJson(json, Position.class);
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(i + "")
                        .setCircularRegion(
                                Double.parseDouble(p.getLatitude()),
                                Double.parseDouble(p.getLongitude()),
                                p.getRadius()
                )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setLoiteringDelay(30000)
                        .build());

            }
            }
        else
            stopSelf();*/
        mGoogleApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onResult(Status status) {
        if(status.isSuccess())
        {
            Log.i("status","HUA");
        }
        else
        {
            Log.i("status","NAHI HUA");
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        Log.i("onConnected","geofen request");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);

        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        Log.i("onConnected", "adding pending intent");
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent("notify2");

        /*return PendingIntent.getService(this, (int)Math.random()*100, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);*/
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }
    protected synchronized void buildGoogleApiClient() {
        Log.i("onConnected","Building g API");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("onConnected", "Connected to GoogleApiClient");
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
