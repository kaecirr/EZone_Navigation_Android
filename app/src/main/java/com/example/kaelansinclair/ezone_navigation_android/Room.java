package com.example.kaelansinclair.ezone_navigation_android;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Class representing rooms and holding room metadata.
 */

public class Room {
    private String name;
    private int floor;
    private String description;
    private LatLng latLng;

    /**
     * Room constructor.
     * @param name the name of the room.
     * @param floor the floor the room is on.
     * @param description the description of the room.
     * @param latLng the position of the room.
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        if (getFloor() != room.getFloor()) return false;
        if (!getName().equals(room.getName())) return false;
        if (!getDescription().equals(room.getDescription())) return false;
        return getLatLng().equals(room.getLatLng());

    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + getFloor();
        result = 31 * result + getDescription().hashCode();
        result = 31 * result + getLatLng().hashCode();
        return result;
    }


}
