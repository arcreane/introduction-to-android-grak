package com.example.headsup.models;

// Deck class with getters and setters of deck attributes (id, title, desc, image and binId)
public class Deck {
    private int id;
    private String title;
    private String desc;
    private int imageResId; // refers to the Resources (R) id of the image

    private String binId; // refers to the json binId of cards for the deck



    public Deck(int id, String title, String desc, int imageResId, String binId) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.imageResId = imageResId;
        this.binId = binId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() {
        return desc;
    }
    public int getImageResId() { return imageResId; }

    public String getBinId() { return binId; }
}