package com.auction.models.core;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuctionManager {

    private static final AuctionManager INSTANCE = new AuctionManager();

    private final List<Auction> auctions = new CopyOnWriteArrayList<>();

    private AuctionManager() {}

    public static AuctionManager getInstance() {
        return INSTANCE;
    }

    public void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException("Phiên đấu giá không được null.");
        }
        auctions.add(auction);
    }

    public void removeAuction(String auctionId) {
        auctions.removeIf(a -> a.getAuctionId().equals(auctionId));
    }

    public Auction findById(String auctionId) {
        return auctions.stream()
                .filter(a -> a.getAuctionId().equals(auctionId))
                .findFirst()
                .orElse(null);
    }

    public List<Auction> getAuctions() {
        return Collections.unmodifiableList(auctions);
    }
}