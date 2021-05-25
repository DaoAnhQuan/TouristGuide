package com.android.touristguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private AppCompatActivity activity;
    private List<Post> postList;
    public PostAdapter(AppCompatActivity activity, List<Post> postList){
        this.activity = activity;
        this.postList = postList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.post_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        CircleImageView imvAvatar = holder.imvAvatar;
        TextView tvUsername = holder.tvUsername;
        TextView tvTime = holder.tvTime;
        ImageView imvPhoto = holder.imvPhoto;
        TextView tvTitle = holder.tvTitle;
        TextView tvNoLike = holder.tvNoLike;
        TextView tvNoComment = holder.tvNoComment;
        TextView tvNoShare = holder.tvNoShare;
        View itemView = holder.itemView;
        Helper.loadAvatar(post.ownerAvatar,imvAvatar,itemView,activity,R.drawable.ic_baseline_person_white_24);
        tvUsername.setText(Helper.getBoldString(post.ownerName));
        tvTime.setText(post.time);
        Glide.with(activity).load(post.photo).into(imvPhoto);
        tvTitle.setText(Helper.getBoldString(post.title));
        tvNoLike.setText(String.valueOf(post.noLike));
        tvNoComment.setText(String.valueOf(post.noComment));
        tvNoShare.setText(String.valueOf(post.noShare));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imvAvatar;
        public TextView tvUsername;
        public TextView tvTime;
        public ImageView imvPhoto;
        public TextView tvTitle;
        public TextView tvNoLike;
        public TextView tvNoComment;
        public TextView tvNoShare;
        public View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
            imvPhoto = itemView.findViewById(R.id.imv_photo);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvNoLike = itemView.findViewById(R.id.tv_no_like);
            tvNoComment = itemView.findViewById(R.id.tv_no_comment);
            tvNoShare = itemView.findViewById(R.id.tv_no_share);
            this.itemView = itemView;
        }
    }
}
