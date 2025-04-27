package com.example.upnext;

public class Community {
    private int id;
    private String name;
    private byte[] image;
    private String description;

    public Community(int id, String name, byte[] image) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = "";
    }

    public Community(int id, String name, byte[] image, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
