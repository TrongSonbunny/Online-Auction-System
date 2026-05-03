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

    private transient List<AuctionObserver> observers;

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        super();
        this.item = item;
        this.seller = seller;
        this.bidHistory = new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "OPEN";
        this.observers = new ArrayList<>();
    }

    public synchronized void startAuction() {
        if ("OPEN".equals(this.status)) {
            this.status = "RUNNING";
            System.out.println("Phiên đấu giá đã bắt đầu! Trạng thái: RUNNING");
        }
    }

    public synchronized void endAuction() {
        if ("RUNNING".equals(this.status)) {
            this.status = "FINISHED";
            System.out.println("Phiên đấu giá kết thúc! Trạng thái: FINISHED");

            if (observers != null) {
                for (AuctionObserver obs : observers)
                    obs.onAuctionClosed(this);
            }
        }
    }

    public synchronized void payAuction() {
        if ("FINISHED".equals(this.status) && getHighestBidder() != null) {
            this.status = "PAID";
            System.out.println("Giao dịch thành công. Trạng thái: PAID");
        }
    }

    public synchronized void cancelAuction() {
        if ("FINISHED".equals(this.status)) {
            this.status = "CANCELED";
            System.out.println("Giao dịch bị hủy. Trạng thái: CANCELED");
        }
    }

    public synchronized Bidder getHighestBidder() {
        if (bidHistory.isEmpty())
            return null;
        return bidHistory.get(bidHistory.size() - 1).getBidder();
    }

    public synchronized boolean placeBid(BidTransaction newBid) {
        if (!"RUNNING".equals(this.status)) {
            System.out.println("[Từ chối] Chỉ có thể đặt giá khi phiên đấu giá đang mở (RUNNING).");
            return false;
        }

        double currentPrice = this.item.getCurrentPrice();
        if (newBid.getBidAmount() <= currentPrice) {
            System.out.println("[Từ chối] Giá đặt (" + newBid.getBidAmount() + ") phải lớn hơn " + currentPrice);
            return false;
        }

        Bidder currentBidder = newBid.getBidder();
        if (currentBidder.getBalance() < newBid.getBidAmount()) {
            System.out.println("[Từ chối] " + currentBidder.getUsername() + " không đủ số dư!");
            return false;
        }

        Bidder previousHighest = getHighestBidder();
        if (previousHighest != null) {
            previousHighest.setBalance(previousHighest.getBalance() + currentPrice);
        }

        currentBidder.setBalance(currentBidder.getBalance() - newBid.getBidAmount());
        this.bidHistory.add(newBid);
        this.item.setCurrentPrice(newBid.getBidAmount());

        System.out.println("[Thành công] " + currentBidder.getUsername() + " đặt $" + newBid.getBidAmount());

        notifyObserversBidPlaced();
        return true;
    }

    public void addObserver(AuctionObserver observer) {
        if (observers == null)
            observers = new ArrayList<>();
        if (!observers.contains(observer))
            observers.add(observer);
    }

    private void notifyObserversBidPlaced() {
        if (observers != null) {
            for (AuctionObserver observer : observers)
                observer.onBidPlaced(this);
        }
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
}