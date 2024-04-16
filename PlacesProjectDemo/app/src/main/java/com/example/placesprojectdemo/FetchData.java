package com.example.placesprojectdemo;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchData extends AsyncTask<Object, String, String> {
    private String googleNearByPlacesData;
    private GoogleMap googleMap;
    private Context context;

    private int totalUserRatings;

    // Constructor to receive the context
    public FetchData(Context context) {
        this.context = context;
    }

    public FetchData() {

    }

    @Override
    protected String doInBackground(Object... objects) {
        try {
            googleMap = (GoogleMap) objects[0];
            String url = (String) objects[1];
            DownloadUrl downLoadUrl = new DownloadUrl();
            googleNearByPlacesData = downLoadUrl.retireveUrl(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleNearByPlacesData;
    }



    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            // Iterate through the results and add markers
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject placeObject = jsonArray.getJSONObject(i);

                LatLng selectedPlaceLatLng = getLatLngFromPlace(placeObject);
                String name = placeObject.getString("name");
                Boolean isOpenNow = getOpenNow(placeObject);
                ArrayList<String> photoReferences = getPhotoReferences(placeObject);
                float rating = getRating(placeObject);
                String address = getAddressFromLatLng(context, selectedPlaceLatLng);

                totalUserRatings = placeObject.optInt("user_ratings_total", 0);

                if (!photoReferences.isEmpty()) {
                    Log.e("PhotoReferences", photoReferences.get(0));
                } else {
                    Log.e("PhotoReferences", "No photo references available");
                }
                Log.e("IsOpenNow", String.valueOf(isOpenNow));


//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.title(name);
//                markerOptions.position(selectedPlaceLatLng);
//                googleMap.addMarker(markerOptions);
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceLatLng, 15));
//
//
//                googleMap.setOnMarkerClickListener(marker -> {
//                    // Check if the washroom is in the database based on its name
//                    Log.e("MARKER", "Set on Click Listener");
//                    checkWashroomInDatabase(name, address, selectedPlaceLatLng, rating, photoReferences, isOpenNow);
//                    return true;
//                });

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name);
                markerOptions.position(selectedPlaceLatLng);
                Marker marker = googleMap.addMarker(markerOptions);

                // Associate marker with its data using tag
                marker.setTag(new Object[]{name, address, selectedPlaceLatLng, rating, photoReferences, isOpenNow});

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceLatLng, 15));

                // Use the data when setting the marker click listener
                googleMap.setOnMarkerClickListener(clickedMarker -> {

                    Object[] data = (Object[]) clickedMarker.getTag();
                    if (data != null && data.length == 6) {
                        String markerName = (String) data[0];
                        String markerAddress = (String) data[1];
                        LatLng markerLatLng = (LatLng) data[2];
                        float markerRating = (float) data[3];
                        ArrayList<String> markerPhotoReferences = (ArrayList<String>) data[4];
                        Boolean markerIsOpenNow = (Boolean) data[5];

                        LatLng markerDirLatLng = marker.getPosition();
                        LocationHolder.setDestinationLocation(markerDirLatLng);

                        checkWashroomInDatabase(markerName, markerAddress, markerLatLng, markerRating, markerPhotoReferences, markerIsOpenNow);
                    }
                    return true;
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private Boolean getOpenNow(JSONObject placeObject) {
        try {
            if (placeObject.has("opening_hours") && placeObject.getJSONObject("opening_hours").has("open_now")) {
                return placeObject.getJSONObject("opening_hours").getBoolean("open_now");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> getPhotoReferences(JSONObject placeObject) {
        ArrayList<String> photoReferences = new ArrayList<>();
        try {
            if (placeObject.has("photos")) {
                JSONArray photosArray = placeObject.getJSONArray("photos");
                for (int i = 0; i < photosArray.length(); i++) {
                    JSONObject photoObject = photosArray.getJSONObject(i);
                    if (photoObject.has("photo_reference")) {
                        String photoReference = photoObject.getString("photo_reference");
                        photoReferences.add(photoReference);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return photoReferences;
    }

    private String getAddressFromLatLng(Context context, LatLng latLng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.d("FetchData", "Selected Place Address: " + address.getAddressLine(0));
                return address.getAddressLine(0); // You can modify this to get more details if needed

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Address not found";
    }

    private LatLng getLatLngFromPlace(JSONObject placeObject) throws JSONException {
        JSONObject location = placeObject.getJSONObject("geometry").getJSONObject("location");
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        return new LatLng(lat, lng);
    }

    private float getRating(JSONObject placeObject) {
        if (placeObject.has("rating")) {
            try {
                return (float) placeObject.getDouble("rating");
            } catch (JSONException e) {
                Log.e("RatingError", e.getMessage());
                e.printStackTrace();
            }
        }
        return 0;
    }

    private void checkWashroomInDatabase(String selectedPlaceName, String address, LatLng selectedPlaceLatLng, float rating, List<String> photoReferences, Boolean isOpenNow) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("washrooms");
        databaseReference.orderByChild("name").equalTo(selectedPlaceName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get the first matching Washroom object (assuming unique names)
                            DataSnapshot firstChild = snapshot.getChildren().iterator().next();
                            Washroom washroom = firstChild.getValue(Washroom.class);

                            // Start the details activity and pass the washroom details
                            if (washroom != null) {
                                // Update the washroom object with the address, latitude, and longitude
                                washroom.setName(selectedPlaceName);
                                washroom.setAddress(address);
                                washroom.setLatitude(selectedPlaceLatLng.latitude);
                                washroom.setLongitude(selectedPlaceLatLng.longitude);
                                //washroom.setRating(rating);
                                washroom.setOpenNow(isOpenNow);
                                washroom.setPhotoReferences(photoReferences);

                                // Save the washroom data to Firebase
                                databaseReference.child(washroom.getID()).setValue(washroom);

                                Intent intent = new Intent(context, WashroomDetailsActivity.class);
                                intent.putExtra("washroom", washroom);
                                context.startActivity(intent);
                            }
                        } else {
                            // Washroom not found in the database
                            Toast.makeText(context, "Place details not found", Toast.LENGTH_SHORT).show();

                            // Create a new Washroom object with the selected details
                            Washroom newWashroom = new Washroom(selectedPlaceName, address, selectedPlaceLatLng.latitude, selectedPlaceLatLng.longitude, rating, isOpenNow, photoReferences);

                            // Add the new washroom to the database
                            addWashroomToFirebase(newWashroom);

                            // Start the details activity and pass the washroom details
                            Intent intent = new Intent(context, WashroomDetailsActivity.class);
                            intent.putExtra("washroom", newWashroom);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("FetchData", "Database error: " + error.getMessage());
                    }
                });
    }

    // Inside the method that adds a washroom to Firebase
    private void addWashroomToFirebase(Washroom washroom) {
        DatabaseReference washroomsRef = FirebaseDatabase.getInstance().getReference("washrooms");
        String washroomId = washroomsRef.push().getKey(); // Generate a unique key for the washroom
        washroom.setID(washroomId);
        washroomsRef.child(washroomId).setValue(washroom);
    }

    public void addRatingAndCommentToWashroom(String washroomId, float rating, String comment) {
        DatabaseReference washroomsRef = FirebaseDatabase.getInstance().getReference("washrooms");

        // Find the washroom by its ID and update the rating and comment
        washroomsRef.child(washroomId).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Washroom washroom = mutableData.getValue(Washroom.class);
                if (washroom != null) {
                    // Update the washroom's rating and comments
                    washroom.updateRating(rating, totalUserRatings);
                    Log.e("AGHA OOMAD", washroom.getID());
                    washroom.getUserComment().add(comment);
                    washroomsRef.child(washroom.getID()).setValue(washroom);
                    // Set the updated washroom back to the database
                    mutableData.setValue(washroom);

                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                if (databaseError != null) {
                    Log.e("FetchData", "Transaction failed: " + databaseError.getMessage());
                } else if (committed) {
                    Log.d("FetchData", "Transaction succeeded!");
                } else {
                    Log.d("FetchData", "Transaction skipped.");
                }
            }
        });
    }

    public void fetchCommentsForWashroom(String washroomId, CommentsCallback callback) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("washrooms").child(washroomId).child("userComment");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> comments = new ArrayList<>();
                Log.d("FetchData", "Washroom ID: " + washroomId);
                for (DataSnapshot commentSnapshot : dataSnapshot.getChildren()) {
                    Log.d("FetchData", "CommentSnapshot: " + commentSnapshot.toString());
                    String comment = commentSnapshot.getValue(String.class);
                    if (comment != null && !comment.trim().isEmpty()) {
                        // Exclude empty comments
                        comments.add(comment);
                    }
                }
                callback.onCommentsReceived(comments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FetchData", "Database error: " + databaseError.getMessage());
            }
        });
    }

    public interface CommentsCallback {
        void onCommentsReceived(List<String> comments);
    }


}