package com.android.touristguide;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MembersActivity extends AppCompatActivity {
    private SkeletonScreen screen;
    private FirebaseFunctions mFunctions;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        MaterialToolbar toolbar = (MaterialToolbar) findViewById(R.id.top_app_bar_members);
        final RecyclerView rcvMembers = (RecyclerView) findViewById(R.id.rcv_members);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rcvMembers.setLayoutManager(linearLayoutManager);
        screen = Skeleton.bind(rcvMembers).show();
        final String groupType = getIntent().getStringExtra("group_type");
        final String groupID = getIntent().getStringExtra("group_id");
        if (groupType != null && groupType.equals("leader")){
            toolbar.inflateMenu(R.menu.add_member_menu);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mFunctions = Helper.initFirebaseFunctions();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.getReference("Groups/"+groupID+"/members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getMembers(mFunctions).addOnCompleteListener(new OnCompleteListener<List<User>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<User>> task) {
                        screen.hide();
                        if (task.isSuccessful()){
                            ListMembersAdapter membersAdapter = new ListMembersAdapter(MembersActivity.this,task.getResult(),groupType,null);
                            rcvMembers.setAdapter(membersAdapter);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MembersActivity.this,NewGroupActivity.class);
                intent.putExtra("add_member",true);
                startActivity(intent);
                return true;
            }
        });
    }

    public static Task<List<User>> getMembers(FirebaseFunctions mFunctions){
        return mFunctions.getHttpsCallable("getMembersInfo")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, List<User>>() {
                    @Override
                    public List<User> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String,Object> responses = (Map<String,Object>) task.getResult().getData();
                        List<User> listUsers = new ArrayList<>();
                        for (Map.Entry<String,Object> response : responses.entrySet() ){
                            Map<String,String> member = (Map<String,String>)response.getValue();
                            String username = member.get("username");
                            String avatar = member.get("avatar_url");
                            String time = member.get("avatar_time");
                            String avatarDownload = member.get("avatar_download");
                            String phone = member.get("phone");
                            String email = member.get("email");
                            String state = member.get("state");
                            String uid = member.get("uid");
                            User user = new User(uid,username,avatar,state,phone,email,time,avatarDownload);
                            listUsers.add(user);
                        }
                        return listUsers;
                    }
                });
    }
}
