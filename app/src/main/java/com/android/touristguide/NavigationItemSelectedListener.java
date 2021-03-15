package com.android.touristguide;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
    private Context currentContext;
    private DrawerLayout drawerLayout;
    public NavigationItemSelectedListener(Context context, DrawerLayout drawerLayout){
        this.currentContext = context;
        this.drawerLayout = drawerLayout;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(Gravity.LEFT, false);
        switch (id){
            case R.id.mn_logout:
                FirebaseAuth.getInstance().signOut();
                Intent toLoginActivity = new Intent(currentContext, LoginActivity.class);
                currentContext.startActivity(toLoginActivity);
                Helper.finishActivityFromContext(currentContext);
                break;
            case R.id.mn_account:
                Intent toAccountActivity = new Intent(currentContext, AccountActivity.class);
                currentContext.startActivity(toAccountActivity);
                break;
            case R.id.mn_group:
                Intent toGroupActivity = new Intent(currentContext, GroupActivity.class);
                currentContext.startActivity(toGroupActivity);
                break;
        }
        return false;
    }


}
