package com.example.placesprojectdemo;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WashroomDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {


    TextView textViewName;
    TextView textViewAddress;
    TextView textViewLatitude;
    TextView textViewLongitude;
    TextView textViewRating;
    TextView textViewOpenNow;
    LinearLayout photoLayout;
    RatingBar ratingBar;
    EditText editTextComment;
    Washroom washroom;

    Button buttonGetDirections;

    private ListView listViewComments;
    private ArrayAdapter<String> commentsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washroom_details);
        initialize();

    }



    private void initialize() {
        // Get the Washroom object from Intent
        washroom = (Washroom) getIntent().getSerializableExtra("washroom");

        // Find TextViews and ImageView in the layout
        textViewName = findViewById(R.id.textViewName);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewLatitude = findViewById(R.id.textViewLatitude);
        textViewLongitude = findViewById(R.id.textViewLongitude);
        textViewRating = findViewById(R.id.textViewRating);
        textViewOpenNow = findViewById(R.id.textViewOpenNow);
        photoLayout = findViewById(R.id.photoLayout);
        ratingBar = findViewById(R.id.ratingBar);
        editTextComment = findViewById(R.id.editTextComment);

        buttonGetDirections= findViewById(R.id.buttonGetDirections);
        buttonGetDirections.setOnClickListener(this);

        listViewComments = findViewById(R.id.listViewComments);
        commentsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewComments.setAdapter(commentsAdapter);

        displayInfo();
    }

    private void displayInfo() {

        buttonGetDirections.setOnClickListener(v -> {
            LatLng destinationLocation = LocationHolder.getDestinationLocation();
            Location currentLocation = LocationHolder.getCurrentLocation();

            if (currentLocation != null && destinationLocation != null) {
                Intent intent = new Intent(WashroomDetailsActivity.this, DirectionActivity.class);
                intent.putExtra("current_lat", currentLocation.getLatitude());
                intent.putExtra("current_lng", currentLocation.getLongitude());
                intent.putExtra("destination_lat", destinationLocation.latitude);
                intent.putExtra("destination_lng", destinationLocation.longitude);
                startActivity(intent);
            } else {
                Toast.makeText(WashroomDetailsActivity.this, "Washroom location not available", Toast.LENGTH_SHORT).show();
            }


        });

        // Set values to TextViews
        if (washroom != null) {
            // Set the name
            String name = washroom.getName();
            textViewName.setText(name);

            // Set the address
            String address = washroom.getAddress();
            if (address != null && !address.isEmpty()) {
                textViewAddress.setText(address);
            } else {
                textViewAddress.setVisibility(View.GONE);
            }

            // Set the latitude
            double latitude = washroom.getLatitude();
            textViewLatitude.setText(String.valueOf(latitude));

            // Set the longitude
            double longitude = washroom.getLongitude();
            textViewLongitude.setText(String.valueOf(longitude));

            // Set the rating
            float rating = washroom.getRating();
            textViewRating.setText(String.valueOf(rating));

            // Set open now status
            Boolean isOpenNow = washroom.isOpenNow();
            if (isOpenNow != null) {
                textViewOpenNow.setText(isOpenNow ? "Open Now" : "Closed");
            } else {
                textViewOpenNow.setVisibility(View.GONE);
            }

            // Display photos if available
            List<String> photoReferences = washroom.getPhotoReferences();
            if (photoReferences != null) {
                displayPhotos(photoLayout, photoReferences);
            }

            displayComments();
        } else {
            Toast.makeText(this, "Washroom details not available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayComments() {
        FetchData fetchData = new FetchData();
        fetchData.fetchCommentsForWashroom(washroom.getID(), new FetchData.CommentsCallback() {
            @Override
            public void onCommentsReceived(List<String> comments) {

                Log.d("FetchData", "Comments received: " + comments.size());
                for (String comment : comments) {
                    Log.d("FetchData", "Comment: " + comment);
                }
                // Update the comments in the adapter
                commentsAdapter.clear();
                commentsAdapter.addAll(comments);
                // Notify the adapter that the data has changed
                commentsAdapter.notifyDataSetChanged();

                setListViewHeightBasedOnChildren(listViewComments);
            }
        });
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void displayPhotos(LinearLayout photoLayout, List<String> photoReferences) {
        for (String photoReference : photoReferences) {
            ImageView imageView = new ImageView(this);
            // Use Picasso to load images from URL
            Picasso.get().load(getPhotoUrl(photoReference)).into(imageView);
            // Set layout parameters as needed
            photoLayout.addView(imageView);
        }
    }

    // Method to construct the URL for the photo
    private String getPhotoUrl(String photoReference) {
        int maxWidth = 800; // Set your desired max width
        return "https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=" + maxWidth +
                "&photoreference=" + photoReference +
                "&key=" + getResources().getString(R.string.google_maps_key);
    }
    public void onSaveButtonClick(View view) {
        // Get the rating and comment from the UI elements
        float userRating = ratingBar.getRating();
        String userComment = editTextComment.getText().toString();

        if (washroom != null) {
            FetchData fetchData = new FetchData();
            fetchData.addRatingAndCommentToWashroom(washroom.getID(), userRating, userComment);
            Toast.makeText(this, "MAmad: " + userRating + ", Comment: " + userComment, Toast.LENGTH_SHORT).show();
        }


        // Provide feedback to the user
        Toast.makeText(this, "Rating: " + userRating + ", Comment: " + userComment, Toast.LENGTH_SHORT).show();
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    @Override
    public void onClick(View v) {

            LatLng destinationLocation = LocationHolder.getDestinationLocation();
            Location currentLocation = LocationHolder.getCurrentLocation();

            if (currentLocation != null && destinationLocation != null) {
                Intent intent = new Intent(WashroomDetailsActivity.this, DirectionActivity.class);
                intent.putExtra("current_lat", currentLocation.getLatitude());
                intent.putExtra("current_lng", currentLocation.getLongitude());
                intent.putExtra("destination_lat", destinationLocation.latitude);
                intent.putExtra("destination_lng", destinationLocation.longitude);
                startActivity(intent);
            } else {
                Toast.makeText(WashroomDetailsActivity.this, "Washroom location not available", Toast.LENGTH_SHORT).show();
            }

    }
}
