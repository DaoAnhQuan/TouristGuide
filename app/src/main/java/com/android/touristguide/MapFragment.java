package com.android.touristguide;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

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
    private String groupType;
    private String groupID;
    private boolean firstTime;

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
        firstTime = true;
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
                searchMarker.setVisible(false);
            }

        });
        Button btnChangeMapLayer = (Button) view.findViewById(R.id.btn_map_layer);
        btnChangeMapLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeMapType();
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
                    searchMarker.setVisible(true);
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
        toolbar.setOnMenuItemClickListener(groupMenuItemClickListener);
        return view;
    }

    androidx.appcompat.widget.Toolbar.OnMenuItemClickListener groupMenuItemClickListener = new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.menu_create_group:
                    Intent startNewGroupActivity = new Intent(getContext(),NewGroupActivity.class);
                    startActivity(startNewGroupActivity);
                    return true;
                case R.id.menu_join_group:
                    showJoinGroupDialog();
                    return true;
                case R.id.menu_leave:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(R.string.leave_group_confirm)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    toolbar.getMenu().clear();
                                    mFunctions.getHttpsCallable("leaveGroup")
                                            .call();
                                    dialogInterface.cancel();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                case R.id.menu_turn_off_location_sharing:
                    NewGroupActivity.updateLocationSetting(mFunctions,"no");
                    setMapMenu(groupType,"off");
                    return true;
                case R.id.menu_turn_on_location_sharing:
                    NewGroupActivity.updateLocationSetting(mFunctions,"yes");
                    setMapMenu(groupType,"on");
                    return true;
                case R.id.menu_change_leader:
                    return true;
                case R.id.menu_get_group_code:
                    getGroupCode();
                    return true;
                case R.id.menu_member:
                    Intent intent = new Intent(getContext(),MembersActivity.class);
                    intent.putExtra("group_type",groupType);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    private void showJoinGroupDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.join_group_layout,null);
        final AlertDialog joinGroupDialog = new AlertDialog.Builder(getContext()).create();
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText edGroupCode = (EditText) view.findViewById(R.id.ed_group_code);
        final Button btnJoin = (Button) view.findViewById(R.id.btn_join);
        btnJoin.setEnabled(false);
        edGroupCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if (text.length()==0){
                    btnJoin.setEnabled(false);
                }else{
                    btnJoin.setEnabled(true);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.cancel();
            }
        });
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinGroupDialog.cancel();
                joinGroup(edGroupCode.getText().toString()).addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(task.isSuccessful()){
                            String result = task.getResult();
                            if (result.equals("failed")){
                                Toast.makeText(getContext(),R.string.accept_invitation_failed,Toast.LENGTH_LONG).show();
                            }else{
                                if (result.equals("joined")){
                                    NewGroupActivity.showTurnOnLocationSharing(getContext(),mFunctions,false);
                                }else{
                                    Toast.makeText(getContext(),R.string.join_request_send,Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
            }
        });
        joinGroupDialog.setView(view);
        joinGroupDialog.show();
    }

    private Task<String> joinGroup(String groupID){
        Map<String,String> data = new HashMap<>();
        data.put("groupID",groupID);
        return mFunctions.getHttpsCallable("joinGroup")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return task.getResult().getData().toString();
                    }
                });
    }

    private void changeMapType(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
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
                        String id = result.get("groupID");
                        String locationSharing = result.get("location sharing");
                        groupType = type;
                        groupID = id;
                        db.getReference("Groups/"+id+"/members/"+currentUser.getUid()+"/state").addValueEventListener(permissionChange);
                        setMapMenu(type,locationSharing);
                        membersRef = db.getReference("Groups/"+id+"/members");
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

    ValueEventListener permissionChange = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            getGroupType().addOnCompleteListener(new OnCompleteListener<Map<String,String>>() {
                @Override
                public void onComplete(@NonNull Task<Map<String,String>> task) {
                    if (task.isSuccessful()){
                        Map<String,String> result = task.getResult();
                        String type = result.get("type");
                        groupType = type;
                        String locationSharing = result.get("location sharing");
                        setMapMenu(type,locationSharing);
                    }
                }
            });
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void setMapMenu(String type, String locationSharing){
        if (type.equals(Helper.INDIVIDUAL_GROUP)){
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.non_group_menu);
        }else {
            if (type.equals(Helper.LEADER_GROUP)){
                if (locationSharing.equals("off")){
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.leader_menu);
                }else{
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.leader_off_menu);
                }
            }else{
                if (locationSharing.equals("off")){
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.member_menu);
                }else{
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.member_off_menu);
                }
            }
        }
    }

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
                    List<Member> members = task.getResult();
                    Map<String,Boolean> checked = new HashMap<>();
                    for (final Member member : members){
                        checked.put(member.uid,true);
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
                                            if (getContext() != null){
                                                if (member.url != null){
                                                    markerBitmap = Helper.getMapMarker(Uri.parse(member.url),getContext());
                                                }else{
                                                    markerBitmap = Helper.getMapMarker(null,getContext());
                                                }
                                            }
                                        } catch (ExecutionException | InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        final MarkerOptions markerOption = new MarkerOptions()
                                                .position(latLng)
                                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
                                        if (getActivity() != null){
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Marker marker = mMap.addMarker(markerOption);
                                                    mapMarker.put(member.uid,marker);
                                                }
                                            });
                                        }
                                    }
                                };
                                executor.execute(addMarker);
                            }
                        }
                        location = latLng;
                    }
                    for (Map.Entry<String,Marker> memberMarker:mapMarker.entrySet()){
                        String key = memberMarker.getKey();
                        Marker marker = memberMarker.getValue();
                        if (!checked.containsKey(key)){
                            marker.remove();
                            mapMarker.remove(key);
                            addMarkerProcessing.remove(key);
                        }
                    }
                    if (firstTime){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10.0f));
                        firstTime = false;
                    }
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

    private void getGroupCode(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String message = getString(R.string.your_group_code_is)+groupID;
        builder.setMessage(message)
                .setPositiveButton(R.string.copy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (getActivity() != null){
                            ClipboardManager clipboard = (ClipboardManager)
                                    getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("simple text", groupID);
                            clipboard.setPrimaryClip(clip);
                            dialogInterface.cancel();
                        }
                    }
                })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
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
