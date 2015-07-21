package com.example.pranay.maps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.widget.Toast.makeText;


public class MainActivity extends FragmentActivity {

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private HashMap<Marker, Circle> map;
    ArrayList<Position> positions;
    private GoogleMap gMap;
    private float zoom = 12;
    private SharedPreferences sp;
    private SharedPreferences.Editor edt;
    private static int loc_count;
    private PendingIntent pendingIntent;
    LocationManager lm;
    private Index ind;
    private HashSet<String> indices;
    private AutoCompleteTextView autocompleteView;
    private ArrayList<String> addressLineList;
    private ArrayAdapter<String> adapter;
    private int selectedAddress;
    private List<Address> list;
    private float ZOOM = 15;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        indices = new HashSet<String>();
        map = new HashMap<>();
        sp = getSharedPreferences("markers", MODE_PRIVATE);
        edt = sp.edit();
        positions = new ArrayList<Position>();
        addressLineList = new ArrayList<>();

        if(servicesOk())
        {
            setContentView(R.layout.map);
            autocompleteView = (AutoCompleteTextView) findViewById(R.id.tv);
            if (initMap()) {
                makeText(this, "gMap available", Toast.LENGTH_LONG).show();
                gMap.setMyLocationEnabled(true);
                init_loc();
            } else {
                makeText(this, "gMap not available", Toast.LENGTH_LONG).show();
            }
        } else
            setContentView(R.layout.activity_main);
        setPoints2();
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                gMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                LayoutInflater li = LayoutInflater.from(MainActivity.this);
                View promptsView = li.inflate(R.layout.dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setView(promptsView);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.et);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Set",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Float rad = Float.parseFloat(userInput.getText().toString());
                                        Position p = new Position();
                                        p.setLatitude(latLng.latitude + "");
                                        p.setLongitude(latLng.longitude + "");
                                        map.put(drawMarker(latLng), drawCircle(latLng, rad));
                                        p.setRadius(rad);
                                        p.setPos(loc_count++);
                                        positions.add(p);
                                        write_pos2(p);
                                        startService(new Intent(MainActivity.this, GeofenceService.class));
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Are you sure you want to delete this location?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Position p = new Position();
                                p.setLatitude(marker.getPosition().latitude + "");
                                p.setLongitude(marker.getPosition().longitude + "");
                                if (positions.contains(p)) {
                                    p = positions.get(positions.indexOf(p));
                                    edt.remove("MyObject" + p.getPos());
                                    Log.i("indices", indices.contains(p.getPos()) + "");
                                    indices.remove(p.getPos() + "");
                                    edt.putStringSet("indices", indices);
                                    edt.commit();
                                    positions.remove(p);
                                    startService(new Intent(MainActivity.this, GeofenceService.class));

                                } else {
                                    Toast.makeText(MainActivity.this, "Not Equal", Toast.LENGTH_LONG).show();
                                }
                                marker.remove();
                                map.remove(marker).remove();

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
                return false;
            }
        });

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,addressLineList);
        Log.i("Address line fine", "");
        autocompleteView.setAdapter(adapter);


        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                                            @Override
                                                            public void onItemClick(AdapterView<?> parent,
                                                                                    View view, int position, long id) {
                                                                selectedAddress = position;
                                                                Log.i("aaaaaa", selectedAddress + "");
                                                            }
                                                        });

        autocompleteView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.i("string", "" + s);
                new LocationSearchTask().execute(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {


            }

        });
        startService(new Intent(this, GeofenceService.class));
    }

    class LocationSearchTask extends AsyncTask<String, Void, ArrayList<String>> {
        private String temp;

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            ArrayList<String> tempAddressLine = new ArrayList<String>();
            Geocoder gc = new Geocoder(MainActivity.this);
            try {

                list = gc.getFromLocationName(params[0].toString(), 5);
            } catch (IOException e) {
                makeText(MainActivity.this, "IO EXCEPTION", Toast.LENGTH_LONG).show();
            }
            if (list.size() > 0) {

                for (int j = 0; j < list.size(); j++) {

                    temp = "";
                    for (int i = 0; i <= list.get(j).getMaxAddressLineIndex(); i++) {
                        if (i != list.get(j).getMaxAddressLineIndex())
                            temp += list.get(j).getAddressLine(i) + ", ";
                        else
                            temp += list.get(j).getAddressLine(i);
                    }
                    tempAddressLine.add(temp);
                }
            }
            return tempAddressLine;

        }

        @Override
        protected void onPostExecute(ArrayList<String> tempAddressLine) {
            Log.i("jitu", "start");
            if (tempAddressLine != null) {
                Log.i("data from server", tempAddressLine.toString());

                adapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_dropdown_item_1line,tempAddressLine);

                autocompleteView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                for (int i = 0; i < adapter.getCount(); i++)
                    Log.i("adapter", adapter.getItem(i));
                Log.i("jitu", "end");
            }

        }

    }

    //  Sets map to your Current location
    private void init_loc() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        GPSTracker gps = new GPSTracker(this);
        if(gps.canGetLocation){
            Toast.makeText(this, "Inside init_loc_if",Toast.LENGTH_LONG).show();
            Double latPoint=gps.getLatitude();
            Double lngPoint =gps.getLongitude();
        LatLng latLng = new LatLng(latPoint, lngPoint);
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gMap.animateCamera(CameraUpdateFactory.zoomTo(20));}

    }

    // Verifies the availability of Google  Play Services
    public boolean servicesOk()
    {
        int isAvial= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(isAvial == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if(GooglePlayServicesUtil.isUserRecoverableError(isAvial))
        {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvial, this, 9001);
            dialog.show();
        }
        else
        {
            makeText(this, "SOMETHING WENT WRONG", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    // Loads the map fragment
    private boolean initMap()
    {
        if(gMap == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            gMap = mapFragment.getMap();

        }
        return gMap!=null;
    }

    // Searches the location entere, onClick of search
    public void geoLocate(View v){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        goToLocation(list.get(selectedAddress).getLatitude(), list.get(selectedAddress).getLongitude(), ZOOM);}
    }



    // Go to the map location searched
    private void goToLocation(double lat, double lon, float zoom)
    {
        LatLng l = new LatLng(lat, lon);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(l, zoom);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(l);
        // gMap.addMarker(markerOptions);
        gMap.moveCamera(update);
    }

    // Writes marked locations in xml
    private void write_pos2(Position p) {
       /* if(ind == null)
            ind = new Index();*/
//        ind.indices.add(loc_count);
        if(indices == null)
            indices = new HashSet<>();
        indices.add(p.getPos()+"");
        Gson gson = new Gson();
        String json = gson.toJson(p);
        //String instr = gson.toJson(ind);
        Log.i("SharedPref", loc_count + " ," + p.getLatitude() + "  " + p.getLongitude());
        edt.putString("MyObject" + p.getPos(), json);
        edt.putString("loc_count", loc_count + "");
        edt.putStringSet("indices", indices);
        //edt.putString("indices",instr);
        edt.apply();
    }

    // Loads the previously added markers from xml
    private void setPoints2()
    {
        Gson gson = new Gson();
        indices = (HashSet<String>) sp.getStringSet("indices", null);
        loc_count = Integer.parseInt(sp.getString("loc_count", "0"));
        Log.i("Setting points", " " + loc_count);
        if(indices !=null && !indices.isEmpty()){
            Position p;
            for(String x : indices)
            {
                p = new Position();
                String json = sp.getString("MyObject"+x, "");
                Log.i("Setting points", json+" " + x);
                p = gson.fromJson(json, Position.class);
                positions.add(p);
                map.put(drawMarker(new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude()))),
                        drawCircle(new LatLng(Double.parseDouble(p.getLatitude()), Double.parseDouble(p.getLongitude())),
                                p.getRadius()));
            }
           }
    }

    // Draws circle around the mark
    private Circle drawCircle(LatLng point, float r){
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(point);
        circleOptions.radius(r);  //metres
        circleOptions.strokeColor(Color.BLACK);
        circleOptions.fillColor(0x30ff0000);
        circleOptions.strokeWidth(2);
        return gMap.addCircle(circleOptions);
    }

    // Draws marker
    private Marker drawMarker(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title("Location Coordinates");
        markerOptions.snippet(Double.toString(point.latitude) + "," + Double.toString(point.longitude));
        return gMap.addMarker(markerOptions);
    }


   /* private void deleteMark(final LatLng latLng, final Marker m, final Circle c, final Position p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this location?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        m.remove();
                        c.remove();
                        indices.remove(p.getPos()+"");
                        Log.i("indices",indices.contains(p.getPos())+"");
                        positions.remove(p);
                        //ind.indices.remove(p.getPos());
                        edt.remove("MyObject"+p.getPos());
                        edt.remove("indices");
                        edt.putStringSet("indices", indices);
                        //loc_count-=1;
                        //edt.putString("loc_count", loc_count + "");
                        edt.apply();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }*/
    /*private void setProximity(LatLng latLng, int pos) {
       *//* for(int i=0;i<loc_count;i++)
        {
            String lat = sp.getString("latitude"+i, "0");
            String lon = sp.getString("longitude"+i, "0");
            lm.addProximityAlert(Double.parseDouble(lat), Double.parseDouble(lon),1000, -1, pendingIntent);


        }*//*
        Intent in = new Intent("notify");
        in.putExtra("lat", latLng.latitude);
        in.putExtra("lng", latLng.longitude);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, pos, in, PendingIntent.FLAG_CANCEL_CURRENT);
        lm.addProximityAlert(latLng.latitude, latLng.longitude,1000, -1, pendingIntent);
        startService(new Intent(this, NotifyService.class));
        Toast.makeText(this, "Alert Added", Toast.LENGTH_LONG).show();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
