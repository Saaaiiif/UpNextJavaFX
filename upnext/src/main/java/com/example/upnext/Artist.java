package com.example.upnext;

public class Artist {
    private int id;
    private String name;
    private byte[] image;
    private int communityId;

    public Artist(int id, String name, byte[] image, int communityId) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.communityId = communityId;
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

    public int getCommunityId() {
        return communityId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setCommunityId(int communityId) {
        this.communityId = communityId;
    }
}