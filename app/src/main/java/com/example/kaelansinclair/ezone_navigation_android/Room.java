package com.example.kaelansinclair.ezone_navigation_android;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Kaelan Sinclair on 2/10/2017.
 */

public class Room {
    private String name;
    private int floor;
    private String description;
    private LatLng latLng;

    public Room (String name, int floor, String description, LatLng latLng) {
        this.name = name;
        this.floor = floor;
        this.description = description;
        this.latLng = latLng;
    }

    public String getName() {return name;}

    public int getFloor() {return floor;}

    public String getDescription() {return description;}

    public LatLng getLatLng() {return latLng;}

    @Override
    public String toString() {
        return name;
    }

}
