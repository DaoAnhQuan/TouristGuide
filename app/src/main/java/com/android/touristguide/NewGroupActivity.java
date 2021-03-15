package com.android.touristguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

public class NewGroupActivity extends AppCompatActivity {
    private MaterialToolbar toolbarNewGroup;
    private RecyclerView listUser;
    private TextView tvSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        toolbarNewGroup = (MaterialToolbar) findViewById(R.id.top_app_bar_new_group);
        listUser = (RecyclerView) findViewById(R.id.rcv_list_user);
        tvSelected = (TextView) findViewById(R.id.tv_selected);

        toolbarNewGroup.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ListUserNewGroupAdapter adapter = new ListUserNewGroupAdapter(this,Helper.createListUsersForTest(),tvSelected);
        listUser.setAdapter(adapter);
        listUser.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListUserNewGroupAdapter.count = 0;
    }
}