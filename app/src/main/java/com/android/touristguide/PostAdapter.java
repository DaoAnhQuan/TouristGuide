package com.android.touristguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private Activity activity;
    private List<Post> postList;
    private AlertDialog loadingDialog;
    private FirebaseFunctions mFunctions;
    private PostFragment postFragment;
    private final String TAG = "PostAdapterTAG";
    public PostAdapter(Activity activity, List<Post> postList, PostFragment postFragment){
        this.activity = activity;
        this.postList = postList;
        loadingDialog = Helper.createLoadingDialog(activity);
        mFunctions = Helper.initFirebaseFunctions();
        this.postFragment = postFragment;
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
        imvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.show();
                getPostDetail(post.postID).addOnCompleteListener(new OnCompleteListener<Post>() {
                    @Override
                    public void onComplete(@NonNull Task<Post> task) {
                        loadingDialog.cancel();
                        if (task.isSuccessful()){
                            Post postSelected = task.getResult();
                            if (postSelected == null){
                                Toast.makeText(activity,R.string.post_deleted,Toast.LENGTH_LONG).show();
                            }else{
                                Intent intent = new Intent(activity,PostDetailActivity.class);
                                intent.putExtra("Post",postSelected);
                                postFragment.startActivityForResult(intent,20);
                            }
                        }else{
                            Log.d(TAG,task.getException().toString());
                        }
                    }
                });
            }
        });
    }

    private Task<Post> getPostDetail(String postID){
        Map<String,String> data = new HashMap<>();
        data.put("postID",postID);
        return mFunctions.getHttpsCallable("getPostDetail").call(data)
                .continueWith(new Continuation<HttpsCallableResult, Post>() {
                    @Override
                    public Post then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String,Object> result = (HashMap<String,Object>)task.getResult().getData();
                        try {
                            Post post = new Post(result.get("postID").toString(),result.get("ownerName").toString(),
                                    result.get("time").toString(),result.get("title").toString(),
                                    result.get("description").toString(),result.get("ownerAvatar").toString(),
                                    result.get("photo").toString(),0,0,0,(boolean)result.get("isLiked"),
                                    (boolean)result.get("isReported"),(double)result.get("latitude"),
                                    (double)result.get("longitude"), (boolean)result.get("isOwner"),
                                    result.get("topic").toString());
                            return post;
                        }catch (NullPointerException e){
                            return null;
                        }
                    }
                });
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
