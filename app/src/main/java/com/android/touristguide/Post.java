package com.android.touristguide;

import android.net.Uri;

public class Post {
    public String postID,ownerName, time,title;
    public String ownerAvatar, photo;
    public Long noLike,noComment,noShare;

    public Post(String postID,String ownerName, String time, String title, String ownerAvatar, String photo, Long noLike, Long noComment, Long noShare) {
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
}
