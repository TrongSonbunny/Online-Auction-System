package com.auction.models;

public class Vehicle extends Item {
    private String brand;
    private int mileage;

    public Vehicle(String name, double startingPrice, String brand, int mileage) {
        super(name, startingPrice);
        this.brand = brand;
        this.mileage = mileage;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    @Override
    public String getItemDetails() {
        return "Xe cộ: " + name + " (Hãng: " + brand + ", ODO: " + mileage + "km) - Giá khởi điểm: $" + startingPrice;
    }
}