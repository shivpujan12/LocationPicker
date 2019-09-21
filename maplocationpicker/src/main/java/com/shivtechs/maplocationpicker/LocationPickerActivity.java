//  The MIT License (MIT)

//  Copyright (c) 2018 Intuz Solutions Pvt Ltd.

//  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
//  (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
//  merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:

//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
//  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.shivtechs.maplocationpicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private final String TAG = LocationPickerActivity.class.getSimpleName();
    private String userAddress = "";
    private String userState = "";
    private String userCity = "";
    private String userPostalCode = "";
    private String userCountry = "";
    private String userAddressline2 = "";
    private String userAddressline1 = "";
    private Bundle addressBundle;
    private List addressdetails;
    private double mLatitude;
    private double mLongitude;
    private String place_id = "";
    private String place_url = " ";
    private GoogleMap mMap;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 2;
    private boolean mLocationPermissionGranted;
    private TextView imgSearch;
    private TextView citydetail;
    private EditText addressline1;
    private EditText addressline2;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    //inital zoom
    private float previousZoomLevel = -1.0f;
    private boolean isZooming = false;

    //Declaration of FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationProviderClient;
    private List<AsyncTask> filterTaskList = new ArrayList<>();
    String regex = "^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$";
    Pattern latLongPattern = Pattern.compile(regex);
    private int doAfterPermissionProvided, doAfterLocationSwitchedOn = 1;
    private double currentLatitude;
    private double currentLongitude;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_location_picker);
        if(getSupportActionBar()!=null)
            getSupportActionBar().hide();
        ImageView imgCurrentloc = findViewById(R.id.imgCurrentloc);
        Button txtSelectLocation = findViewById(R.id.fab_select_location);
        ImageView directionTool = findViewById(R.id.direction_tool);
        ImageView googleMapTool = findViewById(R.id.google_maps_tool);

        imgSearch = findViewById(R.id.imgSearch);
        addressline2 = findViewById(R.id.addressline2);
        citydetail = findViewById(R.id.citydetails);


        // Initialize bundle
        addressBundle = new Bundle();

        //intitalization of FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Prepare for Request for current location
        getLocationRequest();

        //define callback of location request
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                Log.d(TAG, "onLocationAvailability: isLocationAvailable =  " + locationAvailability.isLocationAvailable());
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onLocationResult: " + locationResult);
                if (locationResult == null) {
                    return;
                }

                //show location on map
                switch (doAfterLocationSwitchedOn) {
                    case 1:
                        startParsingAddressToShow();
                        break;
                    case 2:
                        //on click of imgCurrent
                        showCurrentLocationOnMap(false);
                        break;
                    case 3:
                        //on Click of Direction Tool
                        showCurrentLocationOnMap(true);
                        break;
                }

                //Location fetched, update listener can be removed
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            }

        };

        // Try to obtain the map from the SupportMapFragment.
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //if you want to open the location on the LocationPickerActivity through intent
        Intent i = getIntent();
        if (i != null) {
            Bundle extras = i.getExtras();
            if (extras != null) {
                userAddress = extras.getString(MapUtility.ADDRESS);
                //temp -> get lat , log from db
                mLatitude = getIntent().getDoubleExtra(MapUtility.LATITUDE, 0);
                mLongitude = getIntent().getDoubleExtra(MapUtility.LONGITUDE, 0);
            }
        }

        if (savedInstanceState != null) {
            mLatitude = savedInstanceState.getDouble("latitude");
            mLongitude = savedInstanceState.getDouble("longitude");
            userAddress = savedInstanceState.getString("userAddress");
            currentLatitude = savedInstanceState.getDouble("currentLatitude");
            currentLongitude = savedInstanceState.getDouble("currentLongitude");
        }

        if (!MapUtility.isNetworkAvailable(this)) {
            MapUtility.showToast(this, "Please Connect to Internet");
        }


        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Places.isInitialized()) {
                    Places.initialize(LocationPickerActivity.this.getApplicationContext(),MapUtility.apiKey);
                }

                // Set the fields to specify which types of place data to return.
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);


                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(LocationPickerActivity.this);
                LocationPickerActivity.this.startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        txtSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // add data into intent and send back to Parent Activity
                intent.putExtra(MapUtility.ADDRESS, imgSearch.getText().toString().trim());
                intent.putExtra(MapUtility.LATITUDE, mLatitude);
                intent.putExtra(MapUtility.LONGITUDE, mLongitude);
                intent.putExtra("fullAddress",addressBundle);
                intent.putExtra("id", place_id);//if you want place id
                intent.putExtra("url", place_url);//if you want place url
                LocationPickerActivity.this.setResult(Activity.RESULT_OK, intent);
                LocationPickerActivity.this.finish();
            }
        });

        imgCurrentloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationPickerActivity.this.showCurrentLocationOnMap(false);
                doAfterPermissionProvided = 2;
                doAfterLocationSwitchedOn = 2;
            }
        });

        // google maps tools
        directionTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationPickerActivity.this.showCurrentLocationOnMap(true);
                doAfterPermissionProvided = 3;
                doAfterLocationSwitchedOn = 3;
            }
        });

        googleMapTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Default google map
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://maps.google.com/maps?q=loc:" + mLatitude + ", " + mLongitude + ""));
                LocationPickerActivity.this.startActivity(intent);
            }
        });

        try {
            Toast.makeText(getApplicationContext(),this.getResources().getString(R.string.edittext_hint),Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this,this.getResources().getString(R.string.edittext_hint),Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //after a place is searched
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                userAddress = place.getAddress();
              //  addressdetails=place.getAddressComponents();
                imgSearch.setText("" + userAddress);
                mLatitude = place.getLatLng().latitude;
                mLongitude = place.getLatLng().longitude;
                place_id = place.getId();
                place_url = String.valueOf(place.getWebsiteUri());

                addMarker();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            //after location switch on dialog shown
            if (resultCode != RESULT_OK) {
                //Location not switched ON
                Toast.makeText(LocationPickerActivity.this, "Location Not Available..", Toast.LENGTH_SHORT).show();

            } else {
                // Start location request listener.
                //Location will be received onLocationResult()
                //Once loc recvd, updateListener will be turned OFF.
                Toast.makeText(this, "Fetching Location...", Toast.LENGTH_LONG).show();
                startLocationUpdates();

            }
        }
    }

    private boolean checkAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarsePermision = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (coarsePermision != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }

        //getSettingsLocation();
        return true;

    }

    private void showCurrentLocationOnMap(final boolean isDirectionClicked) {

        if (checkAndRequestPermissions()) {

            @SuppressLint("MissingPermission")
            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mMap.clear();
                        if (isDirectionClicked) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            //Go to Map for Directions
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                                    "http://maps.google.com/maps?saddr=" + currentLatitude + ", " + currentLongitude + "&daddr=" + mLatitude + ", " + mLongitude + ""));
                            LocationPickerActivity.this.startActivity(intent);
                        } else {
                            //Go to Current Location
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                            LocationPickerActivity.this.getAddressByGeoCodingLatLng();
                        }

                    } else {
                        //Gps not enabled if loc is null
                        LocationPickerActivity.this.getSettingsLocation();
                        Toast.makeText(LocationPickerActivity.this, "Location not Available", Toast.LENGTH_SHORT).show();

                    }
                }
            });
            lastLocation.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //If perm provided then gps not enabled
//                getSettingsLocation();
                    Toast.makeText(LocationPickerActivity.this, "Location Not Availabe", Toast.LENGTH_SHORT).show();

                }
            });
        }

    }
    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
    private void addMarker() {
        CameraUpdate cameraUpdate;
        String SPACE = " , ";
        LatLng coordinate = new LatLng(mLatitude, mLongitude);
        if (mMap != null) {
            MarkerOptions markerOptions;
            try {
                mMap.clear();
                imgSearch.setText("" + userAddress);
                markerOptions = new MarkerOptions().position(coordinate).title(userAddress).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("ic_pointer",100,100)));
                if(isZooming) {
                   //  camera will not Update
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, mMap.getCameraPosition().zoom);
                }else {
                    // camera will Update zoom
                    cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 18);


                }

                mMap.animateCamera(cameraUpdate);
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                Marker marker = mMap.addMarker(markerOptions);
                //marker.showInfoWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try{
            userAddressline2 = userAddressline2.substring(0, userAddressline2.indexOf(userCity));
           // userAddressline.replace(userCity,"");
          //  userAddressline.replace(userPostalCode,"");
         //   userAddressline.replace(userState,"");
          //  userAddressline.replace(userCountry,"");
        }catch (Exception ex){ Log.d(TAG,"address error "+ex);}

        try {
            addressline2.setText(userAddressline2);
            citydetail.setText(userCity+SPACE+userPostalCode+SPACE+userState+SPACE+userCountry);

        }catch (Exception ex){ ex.printStackTrace();}

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (mMap.isIndoorEnabled()) {
            mMap.setIndoorEnabled(false);
        }


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                // Getting the position from the marker
                LatLng latLng = arg0.getPosition();
                mLatitude = latLng.latitude;
                mLongitude = latLng.longitude;
                TextView tvLat = v.findViewById(R.id.address);
                tvLat.setText(userAddress);
                return v;

            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mLatitude = latLng.latitude;
                mLongitude = latLng.longitude;
                Log.e("latlng", latLng + "");
                isZooming=true;
                LocationPickerActivity.this.addMarker();
                if (!MapUtility.isNetworkAvailable(LocationPickerActivity.this)) {
                    MapUtility.showToast(LocationPickerActivity.this, "Please Connect to Internet");
                }
                LocationPickerActivity.this.getAddressByGeoCodingLatLng();

            }
        });

        if (checkAndRequestPermissions()) {
            startParsingAddressToShow();
        } else {
            doAfterPermissionProvided = 1;
        }

    }

    private void getSettingsLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    //...
                    if (response != null) {
                        LocationSettingsStates locationSettingsStates = response.getLocationSettingsStates();
                        Log.d(TAG, "getSettingsLocation: " + locationSettingsStates);
                        LocationPickerActivity.this.startLocationUpdates();

                    }
                } catch (ApiException exception) {
                    Log.d(TAG, "getSettingsLocation: " + exception);
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        LocationPickerActivity.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            //...
                            break;
                    }
                }
            }
        });
    }

    /**
     * Show location from intent
     */
    private void startParsingAddressToShow() {
        //get address from intent to show on map
        if (userAddress == null || userAddress.isEmpty()) {

            //if intent does not have address,
            //cell is blank
            showCurrentLocationOnMap(false);

        } else

            //check if address contains lat long, then extract
            //format will be lat,lng i.e 19.23234,72.65465
            if (latLongPattern.matcher(userAddress).matches()) {

                Pattern p = Pattern.compile("(-?\\d+(\\.\\d+)?)");   // the pattern to search for
                Matcher m = p.matcher(userAddress);

                // if we find a match, get the group
                int i = 0;
                while (m.find()) {
                    // we're only looking for 2s group, so get it
                    if (i == 0)
                        mLatitude = Double.parseDouble(m.group());
                    if (i == 1)
                        mLongitude = Double.parseDouble(m.group());

                    i++;

                }
                //show on map
                getAddressByGeoCodingLatLng();
                addMarker();

            } else {
                //get  latlong from String address via reverse geo coding
                //Since lat long not present in db
                if (mLatitude == 0 && mLongitude == 0) {
                    getLatLngByRevGeoCodeFromAdd();
                } else {
                    // Latlong is more accurate to get exact point on map ,
                    // String address might not be sufficient (i.e Mumbai, Mah..etc)
                    addMarker();
                }
            }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("latitude", mLatitude);
        outState.putDouble("longitude", mLongitude);
        outState.putString("userAddress", userAddress);
        outState.putBundle("addressBundle",addressBundle);
        outState.putDouble("currentLatitude", currentLatitude);
        outState.putDouble("currentLongitude", currentLongitude);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void getAddressByGeoCodingLatLng() {

        //Get string address by geo coding from lat long
        if (mLatitude != 0 && mLongitude != 0) {

            if (MapUtility.popupWindow != null && MapUtility.popupWindow.isShowing()) {
                MapUtility.hideProgress();
            }

            Log.d(TAG, "getAddressByGeoCodingLatLng: START");
            //Cancel previous tasks and launch this one
            for (AsyncTask prevTask : filterTaskList) {
                prevTask.cancel(true);
            }

            filterTaskList.clear();
            GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
            filterTaskList.add(asyncTask);
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mLatitude, mLongitude);
        }
    }

    private void getLatLngByRevGeoCodeFromAdd() {

        //Get string address by geo coding from lat long
        if (mLatitude == 0 && mLongitude == 0) {

            if (MapUtility.popupWindow != null && MapUtility.popupWindow.isShowing()) {
                MapUtility.hideProgress();
            }

            Log.d(TAG, "getLatLngByRevGeoCodeFromAdd: START");
            //Cancel previous tasks and launch this one
            for (AsyncTask prevTask : filterTaskList) {
                prevTask.cancel(true);
            }

            filterTaskList.clear();
            GetLatLngFromAddress asyncTask = new GetLatLngFromAddress();
            filterTaskList.add(asyncTask);
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userAddress);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        Double latitude, longitude;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MapUtility.showProgress(LocationPickerActivity.this);
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {

                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());
                StringBuilder sb = new StringBuilder();

                //get location from lat long if address string is null
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && addresses.size() > 0) {

                    String address = addresses.get(0).getAddressLine(0);
                    if (address != null)
                        addressBundle.putString("addressline2",address);
                        sb.append(address).append(" ");


                    String city = addresses.get(0).getLocality();
                    if (city != null)
                        addressBundle.putString("city",city);
                        sb.append(city).append(" ");


                    String state = addresses.get(0).getAdminArea();
                    if (state != null)
                        addressBundle.putString("state",state);
                        sb.append(state).append(" ");


                    String country = addresses.get(0).getCountryName();
                    if (country != null)
                        addressBundle.putString("country",country);
                        sb.append(country).append(" ");

                    String postalCode = addresses.get(0).getPostalCode();
                    if (postalCode != null)
                        addressBundle.putString("postalcode",postalCode);
                        sb.append(postalCode).append(" ");
                   // return sb.toString();

                    addressBundle.putString("fulladdress",sb.toString());

                    return addressBundle;
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                addressBundle.putBoolean("error",true);
                return addressBundle;
                //return roundAvoid(latitude) + "," + roundAvoid(longitude);

            }

           // return bu;
        }


        @Override
        // setting address into different components
        protected void onPostExecute(Bundle userAddress) {
            super.onPostExecute(userAddress);

            LocationPickerActivity.this.userAddress = userAddress.getString("fulladdress");
            LocationPickerActivity.this.userCity = userAddress.getString("city");
            LocationPickerActivity.this.userState = userAddress.getString("state");
            LocationPickerActivity.this.userPostalCode = userAddress.getString("postalcode");
            LocationPickerActivity.this.userCountry = userAddress.getString("country");
            LocationPickerActivity.this.userAddressline2 = userAddress.getString("addressline2");
            MapUtility.hideProgress();
            addMarker();
        }
    }

    private class GetLatLngFromAddress extends AsyncTask<String, Void, LatLng> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MapUtility.showProgress(LocationPickerActivity.this);
        }

        @Override
        protected LatLng doInBackground(String... userAddress) {
            LatLng latLng = new LatLng(0, 0);

            try {

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(LocationPickerActivity.this, Locale.getDefault());

                //get location from lat long if address string is null
                addresses = geocoder.getFromLocationName(userAddress[0], 1);

                if (addresses != null && addresses.size() > 0) {
                    latLng = new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
                }
            } catch (Exception ignored) {
            }
            return latLng;
        }


        @Override
        protected void onPostExecute(LatLng latLng) {
            super.onPostExecute(latLng);
            LocationPickerActivity.this.mLatitude = latLng.latitude;
            LocationPickerActivity.this.mLongitude = latLng.longitude;
            MapUtility.hideProgress();
            addMarker();
        }
    }


    double roundAvoid(double value) {
        double scale = Math.pow(10, 6);
        return Math.round(value * scale) / scale;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (AsyncTask task : filterTaskList) {
            task.cancel(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Do tasks for which permission was granted by user in onRequestPermission()
        if (!isFinishing() && mLocationPermissionGranted) {
            // perform action required b4 asking permission
            mLocationPermissionGranted = false;
            switch (doAfterPermissionProvided) {
                case 1:
                    startParsingAddressToShow();
                    break;
                case 2:
                    showCurrentLocationOnMap(false);
                    break;
                case 3:
                    showCurrentLocationOnMap(true);
                    break;
            }

        }

    }

    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(LocationPickerActivity.this, "Location not Available", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "startLocationUpdates: onSuccess: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            Log.d(TAG, "startLocationUpdates: " + ((ApiException) e).getMessage());
                        } else {
                            Log.d(TAG, "startLocationUpdates: " + e.getMessage());
                        }
                    }
                });

    }

    private void getLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

}

