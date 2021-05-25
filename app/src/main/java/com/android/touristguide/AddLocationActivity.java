package com.android.touristguide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.annotations.NotNull;

import java.util.Arrays;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private final String TAG = "AddLocationActivityTAG";
    private Marker searchMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initPlaceAPI();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        DisabledConstrainLayout constrainLayout = findViewById(R.id.map_layout);
        LinearLayout layoutDisabled = (LinearLayout) findViewById(R.id.disable_layout);
        CircularProgressIndicator progressIndicator = layoutDisabled.getRootView().findViewById(R.id.progress_circular);
        constrainLayout.setDisabled(false);
        progressIndicator.setVisibility(View.GONE);
        layoutDisabled.setAlpha(0.0f);
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        Button btnChangeMapLayer = (Button) findViewById(R.id.btn_map_layer);
        btnChangeMapLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMapType();
            }
        });
        setupPlaceAPI(autocompleteFragment);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            location = new LatLng(bundle.getDouble("latitude"),bundle.getDouble("longitude"));
        }
    }

    private void setupPlaceAPI(final AutocompleteSupportFragment autocompleteFragment) {
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng latLng = place.getLatLng();
                try {
                    searchMarker.setPosition(latLng);
                    searchMarker.setVisible(true);
                } catch (NullPointerException e) {
                    searchMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng));
                }
                sendResult();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }


            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

        });
    }

    private void initPlaceAPI() {
        ApplicationInfo app = null;
        try {
            app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (app != null) {
            Bundle bundle = app.metaData;
            String apiKey = bundle.getString("com.google.android.geo.API_KEY");
            if (apiKey != null) {
                Places.initialize(this, apiKey);
            }
        }
    }

    private void changeMapType() {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(searchMarker != null){
                    searchMarker.setPosition(latLng);
                    searchMarker.setVisible(true);
                }else{
                    searchMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng));
                }
                sendResult();
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (location == null){
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.0f));
                                }
                            }
                        });
            }else{
                searchMarker = mMap.addMarker(new MarkerOptions()
                        .position(location));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10.0f));
            }
        }
    }

    private void sendResult(){
        if (searchMarker != null && searchMarker.isVisible()){
            Intent intent = new Intent();
            intent.putExtra("latitude",searchMarker.getPosition().latitude);
            intent.putExtra("longitude",searchMarker.getPosition().longitude);
            setResult(1,intent);
        }
    }
}
