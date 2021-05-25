package com.android.touristguide;

public class Notification {
    public String type;
    public static final String INVITATION_TYPE = "invitation";
    public static final String JOIN_REQUEST_TYPE= "join request";
    public static final String JOIN_REQUEST_RESPONSE_TYPE = "join request response";
    public static final String REMOVE_MEMBER_TYPE = "remove member";
    public String content;
    public String url;
    public String id;
    public String time;

    public Notification(String id, String type, String content, String url, String time) {
        this.type = type;
        this.content = content;
        this.url = url;
        this.id = id;
        this.time = time;
    }
}
