package com.android.touristguide;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;

public class ShowImageActivity extends AppCompatActivity {

    private ZoomageView zm;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        zm = findViewById(R.id.zm);
        Bundle imagePathBundle = getIntent().getExtras();
        String imagePath = imagePathBundle.getString("image_url");
        Glide.with(this).load(Uri.parse(imagePath)).into(zm);
    }
}
