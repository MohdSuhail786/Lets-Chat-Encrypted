package com.mohammadsuhail.letschatencrypted;

public class Message {
    private String message;
    private String time;
    private String number;
    private String status;

    public Message() {
    }

    public Message(String message, String time, String status, String number) {
        this.message = message;
        this.time = time;
        this.number = number;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
