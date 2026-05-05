package com.auction.models.item;

import com.auction.models.user.Seller;
import java.time.LocalDateTime;

/**
 * Lớp trừu tượng đại diện cho sản phẩm đấu giá.
 */
public abstract class AuctionItem {

    protected String id;
    protected String name;
    protected String description;
    protected double startPrice;
    protected double currentPrice;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;
    protected Seller seller;

    /**
     * Khởi tạo sản phẩm đấu giá.
     */
    public AuctionItem(
            String id,
            String name,
            String description,
            double startPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Seller seller) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.seller = seller;
    }

    public Seller getSeller() {
        return seller;
    }

    public String getName() {
        return name;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public String getId() {
        return id;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setCurrentPrice(double price) {
        this.currentPrice = price;
    }

    /**
     * In thong tin san pham.
     */
    public void printInfo() {
        System.out.println("Sản phẩm: " + name
                + " | Giá khởi điểm: " + startPrice
                + " | Giá hiện tại: " + currentPrice
                + " | Người bán: " + seller.getName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{id='" + id + '\''
                + ", name='" + name + '\''
                + ", currentPrice=" + currentPrice + '}';
    }
}