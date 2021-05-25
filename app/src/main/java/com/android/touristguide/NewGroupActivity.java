package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupActivity extends AppCompatActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate, BSImagePicker.OnSelectImageCancelledListener{
    private MaterialToolbar toolbarNewGroup;
    private RecyclerView listUser;
    private TextView tvSelected;
    private CircleImageView imvGroupPhoto;
    private ConstraintLayout groupSetting;
    private EditText edGroupName;
    private Button btnGroupPhoto;
    private Button btnSubmitGroupInfo;
    private TextView tvGroupName;
    private ConstraintLayout bottomSheetLayout;
    public static BottomSheetBehavior sheetBehavior;
    private EditText edSearchUser;
    public static Map<String,User> selectedUsers;
    @SuppressLint("StaticFieldLeak")
    public static SelectedUsersAdapter selectedUsersAdapter;
    @SuppressLint("StaticFieldLeak")
    public static ListUserNewGroupAdapter adapter;
    private Uri groupPhoto = null;
    private FirebaseFunctions mFunctions;
    private android.app.AlertDialog loadingDialog;
    private FirebaseStorage storage;
    private String TAG = "NewGroupActivityTAG";
    private boolean addMember;
    private TextView tvNoUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        toolbarNewGroup = (MaterialToolbar) findViewById(R.id.top_app_bar_new_group);
        listUser = (RecyclerView) findViewById(R.id.rcv_list_user);
        tvSelected = (TextView) findViewById(R.id.tv_selected);
        tvSelected.setVisibility(View.GONE);
        imvGroupPhoto = (CircleImageView) findViewById(R.id.imv_group_photo);
        imvGroupPhoto.setVisibility(View.GONE);
        btnGroupPhoto = (Button) findViewById(R.id.btn_group_photo);
        btnGroupPhoto.setOnClickListener(btnGroupPhotoOnclickListener);
        groupSetting = (ConstraintLayout) findViewById(R.id.group_setting);
        edGroupName = (EditText) findViewById(R.id.ed_group_name);
        btnSubmitGroupInfo = (Button) findViewById(R.id.btn_submit_group_info);
        btnSubmitGroupInfo.setEnabled(false);
        btnSubmitGroupInfo.setOnClickListener(btnSubmitGroupSettingClickListener);
        tvNoUser = findViewById(R.id.tv_no_user);
        toolbarNewGroup.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        edGroupName.addTextChangedListener(groupNameWatcher);
        tvGroupName = (TextView) findViewById(R.id.tv_group_name);
        edSearchUser = (EditText) findViewById(R.id.ed_search_user);
        edSearchUser.addTextChangedListener(searchTextWatcher);
        bottomSheetLayout = (ConstraintLayout) findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        sheetBehavior.setHideable(true);
        sheetBehavior.setPeekHeight(0);
        selectedUsers = new LinkedHashMap<>();
        RecyclerView rcvSelectedUsers = (RecyclerView)findViewById(R.id.rcv_selected_user);
        selectedUsersAdapter = new SelectedUsersAdapter(this, tvSelected);
        rcvSelectedUsers.setAdapter(selectedUsersAdapter);
        LinearLayoutManager selectedUserLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rcvSelectedUsers.setLayoutManager(selectedUserLayoutManager);
        mFunctions = Helper.initFirebaseFunctions();
        loadingDialog = Helper.createLoadingDialog(this);
        Button btnCreateGroup = (Button) findViewById(R.id.btn_create_group);
        storage = FirebaseStorage.getInstance();
        addMember = getIntent().getBooleanExtra("add_member",false);
        if (addMember){
            groupSetting.setVisibility(View.GONE);
            tvGroupName.setText(R.string.add_member);
            btnCreateGroup.setOnClickListener(addMemberOnClickListener);
        }else{
            btnCreateGroup.setOnClickListener(createGroupListener);
        }

    }

    View.OnClickListener createGroupListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadingDialog.show();
            final Map<String,Object> data = new HashMap<>();
            String groupName = "New Group";
            if (!edGroupName.getText().toString().trim().isEmpty()){
                groupName = edGroupName.getText().toString().trim();
            }
            data.put("name",groupName);
            JSONObject members = new JSONObject(selectedUsers);
            data.put("members",members);
            if (groupPhoto == null){
                createGroup(data);
            }else{
                String fbFilename = Helper.createFirebaseStorageFilename(groupPhoto);
                final StorageReference ref = storage.getReference().child(fbFilename);
                UploadTask uploadTask = ref.putFile(groupPhoto);
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
                            data.put("photo",task.getResult().toString());
                            createGroup(data);
                        }
                    }
                });
            }
        }
    };

    View.OnClickListener addMemberOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            loadingDialog.show();
            JSONObject members = new JSONObject(selectedUsers);
            Map<String,Object> data = new HashMap<>();
            data.put("members",members);
            mFunctions.getHttpsCallable("addMember")
                    .call(data)
                    .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                        @Override
                        public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                            if (task.isSuccessful()){
                                loadingDialog.cancel();
                                finish();
                            }
                        }
                    });
        }
    };

    private void createGroup(Map<String,Object> data){
        mFunctions.getHttpsCallable("createGroup")
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        loadingDialog.cancel();
                        if (task.isSuccessful()){
                            showTurnOnLocationSharing(NewGroupActivity.this,mFunctions,true);
                        }else{
                            Log.d(TAG,task.getException().getMessage());
                        }
                    }
                });
    }

    public static void showTurnOnLocationSharing(final Context context, final FirebaseFunctions mFunctions, final boolean finishCurrentActivity){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.share_location_setting)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        updateLocationSetting(mFunctions,"yes");
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        updateLocationSetting(mFunctions,"no");
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (finishCurrentActivity){
                            Helper.finishActivityFromContext(context);
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void updateLocationSetting(FirebaseFunctions mFunctions, String result){
        Map<String,String> data = new HashMap<>();
        data.put("result",result);
        mFunctions.getHttpsCallable("updateLocationSetting")
                .call(data);
    }

    Button.OnClickListener btnGroupPhotoOnclickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
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
    };

    TextWatcher groupNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length()>0){
                btnSubmitGroupInfo.setEnabled(true);
            }else{
                btnSubmitGroupInfo.setEnabled(false);
            }
        }
    };

    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String query = editable.toString().trim();
            if (query.length()>3){
                try {
                    Helper.searchUsers(query).addOnCompleteListener(new OnCompleteListener<List<User>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<User>> task) {
                            List<User> userList = task.getResult();
                            if (userList.size()>0){
                                tvNoUser.setVisibility(View.GONE);
                                tvSelected.setVisibility(View.VISIBLE);
                                adapter = new ListUserNewGroupAdapter(NewGroupActivity.this,task.getResult(),tvSelected);
                                listUser.setAdapter(adapter);
                                listUser.setLayoutManager(new LinearLayoutManager(NewGroupActivity.this));
                            }else{
                                tvNoUser.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                adapter = new ListUserNewGroupAdapter(NewGroupActivity.this,new ArrayList<User>(),tvSelected);
                listUser.setAdapter(adapter);
                listUser.setLayoutManager(new LinearLayoutManager(NewGroupActivity.this));
                tvNoUser.setVisibility(View.VISIBLE);
            }
        }
    };

    Button.OnClickListener btnSubmitGroupSettingClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            tvGroupName.setText(edGroupName.getText().toString());
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        groupPhoto = uri;
        Glide.with(this).load(uri).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imvGroupPhoto.setVisibility(View.VISIBLE);
                return false;
            }
        }).into(imvGroupPhoto);
    }
}