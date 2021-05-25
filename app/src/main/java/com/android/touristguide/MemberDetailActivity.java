package com.android.touristguide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ethanhua.skeleton.Skeleton;
import com.google.android.material.appbar.MaterialToolbar;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberDetailActivity extends AppCompatActivity {
    private User user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_detail);
        user = (User)getIntent().getSerializableExtra("User");
        MaterialToolbar toolbar = findViewById(R.id.top_app_bar_member_detail);
        CircleImageView imvAvatar = findViewById(R.id.imv_avatar);
        ConstraintLayout ctlMemberDetail = findViewById(R.id.ctl_member_detail);
        TextView tvUsername = findViewById(R.id.tv_username);
        TextView tvEmail = findViewById(R.id.tv_email);
        TextView tvPhone = findViewById(R.id.tv_phone);


        Button btnEmail = findViewById(R.id.btn_email);
        Button btnCall = findViewById(R.id.btn_call);

        if (user.phone.length()==0){
            btnCall.setVisibility(View.GONE);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle(user.username);
        Helper.loadAvatar(user.avatar,imvAvatar,ctlMemberDetail,this,R.drawable.ic_baseline_person_white_24);
        if (user.avatar.length()>0){
            imvAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MemberDetailActivity.this,ShowImageActivity.class);
                    intent.putExtra("image_url",user.avatar);
                    intent.putExtra("username",user.username);
                    intent.putExtra("update_time",user.avatarTime);
                    intent.putExtra("download",user.avatarDownload.booleanValue());
                    startActivity(intent);
                }
            });
        }
        Helper.setTextViewUI(tvUsername,user.username,"#FFFFFF","#000000",true);
        String emailText = "Email: "+user.email;
        Helper.setTextViewUI(tvEmail,emailText,"#FFFFFF","#000000",true);
        if (user.phone.length()>0){
            String phoneText = "Tel: "+user.phone;
            Helper.setTextViewUI(tvPhone,phoneText,"#FFFFFF","#000000",true);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tel = "tel:"+user.phone;
                    Uri number = Uri.parse(tel);
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                    try {
                        startActivity(Intent.createChooser(callIntent, "Call"));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MemberDetailActivity.this, "There are no calling clients installed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");;
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {user.email});
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MemberDetailActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
