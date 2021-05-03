package com.android.touristguide;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListUserNewGroupAdapter extends RecyclerView.Adapter<ListUserNewGroupAdapter.ViewHolder> {

    private List<User> users;
    private Activity activity;
    private TextView tvSelected;
    public static int count = 0;

    public  ListUserNewGroupAdapter(Activity activity, List<User> users, TextView tvSelected){
        this.users = users;
        this.activity = activity;
        this.tvSelected = tvSelected;
    }

    @NonNull
    @Override
    public ListUserNewGroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.list_item_add_user,parent, false);
        return new ListUserNewGroupAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);
        TextView tvUsername = holder.tvUsername;
        CircleImageView imvAvatar = holder.imvAvatar;
        CheckBox cbSelected = holder.cbSelected;
        tvUsername.setText(user.username);

        Helper.loadAvatar(user.avatar,imvAvatar,holder.itemView,activity,R.drawable.ic_baseline_person_white_24);
        if (NewGroupActivity.selectedUsers.containsKey(user.uid)){
            cbSelected.setChecked(true);
        }else{
            cbSelected.setChecked(false);
        }
        cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    NewGroupActivity.selectedUsers.put(user.uid,user);
                    if (NewGroupActivity.sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                        NewGroupActivity.sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }else{
                    NewGroupActivity.selectedUsers.remove(user.uid);
                    if (NewGroupActivity.selectedUsers.isEmpty()){
                        NewGroupActivity.sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
                NewGroupActivity.selectedUsersAdapter.notifyDataSetChanged();
                String result = activity.getString(R.string.selected).toString()+" "+String.valueOf(NewGroupActivity.selectedUsers.size());
                tvSelected.setText(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imvAvatar;
        public TextView tvUsername;
        public CheckBox cbSelected;
        public View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imvAvatar = (CircleImageView) itemView.findViewById(R.id.imv_avatar);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            this.itemView = itemView;
            cbSelected = (CheckBox) itemView.findViewById(R.id.cb_selected);
        }
    }
}
