package com.example.whatsappchat.Chat;

import java.util.ArrayList;

public class MessageObject {
    String messsageId,senderId,message;
    ArrayList<String> mediaUrlList;

    public MessageObject(String messsageId, String senderId,String message,ArrayList<String> mediaUrlList){

        this.messsageId = messsageId;
        this.senderId = senderId;
        this.message = message;
        this.mediaUrlList = mediaUrlList;
    }

    public String getMesssageId() {
        return messsageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getMediaUrlList() { return mediaUrlList; }
}
