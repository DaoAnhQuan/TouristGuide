package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.bsimagepicker.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.ImageLoaderDelegate, BSImagePicker.OnSelectImageCancelledListener{
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference usernameRef, avatarRef,phoneRef;
    private MaterialToolbar toolbar;
    private CircleImageView imvAvatar;
    private View parent;
    private TextView tvUsername;
    private TextView tvPhone;
    private TextView tvEmail;
    private String avatar = null;
    private FirebaseStorage storage;
    private FirebaseFunctions mFunctions;
    private String avatarUpdateTime;
    private boolean avatarDownload;

    public AccountFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        usernameRef = db.getReference("Users/"+user.getUid()+"/username");
        avatarRef = db.getReference("Users/"+user.getUid()+"/avatar");
        phoneRef = db.getReference("Users/"+user.getUid()+"/phone");
        storage = FirebaseStorage.getInstance();
        mFunctions = Helper.initFirebaseFunctions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.activity_account, container, false);
        toolbar = (MaterialToolbar) view.findViewById(R.id.top_app_bar_account);
        imvAvatar = (CircleImageView) view.findViewById(R.id.imv_avatar);
        tvUsername = (TextView) view.findViewById(R.id.tv_username);
        tvPhone = (TextView) view.findViewById(R.id.tv_tel);
        tvEmail = (TextView) view.findViewById(R.id.tv_email);
        parent = view;
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_log_out:
                        mAuth.signOut();
                        Intent toLoginActivity = new Intent(getContext(),LoginActivity.class);
                        startActivity(toLoginActivity);
                        getActivity().finish();
                        return true;
                    case R.id.menu_change_avatar:
                        BSImagePicker singleSelectionPicker = new BSImagePicker.Builder("com.android.touristguide.fileprovider")
                                .setMaximumDisplayingImages(200) //Default: Integer.MAX_VALUE. Don't worry about performance :)
                                .setSpanCount(3) //Default: 3. This is the number of columns
                                .setGridSpacing(Utils.dp2px(2)) //Default: 2dp. Remember to pass in a value in pixel.
                                .setPeekHeight(Utils.dp2px(360)) //Default: 360dp. This is the initial height of the dialog.
                                .hideGalleryTile() //Default: show. Set this if you don't want to further let user select from a gallery app. In such case, I suggest you to set maximum displaying images to Integer.MAX_VALUE.
                                .setTag("A request ID") //Default: null. Set this if you need to identify which picker is calling back your fragment / activity.
                                .useFrontCamera() //Default: false. Launching camera by intent has no reliable way to open front camera so this does not always work.
                                .build();
                        singleSelectionPicker.show(getChildFragmentManager(),"picker");
                        return true;
                    case R.id.menu_change_username:

                        return  true;
                }
                return false;
            }
        });
        Helper.setTextViewUI(tvEmail,"Email: "+mAuth.getCurrentUser().getEmail(),"#FFFFFF","#000000",true);
        imvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (avatar != null){
                    Intent intent = new Intent(getContext(),ShowImageActivity.class);
                    intent.putExtra("image_url",avatar);
                    startActivity(intent);
                }
            }
        });
        usernameRef.addValueEventListener(usernameListener);
        avatarRef.addValueEventListener(avatarListener);
        phoneRef.addValueEventListener(phoneListener);
        return view;
    }

    ValueEventListener usernameListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String username = snapshot.getValue().toString();
            toolbar.setTitle(username);
            Helper.setTextViewUI(tvUsername,username,"#FFFFFF","#000000",true);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ValueEventListener avatarListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            try{
                avatar = snapshot.child("url").getValue().toString();
                avatarUpdateTime = snapshot.child("time").getValue().toString();
                avatarDownload = snapshot.child("download").getValue(Boolean.class);
                Uri avatarUri = Uri.parse(avatar);
                Glide.with(parent).load(avatarUri).into(imvAvatar);
            }catch (NullPointerException e){
                Glide.with(parent).load(ContextCompat.getDrawable(getContext(),R.drawable.ic_baseline_person_white_24)).into(imvAvatar);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    ValueEventListener phoneListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            try {
                String phone = snapshot.getValue().toString();
                String text = getString(R.string.tel)+" "+phone;
                Helper.setTextViewUI(tvPhone,text,"#FFFFFF","#000000",true);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {
        Log.d("ImagePicker",uri.toString());
        String fbFilename = Helper.createFirebaseStorageFilename(uri);
        final StorageReference ref = storage.getReference().child(fbFilename);
        Glide.with(parent).load(uri).into(imvAvatar);
        UploadTask uploadTask = ref.putFile(uri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    Map<String,String> data = new HashMap<>();
                    data.put("url",downloadUri.toString());
                    mFunctions.getHttpsCallable("updateAvatar").call(data);
                }else{
                    Toast.makeText(getContext(),getString(R.string.upload_failed),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void loadImage(Uri imageUri, ImageView ivImage) {
        Glide.with(getContext()).load(imageUri).into(ivImage);
    }

    @Override
    public void onCancelled(boolean isMultiSelecting, String tag) {

    }
}