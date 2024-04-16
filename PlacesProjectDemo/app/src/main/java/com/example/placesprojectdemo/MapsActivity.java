package com.example.placesprojectdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.placesprojectdemo.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMapsBinding binding;
    private GoogleMap mMap;
    /*responsible for fetching the current location of the device*/
    private FusedLocationProviderClient mFusedLocationProviderClient;

    /*responsible for loading the suggestions as you see the user type*/
    private PlacesClient placesClient;

    /*as the suggestions are recieved from the google api we need an arraylist
     * to save those*/
    private List<AutocompletePrediction> predictionList;
    private Location mLastKnownLocation;

    //use for updating user request if the last known location is null
    private LocationCallback locationCallback;


    //for the layout and design
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    SupportMapFragment mapFragment;
    private RippleBackground ripple_bg;

    private final float DEFAULT_ZOOM = 18;
    // Constants
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private static final int LOCATION_FASTEST_UPDATE_INTERVAL = 5000; // 5 seconds
    private AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

    //Toolbar
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialize();
        setUpMap();
        //to manipulate the location button
        mapView = mapFragment.getView();
        initializeLocationProviderClient();
//        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //to manipulate the search
        initializeSearchBar();

        // Initialize the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back button to the Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setupSearchBarListener();
        setupSuggestionsClickListener();

        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the ripple animation to indicate searching
                ripple_bg.startRippleAnimation();

                // After a delay, perform the search for nearby places
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Stop the ripple animation
                        ripple_bg.stopRippleAnimation();

                        // Get the current location of the camera (center of the map)
                        LatLng currentMarkerLocation = mMap.getCameraPosition().target;

                        // Now, you can fetch the nearby places using the current location
                        // For example, if you're looking for nearby hospitals, you can construct the URL
                        String keyword = "Public%20Toilet"; // or "restaurant", "bank", etc.
                        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                        stringBuilder.append("location=").append(currentMarkerLocation.latitude).append(",").append(currentMarkerLocation.longitude);
                        stringBuilder.append("&radius=1000"); // Define the search radius
                        stringBuilder.append("&keyword=").append(keyword);
                        stringBuilder.append("&sensor=true");
                        stringBuilder.append("&key=").append(getResources().getString(R.string.google_maps_key));

                        String url = stringBuilder.toString();
                        Log.e("The URL", url);

                        // Use AsyncTask to perform the network request
                        Object dataFetch[] = new Object[2];
                        dataFetch[0] = mMap;
                        dataFetch[1] = url;

                        FetchData fetchData = new FetchData(MapsActivity.this);
                        fetchData.execute(dataFetch);
                    }
                }, 3000); // The delay time after which you want to start the search
            }
        });
    }

    private void initialize() {
        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btnFind);
        ripple_bg = findViewById(R.id.ripple_bg);

        // Initialize the Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back button to the Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle Toolbar navigation clicks here
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back when the back button is clicked
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpMap() {
        //id of the fragmanet in layout
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initializeLocationProviderClient() {
        //to initialize the fuse location
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //will enable places sdk
        Places.initialize(MapsActivity.this, "AIzaSyBBz3HT6FAtlv-BTVSX6nQ_KN0Bstk1bzs");
        placesClient = Places.createClient(this);
    }

    //this function will be called when the map is ready and loaded
    //so that we can perform the action like moving the map or enablning some buttons
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        /*this function is called when the map is ready
         * we take the map,load it to the mMap
         * we enable the location button
         * and we moved the location button to the required place*/
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //so that my location button is shown
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        setUpMapUI();
        checkLocationSettings();


        //to clear the searchbar after i select my location. resetting the searchbar
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible()) {
                    materialSearchBar.clearSuggestions();
                }
                if (materialSearchBar.isSearchEnabled()) {
                    materialSearchBar.disableSearch();
                }
                return false;
            }
        });
    }
    //need to the ce\heck the result; what did the user do
    //did he accept the location or not

    private void setUpMapUI() {
        // Check if mapView is not null
        if (mapView != null) {
            // Attempt to find the first descendant view with the given ID, indicating the "My Location" button
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // Check if the locationButton is not null
            if (locationButton != null) {
                // Get the layout parameters of the location button's parent (assumed to be a FrameLayout or RelativeLayout)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                // Modify the layout parameters as needed (e.g., moving the button to the bottom of the screen)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 40, 180);
                // Apply the modified layout parameters back to the location button's parent
                locationButton.setLayoutParams(layoutParams);
            }
        }
    }

    private void checkLocationSettings() {
        //check if the GPS is enabled or not and then request the user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        //this function will check for the location setting
        //the location settings will be sufficien the location will be on and you can perform the a task
        //or the locationsettings will not be on

        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, this::onLocationSettingsSuccess);
        task.addOnFailureListener(this, this::onLocationSettingsFailure);
    }

    private void onLocationSettingsSuccess(LocationSettingsResponse response) {
        // GPS is enabled, get the device location
        getDeviceLocation();
    }

    private void onLocationSettingsFailure(Exception e) {
        if (e instanceof ResolvableApiException) {
            try {
                // Show the user a dialog to enable location settings
                ResolvableApiException resolvable = (ResolvableApiException) e;
                resolvable.startResolutionForResult(MapsActivity.this, 51);
            } catch (IntentSender.SendIntentException ex) {
                ex.printStackTrace();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 51) {
            if (resultCode == RESULT_OK) {
                //if they enable the gps option on their phone
                //we can now proceed to find the user current location
                //and then to move the map to that location
                getDeviceLocation();

            }
        }
    }

    /*in this function we asked the mFusedLocationProviderClient to
     * give us the last location
     * when the request is complete
     * we check if the task was succesful or not
     *if it was, it doess not still grantee that we could get the location
     * so we get the location and check if it s null or not
     * if it not null
     * we move the camera to the location that we recieve
     * if the last known is null
     * we have to create two things location request and location callback
     * location callback is a fuction that will be executed
     * when an updated location is recived
     * */
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        // Attempt to fetch the user's last known location.
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Successfully retrieved the location.
                    mLastKnownLocation = task.getResult();
                    LocationHolder.setCurrentLocation(mLastKnownLocation);
                    if (mLastKnownLocation != null) {
                        // Store the current location
                        currentLat = mLastKnownLocation.getLatitude();
                        currentLng = mLastKnownLocation.getLongitude();
                        // If the last known location is not null, move the camera to that location.
                        moveCameraToLocation(mLastKnownLocation);

                    } else {
                        // If the last known location is null, request updated location.
                        requestLocationUpdates();
                    }
                } else {
                    // The task failed to retrieve the last location.
                    Log.e("MapsActivity", "Unable to get the last location.");
                    Toast.makeText(MapsActivity.this, "Unable to get the last location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void moveCameraToLocation(Location location) {
        LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, DEFAULT_ZOOM));
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest locationRequest = createLocationRequest();
        locationCallback = createLocationCallback();
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private LocationCallback createLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Update the last known location with the latest location and move the camera.
                mLastKnownLocation = locationResult.getLastLocation();
                moveCameraToLocation(mLastKnownLocation);
                // Stop further updates to conserve battery.
                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
        };
    }

    private void initializeSearchBar() {
        // Configure the search bar's listeners and behavior.
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                // Handle search state change if needed.
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                // Handle search confirmation, perform search operations.
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                // Handle button clicks.
                switch (buttonCode) {
                    case MaterialSearchBar.BUTTON_NAVIGATION:
                        // Opening or closing a navigation drawer.
                        break;
                    case MaterialSearchBar.BUTTON_BACK:
                        materialSearchBar.disableSearch();
                        break;
                }
            }
        });


    }
    private void setupSearchBarListener() {
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // You can leave this empty if you're not using it
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("ca").setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token).setQuery(s.toString()).build();
                placesClient.findAutocompletePredictions(predictionsRequest)
                        .addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                if (task.isSuccessful()) {
                                    FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                                    if (predictionsResponse != null) {
                                        predictionList = predictionsResponse.getAutocompletePredictions();
                                        List<String> suggestionList = new ArrayList<>();
                                        for (AutocompletePrediction prediction : predictionList) {
                                            suggestionList.add(prediction.getFullText(null).toString());
                                        }
                                        materialSearchBar.updateLastSuggestions(suggestionList);
                                        if (!materialSearchBar.isSuggestionsVisible()) {
                                            materialSearchBar.showSuggestionsList();
                                        }
                                    }
                                } else {
                                    Log.i("mytag", "prediction fetching task unsuccessful");
                                }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {
                // You can leave this empty if you're not using it
            }
        });
    }
    private void setupSuggestionsClickListener() {
        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            /*we need the lat and long of that location but we dont have the lat and lng
             * of that location. we have the place id of that place
             * we take that place id, send it to google
             *  places api and request to send us the lat and lng
             * and then we move the camera to that place*/
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                //we need to fetch the id of the place that is selected by the user
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(() -> materialSearchBar.clearSuggestions(), 1000);

                //to close the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                String placeId = selectedPrediction.getPlaceId();
                //we pass this place id to give us the lat and lng
                //which fields are we intesrted, we are only intested in lgn and lat
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(fetchPlaceResponse -> {
                    Place place = fetchPlaceResponse.getPlace();
                    Log.i("mytag", "place found: " + place.getName());
                    LatLng latLngOfPlace = place.getLatLng();
                    if (latLngOfPlace != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                    }
                }).addOnFailureListener(e -> {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        apiException.printStackTrace();
                        int statusCode = apiException.getStatusCode();
                        Log.i("mytag", "place not found: " + e.getMessage());
                        Log.i("mytag", "status code " + statusCode);
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
                // Handle the deletion event if necessary.
            }
        });
    }
    private void findNearbyPlaces(String placeType) {
        if (mLastKnownLocation == null) {
            Toast.makeText(this, "Current location not known. Try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiKey = getString(R.string.google_maps_key); // Your API key.
        String baseUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
        String location = mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
        int radius = 1000; // Search within a 1-kilometer radius.

        // Build the search URL.
        String url = baseUrl + "?location=" + location + "&radius=" + radius + "&type=" + placeType + "&sensor=true&key=" + apiKey;

        // Execute the AsyncTask to fetch nearby places.
        new FetchData().execute(mMap, url);
    }

}