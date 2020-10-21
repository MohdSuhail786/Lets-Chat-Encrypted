package com.mohammadsuhail.letschatencrypted;

public class Contact {
    private String name;
    private String number;
    private String imageurl = null;

    public Contact(String name, String number,String imageurl) {
        this.name = name;
        this.number = number;
        this.imageurl = imageurl;
    }

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

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
