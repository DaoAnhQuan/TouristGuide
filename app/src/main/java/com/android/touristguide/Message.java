package com.android.touristguide;

public class Message {
    public String content, type, from, fromName, fromUrl, time;
    public static final int NOTIFICATION_MESSAGE = 0;
    public static final int TEXT_MESSAGE_LEFT = 1;
    public static final int PHOTO_MESSAGE_LEFT = 2;
    public static final int TEXT_MESSAGE_RIGHT = 3;
    public static final int PHOTO_MESSAGE_RIGHT = 4;
    public static final int LOCATION_MESSAGE_RIGHT=5;
    public static final int LOCATION_MESSAGE_LEFT=6;
    public Message(){}

    public Message(String content, String type, String from, String fromName, String fromUrl, String time) {
        this.content = content;
        this.type = type;
        this.from = from;
        this.fromName = fromName;
        this.fromUrl = fromUrl;
        this.time = time;
    }
}
