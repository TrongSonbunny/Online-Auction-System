package com.auction.models.observer;

import com.auction.models.core.AuctionStatus;

/**
 * Đối tượng chứa thông tin sự kiện được gửi đến các Observer.
 * Khi có bid mới hoặc trạng thái phiên thay đổi,
 * một AuctionEvent sẽ được tạo và broadcast.
 */
public class AuctionEvent {

    /**
     * Các loại sự kiện có thể xảy ra trong phiên đấu giá.
     */
    public enum EventType {
        NEW_BID,
        AUCTION_STARTED,
        AUCTION_FINISHED,
        AUCTION_CANCELED,
        AUCTION_PAID
    }

    private final String auctionId;
    private final EventType eventType;
    private final double currentHighestBid;
    private final String currentLeader;
    private final AuctionStatus newStatus;
    private final long timestamp;

    /**
     * Constructor tạo một sự kiện đấu giá.
     */
    public AuctionEvent(
            String auctionId,
            EventType eventType,
            double currentHighestBid,
            String currentLeader,
            AuctionStatus newStatus) {

        this.auctionId = auctionId;
        this.eventType = eventType;
        this.currentHighestBid = currentHighestBid;
        this.currentLeader = currentLeader;
        this.newStatus = newStatus;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAuctionId() {
        return auctionId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getCurrentLeader() {
        return currentLeader;
    }

    public AuctionStatus getNewStatus() {
        return newStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AuctionEvent{"
                + "auctionId='" + auctionId + '\''
                + ", eventType=" + eventType
                + ", currentHighestBid=" + currentHighestBid
                + ", currentLeader='" + currentLeader + '\''
                + ", newStatus=" + newStatus
                + ", timestamp=" + timestamp
                + '}';
    }
}