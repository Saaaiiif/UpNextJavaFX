package com.example.upnext;

import java.util.ArrayList;
import java.util.List;

public class Community {
    private int id;
    private String name;
    private byte[] image;
    private String description;
    private int status;
    private String social;
    private List<String> keywords;

    public Community(int id, String name, byte[] image) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = "";
        this.status = 2; // Default status
        this.social = ""; // Default empty social link
        this.keywords = new ArrayList<>();
    }

    public Community(int id, String name, byte[] image, String description) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.status = 2; // Default status
        this.social = ""; // Default empty social link
        this.keywords = new ArrayList<>();
    }

    public Community(int id, String name, byte[] image, String description, int status) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.status = status;
        this.social = ""; // Default empty social link
        this.keywords = new ArrayList<>();
    }

    public Community(int id, String name, byte[] image, String description, int status, String social) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.status = status;
        this.social = social;
        this.keywords = new ArrayList<>();
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void addKeyword(String keyword) {
        if (this.keywords == null) {
            this.keywords = new ArrayList<>();
        }
        this.keywords.add(keyword);
    }
}
