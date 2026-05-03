package com.auction.models;

public abstract class Item extends Entity {
    protected String name;
    protected double startingPrice;
    protected double currentPrice;

    public Item(String name, double startingPrice) {
        super();
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public abstract String getItemDetails();
}
