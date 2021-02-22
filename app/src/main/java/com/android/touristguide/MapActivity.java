package com.android.touristguide;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FloatingSearchView svLocation;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView imvAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private View navigationHeaderView;
    private final String TAG = "MapActivityTAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        svLocation = (FloatingSearchView) findViewById(R.id.sv_location);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationHeaderView = navigationView.getHeaderView(0);
        imvAvatar = (ImageView) navigationHeaderView.findViewById(R.id.imv_avatar);
        tvUsername = (TextView) navigationHeaderView.findViewById(R.id.tv_username);
        tvEmail = (TextView) navigationHeaderView.findViewById(R.id.tv_email);
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        svLocation.attachNavigationDrawerToMenuButton(drawerLayout);
        
        NavigationItemSelectedListener navigationItemSelectedListener = new NavigationItemSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        addHeaderValueEventListener();
    }

    private void addHeaderValueEventListener(){
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvEmail.setText(user.email);
                tvUsername.setText(user.username);
                if (user.avatar != null){
                    Glide.with(navigationHeaderView).load(user.avatar).into(imvAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid()).addValueEventListener(userListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
