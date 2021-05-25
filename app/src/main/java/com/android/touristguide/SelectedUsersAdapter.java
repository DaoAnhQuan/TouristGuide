package com.android.touristguide;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.ViewHolder>{
    @NonNull
    private Activity activity;
    private TextView tvSelected;

    public SelectedUsersAdapter(Activity activity, TextView tvSelected){
        this.tvSelected = tvSelected;
        this.activity=activity;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.remove_user_item,parent,false);
        return new SelectedUsersAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        CircleImageView imvAvatar = holder.imvAvatar;
        View itemView = holder.itemView;
        String avatarUrl = "";
        int i = 0;
        User user = null;
        for (Map.Entry<String,User> userEntry:NewGroupActivity.selectedUsers.entrySet()){
            if (i==position){
                user = userEntry.getValue();
                avatarUrl = user.avatar;
                break;
            }
            i++;
        }
        Helper.loadAvatar(avatarUrl,imvAvatar,itemView,activity,R.drawable.ic_baseline_person_white_24);
        final User finalUser = user;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewGroupActivity.selectedUsers.remove(finalUser.uid);
                notifyItemRemoved(position);
                if (NewGroupActivity.selectedUsers.isEmpty()){
                    NewGroupActivity.sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                NewGroupActivity.adapter.notifyDataSetChanged();
                String result = activity.getString(R.string.selected).toString()+" "+String.valueOf(NewGroupActivity.selectedUsers.size());
                tvSelected.setText(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return NewGroupActivity.selectedUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imvAvatar;
        public View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.imvAvatar = itemView.findViewById(R.id.imv_avatar);
        }
    }
}
