package com.android.touristguide;

import android.net.Uri;

public class User {
    public String uid, username,email,phone,avatar;

    public User(){

    }

    public User(String uid, String username, String email, String phone, String avatar) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
    }
}
