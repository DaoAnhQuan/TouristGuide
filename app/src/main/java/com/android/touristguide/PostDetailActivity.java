package com.android.touristguide;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.functions.FirebaseFunctions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {
    private CircleImageView imvAvatar;
    private TextView tvUsername;
    private TextView tvTime;
    private TextView tvTitle;
    private TextView tvDescription;
    private ImageSlider imageSlider;
    private MaterialToolbar toolbar;
    private Button btnReport;
    private MaterialButton btnLike,bntShare;
    private FirebaseFunctions mFunctions;
    private TextView tvLocationDetail;
    private boolean isLiked;
    private final int EDIT_POST_REQUEST_CODE = 0;
    private Button btnComment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_detail);
        Post post = (Post)getIntent().getSerializableExtra("Post");
        init();
        this.isLiked = post.isLiked;
        toolbar.setTitle(post.ownerName+"\'s post");
        ConstraintLayout postDetailLayout = findViewById(R.id.post_detail_layout);
        Helper.loadAvatar(post.ownerAvatar,imvAvatar,postDetailLayout,this,R.drawable.ic_baseline_person_white_24);
        tvUsername.setText(Helper.getBoldString(post.ownerName));
        tvTime.setText(post.time);
        tvTitle.setText(Helper.getBoldString(post.title));
        tvDescription.setText(post.description);
        setupImageSlider(post.photo);
        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportConfirmDialog(post);
            }
        });
        if (post.isOwner){
            toolbar.inflateMenu(R.menu.post_owner_menu);
            btnReport.setVisibility(View.GONE);
        }else{
            if (post.isReported){
                btnReport.setEnabled(false);
            }else{
                btnReport.setEnabled(true);
            }
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if (post.isLiked){
            btnLike.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_baseline_favorite_24));
            btnLike.setIconTint(ContextCompat.getColorStateList(this,R.color.report_button_color));
        }
        setupLikeButton(post,this);
        setupPostLocationDetail(post,this);
        setupToolbar(post,this);
        setupButtonComment(post,this);
    }

    private void setupButtonComment(Post post,Context context){
        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,PostCommentActivity.class);
                intent.putExtra("postID",post.postID);
                startActivity(intent);
            }
        });
    }
    private void setupToolbar(Post post,Context context){
        toolbar.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_post_edit:
                        editPost(post,context);
                        return true;
                    default:
                        deletePost(post,context);
                        return true;
                }
            }
        });
    }
    private void editPost(Post post, Context context){
        Intent intent = new Intent(context,NewPostActivity.class);
        intent.putExtra("post",post);
        startActivityForResult(intent,EDIT_POST_REQUEST_CODE);
    }

    private void deletePost(Post post, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_post_confirm)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Map<String,String> data = new HashMap<>();
                        data.put("postID",post.postID);
                        mFunctions.getHttpsCallable("deletePost").call(data);
                        Intent intent = new Intent();
                        intent.putExtra("postID",post.postID);
                        ((Activity)context).setResult(0,intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_POST_REQUEST_CODE){
            if (resultCode == 1){
                finish();
            }
        }
    }

    private void setupLikeButton(Post post, Context context){
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String,String> data = new HashMap<>();
                data.put("postID",post.postID);
                if (isLiked){
                    isLiked = false;
                    data.put("action","dislike");
                    btnLike.setIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_favorite_border_24));
                    btnLike.setIconTint(ContextCompat.getColorStateList(context,R.color.like_button_color));
                }else{
                    isLiked = true;
                    data.put("action","like");
                    btnLike.setIcon(ContextCompat.getDrawable(context,R.drawable.ic_baseline_favorite_24));
                    btnLike.setIconTint(ContextCompat.getColorStateList(context,R.color.report_button_color));
                }
                mFunctions.getHttpsCallable("likePost").call(data);
            }
        });
    }
    private void setupPostLocationDetail(Post post, Context context){
        tvLocationDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,PostLocationDetailActivity.class);
                intent.putExtra("latitude",post.latitude);
                intent.putExtra("longitude",post.longitude);
                startActivity(intent);
            }
        });
    }

    private void showReportConfirmDialog(Post post){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.post_report_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        Map<String,String> data = new HashMap<>();
                        data.put("postID",post.postID);
                        mFunctions.getHttpsCallable("reportPost").call(data);
                        btnReport.setEnabled(false);
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
    }
    private void init(){
        imvAvatar = findViewById(R.id.imv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvTime = findViewById(R.id.tv_time);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        toolbar = findViewById(R.id.top_app_bar_post_detail);
        imageSlider = findViewById(R.id.image_slider);
        btnReport = findViewById(R.id.btn_report);
        btnLike = findViewById(R.id.btn_like);
        bntShare = findViewById(R.id.btn_share);
        mFunctions = Helper.initFirebaseFunctions();
        tvLocationDetail = findViewById(R.id.tv_post_location_click);
        btnComment = findViewById(R.id.btn_comment);
    }
    private void setupImageSlider(String photo){
        try {
            JSONArray jsonArray = new JSONArray(photo);
            List<SlideModel> imageList = new ArrayList<>();
            for (int i = 0; i<jsonArray.length();i++){
                String url = jsonArray.get(i).toString();
                imageList.add(new SlideModel(url,"", ScaleTypes.CENTER_INSIDE));
            }
            imageSlider.setImageList(imageList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
