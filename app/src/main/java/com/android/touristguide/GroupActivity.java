package com.android.touristguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;

public class GroupActivity extends AppCompatActivity {
    private MaterialToolbar toolbarGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        toolbarGroup = (MaterialToolbar) findViewById(R.id.top_app_bar_group);

        toolbarGroup.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        toolbarGroup.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.create_group){
                    Intent toNewGroupActivity = new Intent(GroupActivity.this, NewGroupActivity.class);
                    startActivity(toNewGroupActivity);
                }
                return false;
            }
        });
    }
}