package com.android.touristguide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity  implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate, BSImagePicker.OnSelectImageCancelledListener{
    private FirebaseStorage storage;
    private FirebaseFunctions mFunctions;
    private String groupID;
    private EditText edMessage;
    private MaterialButton btnSend;
    private List<Message> listMessages;
    private ChatAdapter adapter;
    private RecyclerView rcvMessages;
    private final String TAG = "ChatActivityTAG";
    private String groupName;
    private TextView tvGroupName;
    private CircleImageView imvPhoto;
    private Toolbar toolbar;
    private int mode;
    private final int  SEND_PHOTO_MODE = 0;
    private final int  CHANGE_GROUP_PHOTO_MODE = 1;
    private String bingMapApiKey;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        storage = FirebaseStorage.getInstance();
        mFunctions = Helper.initFirebaseFunctions();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        listMessages = new ArrayList<>();
        groupName = getIntent().getStringExtra("group name");
        String groupPhoto = getIntent().getStringExtra("group photo");
        groupID = getIntent().getStringExtra("groupID");
        imvPhoto = findViewById(R.id.imv_group_photo);
        LinearLayout llGroupTitle = findViewById(R.id.ll_group_title);
        Helper.loadAvatar(groupPhoto,imvPhoto,llGroupTitle,this,R.drawable.ic_baseline_group_24);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvGroupName.setText(groupName);

        Button btnPhoto = findViewById(R.id.btn_photo);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode=SEND_PHOTO_MODE;
                pickPhoto();
            }
        });

        btnSend = findViewById(R.id.btn_send_message);
        edMessage = findViewById(R.id.ed_message);
        edMessage.addTextChangedListener(messageTextWatcher);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                edMessage.setText("");
            }
        });


        DatabaseReference messageRef = db.getReference("Groups/"+groupID+"/messages");
        messageRef.addChildEventListener(messageEventListener);
        toolbar = findViewById(R.id.top_app_bar_chat);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.menu_change_group_name){
                    showGroupNameDialog();
                }else{
                    mode = CHANGE_GROUP_PHOTO_MODE;
                    pickPhoto();
                }
                return false;
            }
        });
        db.getReference("Users/"+currentUser.getUid()+"/group").addValueEventListener(groupChangeEventListener);
        db.getReference("Groups/"+groupID+"/name").addValueEventListener(groupNameChangeListener);
        db.getReference("Groups/"+groupID+"/photo").addValueEventListener(groupPhotoChangeListener);
        ApplicationInfo app = null;
        try {
            app = this.getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = app.metaData;
        bingMapApiKey = bundle.getString("com.bing.geo.API_KEY");
        rcvMessages = findViewById(R.id.rcv_message);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvMessages.setLayoutManager(layoutManager);
        adapter = new ChatAdapter(this,listMessages,currentUser,bingMapApiKey);
        rcvMessages.setAdapter(adapter);
    }

    ValueEventListener groupNameChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            groupName = snapshot.getValue().toString();
            tvGroupName.setText(groupName);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ValueEventListener groupPhotoChangeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String groupPhoto = "";
            if (snapshot.getValue() != null){
                groupPhoto = snapshot.getValue().toString();
            }
            Helper.loadAvatar(groupPhoto,imvPhoto,toolbar,ChatActivity.this,R.drawable.ic_baseline_group_24);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void showGroupNameDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.change_group_name_layout,null);
        final AlertDialog changeGroupNameDialog = new AlertDialog.Builder(this).create();
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        final EditText edGroupName = (EditText) view.findViewById(R.id.ed_group_name);
        final Button btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setEnabled(false);
        edGroupName.setText(groupName);
        edGroupName.selectAll();
        edGroupName.addTextChangedListener(new TextWatcher() {
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
                    btnSave.setEnabled(false);
                }else{
                    btnSave.setEnabled(true);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeGroupNameDialog.cancel();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeGroupNameDialog.cancel();
                Map<String,String> data = new HashMap<>();
                data.put("groupID",groupID);
                data.put("groupName",edGroupName.getText().toString());
                mFunctions.getHttpsCallable("setGroupName").call(data);
            }
        });
        changeGroupNameDialog.setView(view);
        changeGroupNameDialog.show();
    }

    ValueEventListener groupChangeEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!snapshot.getValue().toString().equals(groupID)){
                finish();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ChildEventListener messageEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            Message message = snapshot.getValue(Message.class);
            listMessages.add(message);
            int lastPosition = listMessages.size()-1;
            adapter.notifyItemInserted(lastPosition);
            rcvMessages.smoothScrollToPosition(lastPosition);
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

    TextWatcher messageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            if (text.length() == 0){
                btnSend.setEnabled(false);
            }else{
                btnSend.setEnabled(true);
            }
        }
    };
    private void sendMessage(){
        String message = edMessage.getText().toString().trim();
        Map<String,String> data = new HashMap<>();
        data.put("text",message);
        data.put("groupID",groupID);
        mFunctions.getHttpsCallable("addTextMessage").call(data);
    }
    private void pickPhoto(){
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
                    Map<String,String> data = new HashMap<>();
                    data.put("groupID",groupID);
                    data.put("url",task.getResult().toString());
                    if (mode == SEND_PHOTO_MODE){
                        mFunctions.getHttpsCallable("addPhotoMessage").call(data);
                    }else{
                        mFunctions.getHttpsCallable("setGroupPhoto").call(data);
                    }
                }else{
                    Toast.makeText(ChatActivity.this,getString(R.string.upload_failed),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
