package com.auction.models;

public interface AuctionObserver {
    void onBidPlaced(Auction auction);

    void onAuctionClosed(Auction auction);
}