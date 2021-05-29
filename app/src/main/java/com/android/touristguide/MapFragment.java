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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
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
    private String groupName;
    private String groupPhoto;
    private List<User> listMembers;
    private Marker lastLocationMarker;
    private Message lastLocationMessage;
    private Message locationFromChat;
    private Marker locationFromChatMarker;
    public static final int CHAT_REQUEST_CODE = 0;
    private Button btnStopSOS;
    private Map<String,Boolean> memberSOS;
    private LatLng postLocation;
    private Marker postLocationMarker;
    public MapFragment(){

    }

    public MapFragment(double latitude,double longitude){
        postLocation = new LatLng(latitude,longitude);
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
        memberSOS = new HashMap<>();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHAT_REQUEST_CODE){
            if (resultCode == ChatAdapter.LOCATION_RESULT_CODE && data != null){
                locationFromChat = new Message(data.getStringExtra("message"),"","",
                        data.getStringExtra("fromName"),data.getStringExtra("fromUrl"),
                        data.getStringExtra("time"));
                LatLng position = new LatLng(data.getDoubleExtra("latitude",0f),
                        data.getDoubleExtra("longitude",0f));
                if (locationFromChatMarker == null){
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .zIndex(10);
                    locationFromChatMarker = mMap.addMarker(markerOptions);
                }else{
                    locationFromChatMarker.setPosition(position);
                }
            }
        }
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
        btnStopSOS = (Button) view.findViewById(R.id.btn_stop_sos);
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
        btnStopSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,Boolean> data = new HashMap<>();
                data.put("sos",false);
                mFunctions.getHttpsCallable("updateSOS").call(data);
                btnStopSOS.setVisibility(View.GONE);
            }
        });
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
                    changeLeader();
                    return true;
                case R.id.menu_get_group_code:
                    getGroupCode();
                    return true;
                case R.id.menu_member:
                    Intent intent = new Intent(getContext(),MembersActivity.class);
                    intent.putExtra("group_type",groupType);
                    intent.putExtra("group_id",groupID);
                    startActivity(intent);
                    return true;
                case R.id.menu_chat:
                    db.getReference("Users/"+currentUser.getUid()+"/unread_messages").setValue(0);
                    Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                    chatIntent.putExtra("group name",groupName);
                    chatIntent.putExtra("group photo",groupPhoto);
                    chatIntent.putExtra("groupID",groupID);
                    startActivityForResult(chatIntent,CHAT_REQUEST_CODE);
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
                        db.getReference("Groups/"+id+"/lastLocationMessage").addValueEventListener(lastLocationEventListener);
                        groupType = type;
                        groupID = id;
                        groupName = result.get("group name");
                        groupPhoto = result.get("group photo");
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

    ValueEventListener lastLocationEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()){
                Map<String,String> result = (HashMap<String,String>)snapshot.getValue();
                String fromName = result.get("fromName");
                String fromUrl = result.get("fromUrl");
                String time = result.get("time");
                try {
                    JSONObject jsonObject = new JSONObject(result.get("content").toString());
                    String message = jsonObject.getString("message");
                    lastLocationMessage = new Message(message,"","",fromName,fromUrl,time);
                    double latitude = jsonObject.getDouble("latitude");
                    double longitude = jsonObject.getDouble("longitude");
                    LatLng position = new LatLng(latitude,longitude);
                    if (mMap!=null){
                        if (lastLocationMarker == null){
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(position)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            lastLocationMarker = mMap.addMarker(markerOptions);
                        }else{
                            lastLocationMarker.setVisible(true);
                            lastLocationMarker.setPosition(position);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                if (lastLocationMarker != null){
                    lastLocationMarker.setVisible(false);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void changeLeader(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View changeLeaderView = inflater.inflate(R.layout.activity_change_leader,null);
        final RecyclerView rcvMembers = changeLeaderView.findViewById(R.id.rcv_members);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvMembers.setLayoutManager(linearLayoutManager);
        final SkeletonScreen screen = Skeleton.bind(rcvMembers).show();
        final TextView tvNoActiveMembers = changeLeaderView.findViewById(R.id.tv_no_active_members);
        final AlertDialog changeLeaderDialog = new AlertDialog.Builder(getContext()).create();
        changeLeaderDialog.setView(changeLeaderView);
        changeLeaderDialog.setCanceledOnTouchOutside(true);
        MembersActivity.getMembers(mFunctions).addOnCompleteListener(new OnCompleteListener<List<User>>() {
            @Override
            public void onComplete(@NonNull Task<List<User>> task) {
                if (task.isSuccessful()){
                    List<User> userList = new ArrayList<>();
                    for (User user : task.getResult()){
                        if (user.state.equals("Member")){
                            userList.add(user);
                        }
                    }
                    screen.hide();
                    if (userList.size()==0){
                        rcvMembers.setVisibility(View.INVISIBLE);
                        tvNoActiveMembers.setVisibility(View.VISIBLE);
                    }else{
                        rcvMembers.setVisibility(View.VISIBLE);
                        tvNoActiveMembers.setVisibility(View.INVISIBLE);
                        ListMembersAdapter adapter = new ListMembersAdapter(getActivity(),userList,"leader",changeLeaderDialog);
                        rcvMembers.setAdapter(adapter);
                    }
                }
            }
        });
        changeLeaderDialog.show();
    }

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
        if (postLocation != null){
            toolbar.getMenu().clear();
        }else{
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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
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
            }
        });
        if(postLocation != null){
            postLocationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .position(postLocation)
                    .zIndex(10));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation,10.0f));
        }
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
                        if (currentUser.getUid().equals(member.uid)){
                            if (member.sos){
                                btnStopSOS.setVisibility(View.VISIBLE);
                            }else{
                                btnStopSOS.setVisibility(View.GONE);
                            }
                        }else{
                            if (member.sos && getContext() != null){
                                if (!memberSOS.containsKey(member.uid) || !memberSOS.get(member.uid)){
                                    Helper.createNotification(getContext(),"SOS",getString(R.string.sos_received));
                                }
                            }
                            memberSOS.put(member.uid,member.sos);
                        }
                        if (mapMarker.containsKey(member.uid)){
                            Marker marker = mapMarker.get(member.uid);
                            marker.setVisible(true);
                            marker.setPosition(latLng);
                            Runnable changeMarkerIcon = new Runnable() {
                                @Override
                                public void run() {
                                    if (getContext() != null){
                                        Bitmap markerBitmap = getMarkerBitmap(member);
                                        if (getActivity() != null){
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
                                                }
                                            });
                                        }
                                    }
                                }
                            };
                            executor.execute(changeMarkerIcon);
                        }else{
                            if (!addMarkerProcessing.containsKey(member.uid)){
                                addMarkerProcessing.put(member.uid,true);
                                Runnable addMarker = new Runnable() {
                                    @Override
                                    public void run() {
                                        Bitmap markerBitmap = null;
                                        if (getContext() != null){
                                            markerBitmap = getMarkerBitmap(member);
                                        }
                                        final MarkerOptions markerOption = new MarkerOptions()
                                                .position(latLng)
                                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap));
                                        if (getActivity() != null){
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Marker marker = mMap.addMarker(markerOption);
                                                    marker.setTag(member.uid);
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
                            marker.setVisible(false);
                        }
                    }
                    if (firstTime && mMap !=null){
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10.0f));
                        firstTime = false;
                    }
                    MembersActivity.getMembers(mFunctions).addOnCompleteListener(new OnCompleteListener<List<User>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<User>> task) {
                            listMembers = task.getResult();
                            enableLayout();
                        }
                    });
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private Bitmap getMarkerBitmap(Member member){
        Bitmap markerBitmap = null;
        try {
            if (member.url != null && getContext() != null){
                markerBitmap = Helper.getMapMarker(Uri.parse(member.url),getContext(),member.sos);
            }else{
                markerBitmap = Helper.getMapMarker(null,getContext(),member.sos);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return markerBitmap;
    }
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

    private void showMarkerClickDialog(Marker marker, final User member){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.marker_click_selection,null);
        final AlertDialog markerClickDialog = new AlertDialog.Builder(getContext()).create();
        TextView tvOpenMap = view.findViewById(R.id.tv_open_map);
        TextView tvMemberDetail = view.findViewById(R.id.tv_member_detail);
        final LatLng position = marker.getPosition();
        tvOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClickDialog.cancel();
                startMapIntent(position);
            }
        });
        tvMemberDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClickDialog.cancel();
                Intent intent = new Intent(getContext(),MemberDetailActivity.class);
                intent.putExtra("User",member);
                startActivity(intent);
            }
        });
        markerClickDialog.setView(view);
        markerClickDialog.show();
    }

    private void showLocationPickerDialog(int mode){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.location_picker_menu,null);
        final AlertDialog markerClickDialog = new AlertDialog.Builder(getContext()).create();
        TextView tvOpenMap = view.findViewById(R.id.tv_open_map);
        TextView tvShareLocation = view.findViewById(R.id.tv_share_location);
        TextView tvRemove = view.findViewById(R.id.tv_remove);
        LatLng tmpPosition;
        if (mode == 1){
            tmpPosition = postLocationMarker.getPosition();
        }else{
            tmpPosition = searchMarker.getPosition();
        }
        final LatLng position = tmpPosition;
        tvOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClickDialog.cancel();
                startMapIntent(position);
            }
        });
        if (mode == 0){
            tvRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    markerClickDialog.cancel();
                    searchMarker.setVisible(false);
                }
            });
        }else{
            tvRemove.setVisibility(View.GONE);
        }
        if (groupType.equals("group")){
            tvShareLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    markerClickDialog.cancel();
                    showLocationSharingConfirmation(mode);
                }
            });
        }else{
            tvShareLocation.setVisibility(View.GONE);
        }
        markerClickDialog.setView(view);
        markerClickDialog.show();
    }

    private void showLocationSharingConfirmation(int mode){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.location_share_addition,null);
        final AlertDialog locationSharingDialog = new AlertDialog.Builder(getContext()).create();
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnShare = view.findViewById(R.id.btn_share);
        final EditText edText = view.findViewById(R.id.ed_text);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationSharingDialog.cancel();
            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng position;
                if (mode == 0){
                    position = searchMarker.getPosition();
                    searchMarker.setVisible(false);
                }else{
                    position = postLocationMarker.getPosition();
                }
                Map<String,Object> data = new HashMap<>();
                data.put("groupID",groupID);
                data.put("message", edText.getText().toString().trim());
                data.put("longitude",position.longitude);
                data.put("latitude",position.latitude);
                mFunctions.getHttpsCallable("addLocationMessage").call(data);
                locationSharingDialog.cancel();
            }
        });
        locationSharingDialog.setView(view);
        locationSharingDialog.show();
    }
    private void startMapIntent(LatLng position){
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", position.latitude, position.longitude,position.latitude,position.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");
        if (getActivity() != null && mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        }else{
            Toast.makeText(getContext(),R.string.open_google_map_failed,Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getTag() != null ){
            String memberID = marker.getTag().toString();
            User selectedMember = null;
            for (User member:listMembers){
                if (memberID.equals(member.uid)){
                    selectedMember = member;
                }
            }
            if (selectedMember != null && !selectedMember.uid.equals(currentUser.getUid())){
                showMarkerClickDialog(marker,selectedMember);
                return false;
            }
        }else{
            String markerId = marker.getId();
            if (searchMarker != null && markerId.equals(searchMarker.getId())){
                showLocationPickerDialog(0);
                return false;
            }
            if (lastLocationMarker != null && lastLocationMarker.getId().equals(markerId)){
                showLocationDetailDialog(lastLocationMessage.fromName,lastLocationMessage.fromUrl,
                        lastLocationMessage.time,lastLocationMessage.content,lastLocationMarker.getPosition());
                return false;
            }
            if (locationFromChatMarker != null && locationFromChatMarker.getId().equals(markerId)){
                showLocationDetailDialog(locationFromChat.fromName,locationFromChat.fromUrl,
                        locationFromChat.time,locationFromChat.content,locationFromChatMarker.getPosition());
                return false;
            }
            if (postLocationMarker != null && postLocationMarker.getId().equals(markerId)){
                showLocationPickerDialog(1);
                return false;
            }
        }

        return false;
    }
    private void showLocationDetailDialog(String fromName, String fromUrl, String time, String message, final LatLng position){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.location_detail,null);
        final AlertDialog markerClickDialog = new AlertDialog.Builder(getContext()).create();
        TextView tvUsername = view.findViewById(R.id.tv_username);
        TextView tvTime = view.findViewById(R.id.tv_time);
        CircleImageView imvAvatar = view.findViewById(R.id.imv_avatar);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        Button btnOpenMap = view.findViewById(R.id.btn_open_map);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClickDialog.cancel();
            }
        });
        tvUsername.setText(Helper.getBoldString(fromName));
        Helper.loadAvatar(fromUrl,imvAvatar,view,getContext(),R.drawable.ic_baseline_person_white_24);
        tvTime.setText(time);
        tvMessage.setText(message);
        btnOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markerClickDialog.cancel();
                startMapIntent(position);
            }
        });
        markerClickDialog.setView(view);
        markerClickDialog.show();
    }
}
