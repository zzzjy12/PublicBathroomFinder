package com.example.placesprojectdemo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Washroom implements Serializable {
    private String ID;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private float rating;

    private int totalUserRatings = 0;
    private ArrayList<String> userComments;

    private Boolean openNow;
    private ArrayList<String> photoReferences;

    public Washroom() {
        this.photoReferences = new ArrayList<String>();
        this.userComments = new ArrayList<String>();
    }



    public Washroom(String name, String address, double latitude, double longitude, float rating, Boolean openNow, List<String> photoReferences) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.openNow = false; // Default value
        this.photoReferences = new ArrayList<String>(photoReferences);
        this.userComments = new ArrayList<String>();
    }

    public String getID() {
        return this.ID;
    }

    public void setID(String id) {
        this.ID = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Boolean isOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public ArrayList<String> getUserComment() {
        return userComments;
    }

    public void setUserComment(ArrayList<String> userComment) {
        this.userComments = userComment;
    }

    public List<String> getPhotoReferences() {
        return photoReferences;
    }

    public void setPhotoReferences(List<String> photoReferences) {
        this.photoReferences = new ArrayList<>(photoReferences);
    }

    // Add getters and setters for other fields

    @Override
    public String toString() {
        return "Washroom{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", rating=" + rating +
                ", is Open now? =" + isOpenNow() +
                // Add other fields as needed
                '}';
    }

    public void updateRating(float rating, int totalUserRatings) {
        this.totalUserRatings += totalUserRatings;
        this.totalUserRatings++;
        this.rating = (this.rating + rating) / 2;
    }
}
