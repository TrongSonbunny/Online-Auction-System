package com.auction.models;

public class Art extends Item {
    private String artist;
    private int creationYear;

    public Art(String name, double startingPrice, String artist, int creationYear) {
        super(name, startingPrice); // Chỉ truyền name và startingPrice lên Item
        this.artist = artist;
        this.creationYear = creationYear;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    public void setCreationYear(int creationYear) {
        this.creationYear = creationYear;
    }

    @Override
    public String getItemDetails() {
        return "Nghệ thuật: " + name + " (Tác giả: " + artist + ", Năm: " + creationYear + ") - Giá khởi điểm: $"
                + startingPrice;
    }
}