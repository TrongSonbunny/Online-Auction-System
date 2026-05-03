package com.auction.models;

public class Seller extends User {
    private double rating;

    public Seller(String username, String password) {
        super(username, password);
        this.rating = 5.0;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public void printInfo() {
        System.out.println("[seller] " + getUsername() + " - Rating: " + rating + "sao");
    }
}
