package com.android.touristguide;

import android.Manifest;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {

    private ZoomableImageView zm;
    private AppBarLayout appbar;
    private TextView tvCreator;
    private TextView tvUpdateTime;
    private MenuItem downloadMenu;
    private Toolbar toolbar;
    private boolean download;
    private FirebaseStorage storage;
    private AlertDialog.Builder buider;
    private StorageReference storageRef;
    private DownloadManager downloadManager;
    private String TAG = "ImageShow";
    private String imagePath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        zm = (ZoomableImageView) findViewById(R.id.zm);
        appbar = (AppBarLayout)findViewById(R.id.app_bar);
        tvCreator = (TextView) findViewById(R.id.tv_creator);
        tvUpdateTime = (TextView) findViewById(R.id.tv_time);
        toolbar = (Toolbar) findViewById(R.id.top_app_bar_account);
        storage = FirebaseStorage.getInstance();
        buider = new AlertDialog.Builder(this);
        downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);

        setSupportActionBar(toolbar);
        setTitle("");

        zm.setAppbar(appbar);
        Bundle imageBundle = getIntent().getExtras();
        imagePath = imageBundle.getString("image_url");
        download = imageBundle.getBoolean("download");
        String creator = imageBundle.getString("username");
        String updateTime = imageBundle.getString("update_time");
        final CircularProgressIndicator progressIndicator = findViewById(R.id.prg_image);

        storageRef = storage.getReferenceFromUrl(imagePath);
        Glide.with(this)
                .load(Uri.parse(imagePath))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressIndicator.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressIndicator.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(zm);
        tvCreator.setText(creator);
        tvUpdateTime.setText(updateTime);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_download){
                    requestPermission();
                }
                return false;
            }
        });
    }

    private void requestPermission(){
        if (Build.VERSION.SDK_INT>=23){
               if( ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                   download();
               }
            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                download();
            }
            else{
                buider.setMessage("You need to give the app permission to save the photo.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                AlertDialog dialog = buider.create();
                dialog.show();
            }
        }
    }

    private void download(){
        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String type = storageMetadata.getContentType().split("/")[1];
                String filename = storageMetadata.getName()+"."+type;
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imagePath));
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                downloadManager.enqueue(request);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu,menu);
        downloadMenu = menu.findItem(R.id.menu_download);
        if (download){
            downloadMenu.setVisible(true);
        }else{
            downloadMenu.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

}
