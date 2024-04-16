package com.example.placesprojectdemo;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import getDirection.FetchURL;
import getDirection.TaskLoadedCallback;


public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback,TaskLoadedCallback {
    GoogleMap map;
    Button btngetDirection;
    MarkerOptions place1,place2;
    Polyline currentPolyline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        btngetDirection= findViewById(R.id.btnDirection);
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFrag);
        mapFragment.getMapAsync(this);
        Location currentLocation = LocationHolder.getCurrentLocation();
        LatLng currentLatLng= new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        LatLng destinationLatLng= LocationHolder.getDestinationLocation();
        place1 = new MarkerOptions().position(currentLatLng).title("Current Location");
        place2 = new MarkerOptions().position(destinationLatLng).title("Destination");
//            place1 = new MarkerOptions().position(new LatLng(currentLat, currentLng)).title("Location 1");
//            place2 = new MarkerOptions().position(new LatLng(markerLat, markerLng)).title("Location 2");

        btngetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url= getUrl(place1.getPosition(),place2.getPosition(),"walking");;
                new FetchURL (DirectionActivity.this).execute(url,"walking");
                // Zoom the camera to include both markers (origin and destination)
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(place1.getPosition());
                builder.include(place2.getPosition());
                LatLngBounds bounds = builder.build();
                int padding = 100; // Adjust the padding as needed
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);
            }
        });

    }
    

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.addMarker(place1);
        map.addMarker(place2);

    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = map.addPolyline((PolylineOptions) values[0]);
    }
}