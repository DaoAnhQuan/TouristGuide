package com.android.touristguide;

import android.net.Uri;

import java.io.Serializable;

public class Post implements Serializable {
    public String postID,ownerName, time,title,description,topic;
    public String ownerAvatar, photo;
    public Integer noLike,noComment,noShare;
    public Boolean isLiked, isReported,isOwner;
    public Double latitude,longitude;

    public Post(){}

    public Post(String postID,String ownerName, String time, String title, String ownerAvatar, String photo, Integer noLike, Integer noComment, Integer noShare) {
        this.ownerName = ownerName;
        this.postID = postID;
        this.time = time;
        this.title = title;
        this.ownerAvatar = ownerAvatar;
        this.photo = photo;
        this.noLike = noLike;
        this.noComment = noComment;
        this.noShare = noShare;
    }

    public Post(String postID, String ownerName, String time, String title, String description,
                String ownerAvatar, String photo, Integer noLike, Integer noComment,
                Integer noShare, Boolean isLiked, Boolean isReported, Double latitude, Double longitude,
                Boolean isOwner, String topic) {
        this.postID = postID;
        this.ownerName = ownerName;
        this.time = time;
        this.title = title;
        this.description = description;
        this.ownerAvatar = ownerAvatar;
        this.photo = photo;
        this.noLike = noLike;
        this.noComment = noComment;
        this.noShare = noShare;
        this.isLiked = isLiked;
        this.isReported = isReported;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOwner = isOwner;
        this.topic = topic;
    }
}
