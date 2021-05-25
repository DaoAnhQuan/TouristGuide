package com.android.touristguide;

public class Member {
    public String uid;
    public String url;
    public Double latitude;
    public Double longitude;
    public Boolean sos;

    public Member(){

    }

    public Member(String uid, String url, Double latitude, Double longitude, Boolean sos) {
        this.uid = uid;
        this.url = url;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sos = sos;
    }
}
