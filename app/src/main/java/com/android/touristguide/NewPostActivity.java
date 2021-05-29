package com.android.touristguide;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewPostActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate, BSImagePicker.OnSelectImageCancelledListener{
    private RecyclerView rcvPhotos;
    private List<Uri> listPhotoUri;
    private PostPhotosAdapter adapter;
    private Button btnPhotography;
    private Button btnFood;
    private Button btnTravel;
    private Button btnActivity;
    private List<Button> topicButtonList;
    private Button btnAddLocation;
    private final int ADD_LOCATION_REQUEST_CODE = 5;
    private final String TAG = "NewPostActivityTAG";
    private String bingMapApiKey;
    private ImageView imvStaticMap;
    private LatLng location;
    private final String[] topics = {"Photography","Food and Drink","Travel","Activity"};
    private EditText edTitle;
    private EditText edDescription;
    private FirebaseStorage storage;
    private List<String> listDownloadUri;
    private FirebaseFunctions mFunctions;
    private AlertDialog loadingDialog;
    private Post post;
    private Button btnPublish;
    private List<Boolean> isFirebasePhoto;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        getBingMapApiKey();
        if (getIntent().getSerializableExtra("post") != null){
            post = (Post)getIntent().getSerializableExtra("post");
        }
        storage = FirebaseStorage.getInstance();
        mFunctions = Helper.initFirebaseFunctions();
        listPhotoUri = new ArrayList<>();
        listDownloadUri = new ArrayList<>();
        isFirebasePhoto = new ArrayList<>();
        loadingDialog = Helper.createLoadingDialog(this);
        rcvPhotos = findViewById(R.id.rcv_photos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rcvPhotos.setLayoutManager(linearLayoutManager);
        adapter = new PostPhotosAdapter(this,listPhotoUri,isFirebasePhoto);
        rcvPhotos.setAdapter(adapter);
        btnPhotography = findViewById(R.id.btn_photography);
        btnFood = findViewById(R.id.btn_food);
        btnTravel = findViewById(R.id.btn_travel);
        btnActivity = findViewById(R.id.btn_activity);
        edTitle = findViewById(R.id.ed_title);
        edDescription = findViewById(R.id.ed_description);
        topicButtonList = new ArrayList<>();
        setupTopicButton();
        btnAddLocation = findViewById(R.id.btn_add_location);
        btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewPostActivity.this,AddLocationActivity.class);
                startActivityForResult(intent,ADD_LOCATION_REQUEST_CODE);
            }
        });
        imvStaticMap = findViewById(R.id.imv_static_map);
        imvStaticMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NewPostActivity.this,AddLocationActivity.class);
                intent.putExtra("latitude",location.latitude);
                intent.putExtra("longitude",location.longitude);
                startActivityForResult(intent,ADD_LOCATION_REQUEST_CODE);
            }
        });
        MaterialToolbar toolbar = findViewById(R.id.top_app_bar_new_post);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (post != null){
            toolbar.setTitle(R.string.edit_post);
        }
        btnPublish = findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publish();
            }
        });
        if (post != null){
            showPostDetail();
        }
    }

    private void showPostDetail(){
        edTitle.setText(post.title);
        edDescription.setText(post.description);
        if (imvStaticMap.getVisibility() == View.GONE){
            btnAddLocation.setVisibility(View.GONE);
            imvStaticMap.setVisibility(View.VISIBLE);
        }
        location = new LatLng(post.latitude,post.longitude);
        String url = getStaticMapLink(post.latitude,post.longitude);
        Glide.with(this).load(url).into(imvStaticMap);
        btnPublish.setText(R.string.save);
        try {
            JSONArray photos = new JSONArray(post.photo);
            for (int i = 0; i<photos.length();i++){
                String photoUrl = photos.get(i).toString();
                listPhotoUri.add(Uri.parse(photoUrl));
                isFirebasePhoto.add(true);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBingMapApiKey(){
        ApplicationInfo app = null;
        try {
            app = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (app != null){
            Bundle bundle = app.metaData;
            bingMapApiKey = bundle.getString("com.bing.geo.API_KEY");
        }
    }

    private void uploadMultipleFile(final int  index){
        if (index == listPhotoUri.size()){
            Map<String,Object> data = new HashMap<>();
            if (post != null){
                data.put("action","update");
                data.put("postID",post.postID);
            }else{
                data.put("action","create");
            }
            data.put("latitude",location.latitude);
            data.put("longitude",location.longitude);
            data.put("topic",getTopic());
            data.put("title",edTitle.getText().toString().trim());
            data.put("description",edDescription.getText().toString().trim());
            data.put("photoUrls",listDownloadUri);
            mFunctions.getHttpsCallable("createPost").call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                @Override
                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                    loadingDialog.cancel();
                    setResult(1);
                    finish();
                }
            });
            return;
        }
        Uri uri = listPhotoUri.get(index);
        if (!isFirebasePhoto.get(index)){
            final String fbFilename = Helper.createFirebaseStorageFilename(uri);
            final StorageReference ref = storage.getReference().child(fbFilename);
            UploadTask uploadTask = ref.putFile(uri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    listDownloadUri.add(uri.toString());
                    uploadMultipleFile(index+1);
                }
            });
        }else{
            listDownloadUri.add(uri.toString());
            uploadMultipleFile(index+1);
        }
    }

    private void publish(){
        if (!checkInformation()){
            return;
        }
        loadingDialog.show();
        uploadMultipleFile(0);
    }

    private boolean checkInformation(){
        if (getTopic() == null){
            Toast.makeText(this,R.string.topic_failed,Toast.LENGTH_LONG).show();
            return false;
        }
        if (edTitle.getText().toString().trim().length()==0){
            Toast.makeText(this,R.string.title_failed,Toast.LENGTH_LONG).show();
            return false;
        }
        if (edDescription.getText().toString().trim().length()<2){
            Toast.makeText(this,R.string.description_failed,Toast.LENGTH_LONG).show();
            return false;
        }
        if (location == null){
            Toast.makeText(this,R.string.location_failed,Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private String getTopic(){
        for (int i = 0; i<topicButtonList.size();i++){
            Button button = topicButtonList.get(i);
            if ((boolean)button.getTag()){
                return topics[i];
            }
        }
        return null;
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(this).load(imageUri).into(ivImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_LOCATION_REQUEST_CODE){
            if (resultCode == 1 && data != null){
                location = new LatLng(data.getDoubleExtra("latitude",0),data.getDoubleExtra("longitude",0));
                if (imvStaticMap.getVisibility() == View.GONE){
                    btnAddLocation.setVisibility(View.GONE);
                    imvStaticMap.setVisibility(View.VISIBLE);
                }
                String url = getStaticMapLink(location.latitude,location.longitude);
                Glide.with(this).load(url).into(imvStaticMap);
            }
        }
    }

    private void setupTopicButton(){
        topicButtonList.add(btnPhotography);
        topicButtonList.add(btnFood);
        topicButtonList.add(btnTravel);
        topicButtonList.add(btnActivity);
        for (int i = 0; i<topicButtonList.size();i++){
            Button button = topicButtonList.get(i);
            button.setTag(false);
        }
        if (post != null){
            int index = 0;
            for (int i = 1; i<topics.length;i++){
                if (topics[i].equals(post.topic)){
                    index = i;
                }
            }
            Button button = topicButtonList.get(index);
            button.setTag(true);
            button.setBackgroundColor(Color.parseColor("#1964E6"));
        }else{
            Button button = topicButtonList.get(0);
            button.setTag(true);
            button.setBackgroundColor(Color.parseColor("#1964E6"));
        }
        for (int i = 0; i<topicButtonList.size();i++){
            Button button = topicButtonList.get(i);
            int finalI = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean tag = (boolean) button.getTag();
                    if (!tag){
                        button.setTag(true);
                        button.setBackgroundColor(Color.parseColor("#1964E6"));
                        for (int j = 0; j<topicButtonList.size();j++){
                            if (j != finalI){
                                Button button1 = topicButtonList.get(j);
                                button1.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                button1.setTag(false);
                            }
                        }
                    }else{
                        button.setTag(false);
                        button.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    }
                }
            });
        }

    }

    @Override
    public void onCancelled(boolean isMultiSelecting, String tag) {

    }

    private String getStaticMapLink(double latitude, double longitude){
        String mapLink = "http://dev.virtualearth.net/REST/v1/Imagery/Map/Road/"
                +String.valueOf(latitude)+","
                +String.valueOf(longitude)
                +"/16?mapSize=400,200&pp="
                +String.valueOf(latitude)+","
                +String.valueOf(longitude)+";66&mapLayer=Basemap,Buildings&key="
                +bingMapApiKey;
        return mapLink;
    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        listPhotoUri.add(uri);
        isFirebasePhoto.add(false);
        adapter.notifyItemInserted(listPhotoUri.size()-1);
        rcvPhotos.scrollToPosition(listPhotoUri.size());
    }
}
