package com.android.touristguide;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
    private Context currentContext;
    public NavigationItemSelectedListener(Context context){
        this.currentContext = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.mn_logout:
                FirebaseAuth.getInstance().signOut();
                Intent toLoginActivity = new Intent(currentContext, LoginActivity.class);
                currentContext.startActivity(toLoginActivity);
                Helper.finishActivityFromContext(currentContext);
            case R.id.mn_account:
                Intent toAccountActivity = new Intent(currentContext, AccountActivity.class);
                currentContext.startActivity(toAccountActivity);
        }
        return false;
    }


}
