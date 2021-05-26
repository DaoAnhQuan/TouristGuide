package com.android.touristguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

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
    private final String TAG = "MainActivityTAG";
    private BadgeDrawable groupBadge;
    private DatabaseReference numberOfUnreadMessage;
    private DatabaseReference numberOfNotificationRef;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                viewPager.setCurrentItem(item.getOrder());
                return true;
            }
        });
        notificationBadge = navigation.getOrCreateBadge(R.id.navigation_notification);
        notificationBadge.setBadgeTextColor(Color.parseColor("#FFFFFF"));
        notificationBadge.setBackgroundColor(Color.parseColor("#FF0000"));
        groupBadge = navigation.getOrCreateBadge(R.id.navigation_group);
        groupBadge.setBadgeTextColor(Color.parseColor("#FFFFFF"));
        groupBadge.setBackgroundColor(Color.parseColor("#FF0000"));
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new MainAdapter(this));
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPager.setUserInputEnabled(position != 0);
                navigation.getMenu().getItem(position).setChecked(true);
            }
        });
        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        numberOfNotificationRef = mDatabase.getReference("Users/"+user.getUid()+"/number_of_notifications");
        numberOfNotificationRef.addValueEventListener(numberOfNotificationEventListener);
        numberOfUnreadMessage = mDatabase.getReference("Users/"+user.getUid()+"/unread_messages");
        numberOfUnreadMessage.addValueEventListener(unreadMessageEventListener);
        Intent intent = new Intent(this,UpdateLocationService.class);
        startService(intent);
        startSOSService();
    }

    private void startSOSService(){
        Intent sosService = new Intent(this,SOSService.class);
        startService(sosService);
    }

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

    ValueEventListener unreadMessageEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Long numNotLong = (Long) snapshot.getValue();
            int numMessage = numNotLong.intValue();
            if (notificationBadge != null){
                if (numMessage == 0){
                    groupBadge.setVisible(false);
                    groupBadge.clearNumber();
                }else{
                    groupBadge.setVisible(true);
                    groupBadge.setNumber(numMessage);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

}