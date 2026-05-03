package com.auction.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class Auction extends Entity {
    private Item item;
    private Seller seller;
    private List<BidTransaction> bidHistory;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.bidHistory = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "OPEN";
    }

    public void addBid(BidTransaction bid) {
        this.bidHistory.add(bid);
        this.item.setCurrentPrice(bid.getBidAmount());
    }

    public Item getItem() {
        return item;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<BidTransaction> getBidHistory() {
        return bidHistory;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}