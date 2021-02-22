package com.android.touristguide;

import android.net.Uri;

public class User {
    public String username, email, phone, avatar;

    public User(){

    }

    public User(String username, String email, String phone, String avatar) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.avatar = avatar;
    }
}
