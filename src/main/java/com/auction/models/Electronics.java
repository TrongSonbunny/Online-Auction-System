package com.auction.models;

public class Electronics extends Item {
    private int warrantyMonths;

    public Electronics(String name, double startingPrice, int warrantyMonths) {
        super(name, startingPrice);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public String getItemDetails() {
        return "Điện tử: " + name + " - Giá khởi điểm: $" + startingPrice + " - Bảo hành: " + warrantyMonths + " tháng";
    }
}
