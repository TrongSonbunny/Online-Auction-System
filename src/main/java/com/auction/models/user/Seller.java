package com.auction.models.user;

import com.auction.models.core.Auction;
import com.auction.models.core.AuctionManager;
import com.auction.models.item.AuctionItem;
import com.auction.models.item.ItemFactory;
import com.auction.models.observer.AuctionEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp đại diện cho người bán (Seller).
 */
public class Seller extends User {

    private final List<String> auctionIds;
    private final List<AuctionItem> items;
    private double totalRevenue;

    public Seller(String userId, String name, String email, String passwordHash) {
        super(userId, name, email, passwordHash);
        this.auctionIds = new ArrayList<>();
        this.items = new ArrayList<>();
        this.totalRevenue = 0.0;
    }

    public AuctionItem createItem(
            String type,
            String id,
            String name,
            String description,
            double startPrice,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        AuctionItem item = ItemFactory.createItem(
                type, id, name, description, startPrice, startTime, endTime, this);

        items.add(item);
        return item;
    }

    public Auction createAuction(
            String auctionId,
            AuctionItem item,
            double startingPrice,
            LocalDateTime endTime) {

        Auction auction = new Auction(
                auctionId, item, getUserId(), startingPrice, endTime);

        AuctionManager.getInstance().addAuction(auction);
        auctionIds.add(auctionId);
        auction.registerObserver(this);

        return auction;
    }

    @Override
    public void update(AuctionEvent event) {
        switch (event.getEventType()) {
            case AUCTION_PAID:
                totalRevenue += event.getCurrentHighestBid();
                break;
            default:
                break;
        }
    }

    @Override
    public String getRole() {
        return "SELLER";
    }

    public List<String> getAuctionIds() {
        return Collections.unmodifiableList(auctionIds);
    }

    public List<AuctionItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}