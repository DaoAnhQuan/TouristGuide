package com.android.touristguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListMembersAdapter extends RecyclerView.Adapter<ListMembersAdapter.ViewHolder>{
    private Activity activity;
    private List<User> userList;
    private String groupType;
    private FirebaseFunctions mFunctions;
    private FirebaseUser currentUser;
    private AlertDialog changeLeaderDialog;

    public ListMembersAdapter(Activity activity, List<User> userList, String groupType, AlertDialog changeLeaderDialog) {
        this.activity = activity;
        this.userList = userList;
        this.groupType = groupType;
        this.changeLeaderDialog = changeLeaderDialog;
        mFunctions = Helper.initFirebaseFunctions();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.member_item,parent,false);
        return new ListMembersAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        CircleImageView imvAvatar = holder.imvAvatar;
        TextView tvUsername = holder.tvUsername;
        TextView tvState = holder.tvState;
        final MaterialButton btnDelete = holder.btnDelete;
        View itemView = holder.itemView;
        LinearLayout ltInfo = holder.ltInfo;
        final User user = userList.get(position);
        if (changeLeaderDialog==null){
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnDelete.setEnabled(false);
                    deleteMember(position,btnDelete);
                }
            });
        }else{
            btnDelete.setIcon(ContextCompat.getDrawable(activity,R.drawable.ic_baseline_check_24));
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Map<String,String> data = new HashMap<>();
                    data.put("memberID",user.uid);
                    mFunctions.getHttpsCallable("changeLeader")
                            .call(data);
                    changeLeaderDialog.cancel();
                }
            });
        }

        if (groupType.equals("member")){
            btnDelete.setVisibility(View.INVISIBLE);
        }
        if (!user.uid.equals(currentUser.getUid())){
            imvAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startMemberDetailActivity(user);
                }
            });
            ltInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startMemberDetailActivity(user);
                }
            });
        }else{
            btnDelete.setVisibility(View.INVISIBLE);
        }
        Helper.loadAvatar(user.avatar,imvAvatar,itemView,activity,R.drawable.ic_baseline_person_white_24);
        Helper.setTextViewUI(tvUsername,user.username,"#FFFFFF","#000000",true);
        tvState.setText(user.state);
    }

    private void startMemberDetailActivity(User user){
        Intent intent = new Intent(activity,MemberDetailActivity.class);
        intent.putExtra("User",user);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void deleteMember(int position, final Button btnDelete){
        final User user = userList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.remove_member_confirm)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        btnDelete.setEnabled(true);
                        dialogInterface.cancel();
                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, int i) {
                        Map<String,String> data = new HashMap<>();
                        data.put("uid",user.uid);
                        mFunctions.getHttpsCallable("leaveGroup")
                                .call(data)
                                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                                        if(task.isSuccessful()){
                                            dialogInterface.cancel();
                                        }
                                    }
                                });
                    }
                });
        builder.create().show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View itemView;
        public CircleImageView imvAvatar;
        public TextView tvUsername;
        public TextView tvState;
        public MaterialButton btnDelete;
        public LinearLayout ltInfo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.imvAvatar = itemView.findViewById(R.id.imv_avatar);
            this.tvUsername = itemView.findViewById(R.id.tv_username);
            this.tvState = itemView.findViewById(R.id.tv_state);
            this.btnDelete = itemView.findViewById(R.id.btn_delete_member);
            this.ltInfo = itemView.findViewById(R.id.lt_info);
        }
    }
}
