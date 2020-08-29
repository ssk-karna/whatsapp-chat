package com.example.whatsappchat.Chat;

public class MessageObject {
    String messsageId,senderId,message;

    public MessageObject(String messsageId, String senderId,String message){

        this.messsageId = messsageId;
        this.senderId = senderId;
        this.message = message;

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
}
