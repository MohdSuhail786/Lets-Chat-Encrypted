package com.mohammadsuhail.letschatencrypted;

public class Contact {
    private String name;
    private String number;
    private String image = null;
    private int unread;

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public Contact(String name, String number, String imageurl, int unread) {
        this.name = name;
        this.number = number;
        this.image = imageurl;
        this.unread = unread;
    }

    public Contact(){}

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String imageurl) {
        this.image = imageurl;
    }
}
