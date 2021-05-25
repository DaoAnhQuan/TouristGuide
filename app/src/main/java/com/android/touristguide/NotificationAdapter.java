package com.android.touristguide;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Activity activity;
    private List<Notification> listNotifications;
    private FirebaseFunctions mFunctions;
    public NotificationAdapter(Activity activity, List<Notification> listNotifications){
        this.activity = activity;
        this.listNotifications = listNotifications;
        mFunctions = Helper.initFirebaseFunctions();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.notification_item,parent,false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CircleImageView imvAvatar = holder.imvAvatar;
        TextView tvContent = holder.tvContent;
        final Button btnAccept = holder.btnAccept;
        final Button btnDecline = holder.btnDecline;
        TextView tvNotificationTime = holder.tvNotificationTime;
        final Notification notification = listNotifications.get(position);
        tvNotificationTime.setText(notification.time);
        Helper.setHtmlToTextView(tvContent,notification.content);
        if (notification.type.equals(Notification.INVITATION_TYPE)){
            Helper.loadAvatar(notification.url,imvAvatar,holder.itemView,activity,R.drawable.ic_baseline_person_white_24);
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableButtons(btnAccept,btnDecline);
                    responseInvitation("accept",notification.id)
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()){
                                String result = task.getResult();
                                if (result.equals("failed")){
                                    Toast.makeText(activity,R.string.accept_invitation_failed,Toast.LENGTH_LONG).show();
                                }else{
                                    NewGroupActivity.showTurnOnLocationSharing(activity,mFunctions,false);
                                }
                            }
                        }
                    });
                }
            });
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableButtons(btnAccept,btnDecline);
                    responseInvitation("decline",notification.id);
                }
            });
        }
        if (notification.type.equals(Notification.JOIN_REQUEST_TYPE)){
            Helper.loadAvatar(notification.url,imvAvatar,holder.itemView,activity,R.drawable.ic_baseline_person_white_24);
            btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableButtons(btnAccept,btnDecline);
                    responseInvitation("decline",notification.id);
                }
            });
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disableButtons(btnAccept,btnDecline);
                    responseInvitation("accept",notification.id)
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    String result = task.getResult();
                                    Toast.makeText(activity,result,Toast.LENGTH_LONG).show();
                                }
                            });
                }
            });
        }
        if (notification.type.equals(Notification.JOIN_REQUEST_RESPONSE_TYPE)){
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
            imvAvatar.setVisibility(View.INVISIBLE);
        }
        if (notification.type.equals(Notification.REMOVE_MEMBER_TYPE)){
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
            Helper.loadAvatar(notification.url,imvAvatar,holder.itemView,activity,R.drawable.ic_baseline_person_white_24);
        }
    }

    private void disableButtons(Button btnAccept, Button btnDecline){
        btnAccept.setEnabled(false);
        btnDecline.setEnabled(false);
    }
    private Task<String> responseInvitation(String action, String notificationID){
        Map<String,String> data = new HashMap<>();
        data.put("action",action);
        data.put("notificationID",notificationID);
        return mFunctions.getHttpsCallable("notificationProcess")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return task.getResult().getData().toString();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return listNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imvAvatar;
        public TextView tvContent;
        public Button btnAccept;
        public Button btnDecline;
        public View itemView;
        public TextView tvNotificationTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
            tvNotificationTime = itemView.findViewById(R.id.tv_notification_time);
            this.itemView = itemView;
        }
    }
}
