package com.auction.models.user;

import com.auction.models.core.Auction;
import com.auction.models.core.AuctionStatus;
import com.auction.models.bid.BidTransaction;
import com.auction.models.observer.AuctionEvent;
import com.auction.models.payment.PaymentStrategy;

/**
 * Lớp đại diện cho người mua (Bidder).
 */
public class Bidder extends User {

    private int totalWins;
    private double totalSpent;
    private final PaymentStrategy paymentStrategy;

    public Bidder(
            String userId,
            String name,
            String email,
            String passwordHash,
            PaymentStrategy paymentStrategy) {

        super(userId, name, email, passwordHash);
        this.paymentStrategy = paymentStrategy;
        this.totalWins = 0;
        this.totalSpent = 0.0;
    }

    public BidTransaction placeBid(Auction auction, double bidAmount) {
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            throw new IllegalStateException(
                    "Phiên đấu giá không ở trạng thái RUNNING");
        }

        if (bidAmount <= auction.getCurrentHighestBid()) {
            throw new IllegalArgumentException("Giá đặt không hợp lệ");
        }

        return auction.placeBid(this, bidAmount);
    }

    @Override
    public void update(AuctionEvent event) {
        switch (event.getEventType()) {
            case NEW_BID:
                if (!event.getCurrentLeader().equals(getUserId())) {
                    System.out.println("[BIDDER " + getName()
                            + "] Bạn bị outbid!");
                }
                break;

            case AUCTION_FINISHED:
                if (event.getCurrentLeader().equals(getUserId())) {
                    System.out.println("[BIDDER " + getName()
                            + "] Bạn thắng!");
                }
                break;

            default:
                break;
        }
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void incrementTotalWins() {
        totalWins++;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void addToTotalSpent(double amount) {
        totalSpent += amount;
    }

    public PaymentStrategy getPaymentStrategy() {
        return paymentStrategy;
    }
}