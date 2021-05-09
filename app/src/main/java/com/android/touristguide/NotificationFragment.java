package com.android.touristguide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aglibs.loading.skeleton.layout.SkeletonRecyclerView;

public class NotificationFragment extends Fragment {
    private FirebaseDatabase db;
    private FirebaseUser user;
    private FirebaseFunctions mFunctions;
    private RecyclerView rcvNotifications;
    private SkeletonScreen skeletonScreen;
    private TextView tvNoNotification;

    public NotificationFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mFunctions = Helper.initFirebaseFunctions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_notification, container, false);
        rcvNotifications = (RecyclerView) view.findViewById(R.id.rcv_notifications);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rcvNotifications.setLayoutManager(linearLayoutManager);
        skeletonScreen = Skeleton.bind(rcvNotifications).show();
        tvNoNotification = (TextView) view.findViewById(R.id.tv_no_notification);
        DatabaseReference notificationsRef = db.getReference("Users/" + user.getUid() + "/notifications");
        notificationsRef.addValueEventListener(notificationEventListener);
        return view;
    }

    ValueEventListener notificationEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            getNotifications().addOnCompleteListener(new OnCompleteListener<List<Notification>>() {
                @Override
                public void onComplete(@NonNull Task<List<Notification>> task) {
                    List<Notification> listNotifications = task.getResult();
                    skeletonScreen.hide();
                    if (listNotifications.size()==0){
                        tvNoNotification.setVisibility(View.VISIBLE);
                    }else{
                        tvNoNotification.setVisibility(View.GONE);
                    }
                    NotificationAdapter adapter = new NotificationAdapter(getActivity(),listNotifications);
                    rcvNotifications.setAdapter(adapter);
                }
            });
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    private Task<List<Notification>> getNotifications(){
        return  mFunctions.getHttpsCallable("getNotifications")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, List<Notification>>() {
                    @Override
                    public List<Notification> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        List<Notification> listNotifications = new ArrayList<>();
                        Map<String,Object> notifications = (Map<String,Object>)task.getResult().getData();
                        for (Map.Entry<String,Object> notification:notifications.entrySet()){
                            Map<String,String> map = (HashMap<String,String>)notification.getValue();
                            Notification nof = new Notification(map.get("id"),map.get("type"),map.get("content"),map.get("url"),map.get("time"));
                            listNotifications.add(nof);
                        }
                        return listNotifications;
                    }
                });
    }
}
