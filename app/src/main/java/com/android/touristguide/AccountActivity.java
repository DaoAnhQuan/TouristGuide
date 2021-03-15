package com.android.touristguide;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;

public class AccountActivity extends AppCompatActivity {
    private MaterialToolbar toolbarAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        toolbarAccount = (MaterialToolbar) findViewById(R.id.top_app_bar_account);
        toolbarAccount.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}