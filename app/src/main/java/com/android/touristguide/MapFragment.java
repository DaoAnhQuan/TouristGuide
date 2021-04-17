package com.android.touristguide;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseFunctions mFunctions;
    private  DatabaseReference userGroupRef;
    private DisabledConstrainLayout mainLayout;
    private CircularProgressIndicator progressIndicator;
    private DatabaseReference membersRef;
    private LinearLayout layoutDisabled;
    private FirebaseDatabase db;
    private final String TAG = "MapActivityTAG";
    private Map<String, Marker> mapMarker;
    private boolean disable;
    private ExecutorService executor;
    private Map<String,Boolean> addMarkerProcessing;
    private MaterialToolbar toolbar;
    private Marker searchMarker;

    public MapFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFunctions = Helper.initFirebaseFunctions();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        userGroupRef = db.getReference("Users/"+currentUser.getUid()+"/group");
        mapMarker = new HashMap<>();
        disable = true;
        executor = Executors.newSingleThreadExecutor();
        addMarkerProcessing = new HashMap<>();
        ApplicationInfo app = null;
        try {
            app = getContext().getPackageManager().getApplicationInfo(getContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = app.metaData;
        Places.initialize(getContext(),bundle.getString("com.google.android.geo.API_KEY"));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map,container,false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        userGroupRef.addValueEventListener(groupEventListener);
        mainLayout = (DisabledConstrainLayout)view.findViewById(R.id.map_layout);
        layoutDisabled = (LinearLayout)view.findViewById(R.id.disable_layout);
        progressIndicator = layoutDisabled.getRootView().findViewById(R.id.progress_circular);
        mainLayout.setDisabled(true);
        toolbar = (MaterialToolbar) view.findViewById(R.id.top_app_bar_group);
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        final View clearButton = (View) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autocompleteFragment.setText("");
                searchMarker.remove();
            }

        });


        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng latLng = place.getLatLng();
                try {
                    searchMarker.setPosition(latLng);
                }catch (NullPointerException e){
                    searchMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng));
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }


            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }

        });
        return view;
    }

    ValueEventListener groupEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            getGroupType().addOnCompleteListener(new OnCompleteListener<Map<String,String>>() {
                @Override
                public void onComplete(@NonNull Task<Map<String,String>> task) {
                    if (task.isSuccessful()){
                        Map<String,String> result = task.getResult();
                        String type = result.get("type");
                        if (type.equals(Helper.INDIVIDUAL_GROUP)){
                            toolbar.inflateMenu(R.menu.non_group_menu);
                        }else {
                            toolbar.inflateMenu(R.menu.group_menu);
                        }
                        membersRef = db.getReference("Groups/"+result.get("groupID")+"/members");
                        membersRef.addValueEventListener(membersEventListener);
                    }
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG,"On Failed");
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private ValueEventListener membersEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Helper.getMembersLocation(mFunctions).addOnCompleteListener(new OnCompleteListener<List<Member>>() {
                @Override
                public void onComplete(@NonNull Task<List<Member>> task) {
                    LatLng location = new LatLng(0,0);
                    for (final Member member : task.getResult()){
                        final LatLng latLng = new LatLng(member.latitude,member.longitude);
                        if (mapMarker.containsKey(member.uid)){
                            Marker marker = mapMarker.get(member.uid);
                            marker.setPosition(latLng);
                        }else{
                            if (!addMarkerProcessing.containsKey(member.uid)){
                                addMarkerProcessing.put(member.uid,true);
                                Runnable addMarker = new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap markerBitmap = null;
                                        try {
                                            markerBitmap = Helper.getMapMarker(Uri.parse(member.url),getContext());
                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        final MarkerOptions markerOption = new MarkerOptions()
                                                .position(latLng)
                                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Marker marker = mMap.addMarker(markerOption);
                                                mapMarker.put(member.uid,marker);
                                            }
                                        });
                                    }
                                };
                                executor.execute(addMarker);
                            }
                        }
                        location = latLng;
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10.0f));
                    enableLayout();
                }
            });

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void enableLayout(){
        if (disable){
            mainLayout.setDisabled(false);
            progressIndicator.setVisibility(View.GONE);
            layoutDisabled.setAlpha(0.0f);
            disable = false;
        }
    }

    private Task<Map<String,String>> getGroupType(){
        return mFunctions.getHttpsCallable("getGroupType")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Map<String,String>>() {
                    @Override
                    public Map<String,String> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String, String> groupInfo = (Map<String, String>) task.getResult().getData();
                        return groupInfo;
                    }
                });
    }
}
