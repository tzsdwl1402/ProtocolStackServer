package com.yijiagou.pojo;

public class LeaveMessage {
    private String sender;
    private String receiver;
    private String message;
    private long addTime;

    public LeaveMessage(){

    }

    public LeaveMessage(String sender, String receiver, String message, long addTime) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.addTime = addTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }
}
