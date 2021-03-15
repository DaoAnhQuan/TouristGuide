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

import java.util.List;

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
        User user = users.get(position);
        TextView tvUsername = holder.tvUsername;
        TextView tvEmail = holder.tvEmail;
        CircleImageView imvAvatar = holder.imvAvatar;
        CheckBox cbSelected = holder.cbSelected;
        tvUsername.setText(user.username);
        tvEmail.setText(user.email);
        Helper.loadAvatar(user.avatar,imvAvatar,holder.itemView,activity,R.drawable.ic_baseline_person_white_24);
        cbSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    count++;
                }else{
                    count--;
                }
                String result = activity.getString(R.string.selected).toString()+" "+String.valueOf(count);
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
        public TextView tvEmail;
        public CheckBox cbSelected;
        public View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imvAvatar = (CircleImageView) itemView.findViewById(R.id.imv_avatar);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_username);
            tvEmail = (TextView) itemView.findViewById(R.id.tv_email);
            this.itemView = itemView;
            cbSelected = (CheckBox) itemView.findViewById(R.id.cb_selected);
        }
    }
}
