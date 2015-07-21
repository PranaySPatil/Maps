package com.example.pranay.maps;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Pranay on 6/25/2015.
 */
public class Position {
    String latitude,longitude;
    int pos;
   // Marker m;
   // Circle c;
    float radius;

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {

        return radius;
    }

    @Override
    public int hashCode() {
        System.out.println("hashCode called");
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((latitude == null) ? 0 : longitude.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        System.out.println("Equals called "+this.latitude+" != "+((Position)o).latitude);
       return  (o != null && o instanceof Position && this.latitude.equals(((Position)o).latitude) && this.longitude.equals(((Position)o).longitude));
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {

        return pos;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {

        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
