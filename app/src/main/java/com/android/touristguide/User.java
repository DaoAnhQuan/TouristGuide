package com.android.touristguide;

import java.io.Serializable;

public class User{
    public String uid, username,avatar;

    public User(){

    }

    public User(String uid, String username, String avatar) {
        this.uid = uid;
        this.username = username;
        this.avatar = avatar;
    }
}
