package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;


import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private BadgeDrawable notificationBadge;
    private int currentID = R.id.navigation_group;
    private FirebaseDatabase mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        notificationBadge = navigation.getOrCreateBadge(R.id.navigation_notification);
        notificationBadge.setBadgeTextColor(Color.parseColor("#FFFFFF"));
        notificationBadge.setBackgroundColor(Color.parseColor("#FF0000"));
        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference numberOfNotificationRef = mDatabase.getReference("Users/"+user.getUid()+"/number_of_notifications");
        numberOfNotificationRef.addValueEventListener(numberOfNotificationEventListener);
        loadFragment(new MapFragment());
        Intent intent = new Intent(this,UpdateLocationService.class);
        startService(intent);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_group:
                    if (currentID != R.id.navigation_group){
                        currentID = R.id.navigation_group;
                        fragment = new MapFragment();
                        loadFragment(fragment);
                        return true;
                    }else{
                        return false;
                    }
                case R.id.navigation_post:
                    return true;
                case R.id.navigation_notification:
                    if (currentID != R.id.navigation_notification){
                        mDatabase.getReference("Users/"+user.getUid()+"/number_of_notifications").setValue(0);
                        currentID = R.id.navigation_notification;
                        fragment = new NotificationFragment();
                        loadFragment(fragment);
                        return true;
                    }else{
                        return false;
                    }
                case R.id.navigation_account:
                    if (currentID != R.id.navigation_account){
                        currentID = R.id.navigation_account;
                        fragment = new AccountFragment();
                        loadFragment(fragment);
                        return true;
                    }else{
                        return false;
                    }
            }
            return false;
        }
    };

    ValueEventListener numberOfNotificationEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Long numNotLong = (Long) snapshot.getValue();
            int numNot = numNotLong.intValue();
            Log.d("MainActivityTAG",String.valueOf(numNot));
            if (notificationBadge != null){
                if (numNot == 0){
                    notificationBadge.setVisible(false);
                    notificationBadge.clearNumber();
                }else{
                    notificationBadge.setVisible(true);
                    notificationBadge.setNumber(numNot);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.commit();
    }

}