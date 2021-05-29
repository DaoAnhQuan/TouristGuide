package com.android.touristguide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostCommentActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate, BSImagePicker.OnSelectImageCancelledListener{
    private TextView tvNoComment;
    private RecyclerView rcvComment;
    private Button btnPhoto;
    private EditText edComment;
    private Button btnSend;
    private MaterialToolbar toolbar;
    private String postID;
    private FirebaseFunctions mFunctions;
    private final String TAG = "PostCommentActivityTAG";
    private FirebaseStorage storage;
    private PostCommentAdapter adapter;
    private List<Comment> comments;
    private FirebaseDatabase db;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_comment);
        init();
        postID = getIntent().getStringExtra("postID");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        setupEditextComment();
        setupButtonSend(this);
        comments = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rcvComment.setLayoutManager(layoutManager);
        adapter = new PostCommentAdapter(this,comments);
        rcvComment.setAdapter(adapter);
        db.getReference("Posts/"+postID+"/comments").addChildEventListener(commentEventListener);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }
    private void selectImage(){
        BSImagePicker singleSelectionPicker = new BSImagePicker.Builder("com.android.touristguide.fileprovider")
                .setMaximumDisplayingImages(200) //Default: Integer.MAX_VALUE. Don't worry about performance :)
                .setSpanCount(3) //Default: 3. This is the number of columns
                .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
                .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
                .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
                .setTag("A request ID") //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
                .useFrontCamera() //Default: false. Launching camera by intent has no reliable way to open front camera so this does not always work.
                .build();
        singleSelectionPicker.show(getSupportFragmentManager(),"picker");
    }
    ChildEventListener commentEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Comment comment = snapshot.getValue(Comment.class);
            comments.add(0,comment);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };
    private void setupEditextComment(){
        edComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length()>0){
                    btnSend.setEnabled(true);
                }else{
                    btnSend.setEnabled(false);
                }
            }
        });
    }
    private void setupButtonSend(Context context){
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = edComment.getText().toString().trim();
                addComment("text",comment,context);
                edComment.setText("");
            }
        });
    }
    private void addComment(String type,String content,Context context){
        Map<String,String> data = new HashMap<>();
        data.put("postID",postID);
        data.put("type",type);
        data.put("content",content);
        mFunctions.getHttpsCallable("addComment").call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        String result = task.getResult().getData().toString();
                        if (result.equals("fail")){
                            Toast.makeText(context,R.string.post_deleted,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void init() {
        tvNoComment = findViewById(R.id.tv_no_comment);
        rcvComment = findViewById(R.id.rcv_comment);
        btnPhoto = findViewById(R.id.btn_photo);
        btnSend = findViewById(R.id.btn_send);
        edComment = findViewById(R.id.ed_comment);
        toolbar = findViewById(R.id.top_app_bar_comment);
        mFunctions = Helper.initFirebaseFunctions();
        storage = FirebaseStorage.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(this).load(imageUri).into(ivImage);
    }

    @Override
    public void onCancelled(boolean isMultiSelecting, String tag) {

    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
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
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    addComment("photo",task.getResult().toString(),PostCommentActivity.this);
                }
            }
        });
    }
}
