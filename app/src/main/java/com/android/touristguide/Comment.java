package com.android.touristguide;

public class Comment {
    public String fromName,fromUrl,content,id,type,time;

    public Comment(){}

    public Comment(String fromName, String fromUrl, String content, String id, String type, String time) {
        this.fromName = fromName;
        this.fromUrl = fromUrl;
        this.content = content;
        this.id = id;
        this.type = type;
        this.time = time;
    }
}
