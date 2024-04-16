package com.example.placesprojectdemo;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class LocationHolder {
    private static Location currentLocation = null;
    private static LatLng DestinationLocation = null;

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static void setCurrentLocation(Location location) {
        currentLocation = location;
    }
    public static LatLng getDestinationLocation() {
        return DestinationLocation;
    }

    public static void setDestinationLocation(LatLng  location) {
        DestinationLocation = location;
    }
}