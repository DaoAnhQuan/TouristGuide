package com.android.touristguide;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentAdapter.ViewHolder>{
    private Activity activity;
    private List<Comment> comments;
    private final int TEXT_TYPE = 0;
    private final int PHOTO_TYPE = 1;
    public PostCommentAdapter(Activity activity,List<Comment> comments){
        this.activity = activity;
        this.comments = comments;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.comment_text_item,parent,false);
        if (viewType == PHOTO_TYPE){
            itemView = LayoutInflater.from(activity).inflate(R.layout.comment_photo_item,parent,false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        CircleImageView imvAvatar = holder.imvAvatar;
        TextView tvUsername=holder.tvUsername;
        TextView tvComment=holder.tvComment;
        ImageView imvPhoto=holder.imvPhoto;
        TextView tvTime=holder.tvTime;
        View itemView = holder.itemView;
        Helper.loadAvatar(comment.fromUrl,imvAvatar,itemView,activity,R.drawable.ic_baseline_person_white_24);
        tvUsername.setText(comment.fromName);
        if (tvComment != null){
            tvComment.setText(comment.content);
        }
        if (imvPhoto != null){
            Glide.with(activity).load(comment.content).into(imvPhoto);
        }
        tvTime.setText(comment.time);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public int getItemViewType(int position) {
        Comment comment = comments.get(position);
        if (comment.type.equals("text")){
            return TEXT_TYPE;
        }else{
            return PHOTO_TYPE;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imvAvatar;
        public TextView tvUsername;
        public TextView tvComment;
        public ImageView imvPhoto;
        public TextView tvTime;
        private View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.imv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvComment = itemView.findViewById(R.id.tv_comment);
            imvPhoto = itemView.findViewById(R.id.imv_photo);
            tvTime = itemView.findViewById(R.id.tv_time);
            this.itemView = itemView;
        }
    }
}
