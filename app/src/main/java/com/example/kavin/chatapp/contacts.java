package com.example.kavin.chatapp;

public class contacts
{
    public String name,status,image;

    public contacts()
    {

    }

    public contacts(String name, String status, String image) {
        this.name = name;
        this.status=status;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
