package com.android.touristguide;

import java.io.Serializable;

public class User implements Serializable{
    public String uid, username,avatar,state,phone,email,avatarTime;
    public Boolean avatarDownload;

    public User(){

    }

    public User(String uid, String username, String avatar) {
        this.uid = uid;
        this.username = username;
        this.avatar = avatar;
    }

    public User(String uid, String username, String avatar, String state, String phone, String email, String avatarTime, String avatarDownload) {
        this.uid = uid;
        this.username = username;
        this.avatar = avatar;
        this.state = state;
        this.phone = phone;
        this.email = email;
        this.avatarTime = avatarTime;
        if (avatarDownload.equals("true")){
            this.avatarDownload = true;
        }else{
            this.avatarDownload = false;
        }
    }
}
