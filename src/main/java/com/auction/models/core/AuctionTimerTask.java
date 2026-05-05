package com.auction.models.core;

public class AuctionTimerTask implements Runnable {

    private final Auction targetAuction;
    private final long durationInSeconds;

    public AuctionTimerTask(Auction targetAuction, long durationInSeconds) {
        this.targetAuction = targetAuction;
        this.durationInSeconds = durationInSeconds;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(durationInSeconds * 1000L);
            targetAuction.endAuction();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}