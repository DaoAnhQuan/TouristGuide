package com.android.touristguide;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()){
            Intent toMainActivity = new Intent(this, MainActivity.class);
            startActivity(toMainActivity);
        }
        else{
            Intent toLoginActivity = new Intent(this, LoginActivity.class);
            startActivity(toLoginActivity);
        }
        finish();
    }
}
