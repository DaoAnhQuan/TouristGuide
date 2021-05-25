package com.android.touristguide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends  RecyclerView.Adapter<ChatAdapter.ViewHolder>{
    private Activity activity;
    private List<Message> listMessages;
    private FirebaseUser currentUser;
    private String TAG = "ChatAdapterTAG";
    private String bingMapApiKey;
    public static final int LOCATION_RESULT_CODE = 0;

    public ChatAdapter(Activity activity, List<Message> listMessages, FirebaseUser currentUser,String bingMapApiKey){
        this.activity = activity;
        this.listMessages = listMessages;
        this.currentUser = currentUser;
        this.bingMapApiKey = bingMapApiKey;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(activity).inflate(R.layout.chat_notification_item,parent,false);
        if (viewType == Message.TEXT_MESSAGE_LEFT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_text_left_item,parent,false);
        }
        if (viewType == Message.TEXT_MESSAGE_RIGHT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_text_right_item,parent,false);
        }
        if (viewType == Message.PHOTO_MESSAGE_LEFT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_photo_left_item,parent,false);
        }
        if (viewType == Message.PHOTO_MESSAGE_RIGHT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_photo_right_item,parent,false);
        }
        if (viewType == Message.LOCATION_MESSAGE_RIGHT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_location_right_item,parent,false);
        }
        if (viewType == Message.LOCATION_MESSAGE_LEFT){
            itemView = LayoutInflater.from(activity).inflate(R.layout.chat_location_left_item,parent,false);
        }
        return new ChatAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final TextView tvTextMessage = holder.tvTextMessage;
        CircleImageView imvAvatar = holder.imvAvatar;
        final TextView tvMessageTime = holder.tvMessageTime;
        TextView tvSender = holder.tvSender;
        ImageView imvPhoto = holder.imvPhoto;
        final Message message = listMessages.get(position);
        View itemView = holder.itemView;
        int viewType = getItemViewType(position);
        if (viewType==Message.NOTIFICATION_MESSAGE){
            tvTextMessage.setText(message.content);
        }
        if (tvMessageTime != null){
            tvMessageTime.setText(message.time);
            tvMessageTime.setVisibility(View.GONE);
            if (tvTextMessage != null){
                tvTextMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tvMessageTime.getVisibility() == View.GONE){
                            tvMessageTime.setVisibility(View.VISIBLE);
                        }else{
                            tvMessageTime.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
        if (imvAvatar != null){
            Helper.loadAvatar(message.fromUrl, imvAvatar,itemView,activity,R.drawable.ic_baseline_person_white_24);
        }
        if (tvSender != null){
            tvSender.setText(message.fromName);
        }
        if (viewType == Message.TEXT_MESSAGE_LEFT){
            tvTextMessage.setText(message.content);
        }
        if (viewType == Message.TEXT_MESSAGE_RIGHT){
            tvTextMessage.setText(message.content);
        }
        if (imvPhoto != null && (viewType == Message.PHOTO_MESSAGE_LEFT || viewType == Message.PHOTO_MESSAGE_RIGHT)){
            Glide.with(activity).load(message.content).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    tvTextMessage.setVisibility(View.GONE);
                    return false;
                }
            }).into(imvPhoto);
            imvPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity,ShowImageActivity.class);
                    intent.putExtra("image_url",message.content);
                    intent.putExtra("download",true);
                    intent.putExtra("username",message.fromName);
                    intent.putExtra("update_time",message.time);
                    activity.startActivity(intent);
                }
            });
        }
        if (viewType == Message.LOCATION_MESSAGE_LEFT || viewType==Message.LOCATION_MESSAGE_RIGHT){
            try {
                JSONObject jsonObject = new JSONObject(message.content);
                final String description = jsonObject.get("message").toString();
                tvTextMessage.setText(description);
                final double latitude = (double) jsonObject.getDouble("latitude");
                final double longitude = (double) jsonObject.getDouble("longitude");
                String mapLink = getStaticMapLink(latitude,longitude);
                Glide.with(activity).load(mapLink).into(imvPhoto);
                imvPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("fromName",message.fromName);
                        intent.putExtra("fromUrl",message.fromUrl);
                        intent.putExtra("time",message.time);
                        intent.putExtra("longitude",longitude);
                        intent.putExtra("latitude",latitude);
                        intent.putExtra("message",description);
                        activity.setResult(LOCATION_RESULT_CODE,intent);
                        activity.finish();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String getStaticMapLink(double latitude, double longitude){
        String mapLink = "http://dev.virtualearth.net/REST/v1/Imagery/Map/Road/"
                +String.valueOf(latitude)+","
                +String.valueOf(longitude)
                +"/16?mapSize=300,200&pp="
                +String.valueOf(latitude)+","
                +String.valueOf(longitude)+";66&mapLayer=Basemap,Buildings&key="
                +bingMapApiKey;
        return mapLink;
    }

    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = listMessages.get(position);
        if (message.type.equals("notification")){
            return Message.NOTIFICATION_MESSAGE;
        }
        if (message.type.equals("text")){
            if (message.from.equals(currentUser.getUid())){
                return  Message.TEXT_MESSAGE_RIGHT;
            }else{
                return Message.TEXT_MESSAGE_LEFT;
            }
        }
        if (message.type.equals("photo")){
            if (message.from.equals(currentUser.getUid())){
                return  Message.PHOTO_MESSAGE_RIGHT;
            }else{
                return Message.PHOTO_MESSAGE_LEFT;
            }
        }
        if (message.type.equals("location")){
            if (message.from.equals(currentUser.getUid())){
                return  Message.LOCATION_MESSAGE_RIGHT;
            }else{
                return Message.LOCATION_MESSAGE_LEFT;
            }
        }
        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTextMessage;
        public CircleImageView imvAvatar;
        public TextView tvMessageTime;
        public TextView tvSender;
        public ImageView imvPhoto;
        public View itemView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvTextMessage = itemView.findViewById(R.id.tv_text_message);
            this.tvMessageTime = itemView.findViewById(R.id.tv_message_time);
            this.tvSender = itemView.findViewById(R.id.tv_sender);
            this.imvPhoto = itemView.findViewById(R.id.imv_photo_message);
            this.imvAvatar = itemView.findViewById(R.id.imv_avatar);
            this.itemView = itemView;
        }
    }
}
