package com.auction.models.core;

public enum AuctionStatus {
    OPEN,
    RUNNING,
    FINISHED,
    PAID,
    CANCELED;

    public boolean canTransitionTo(AuctionStatus nextStatus) {
        switch (this) {
            case OPEN:
                return nextStatus == RUNNING || nextStatus == CANCELED;
            case RUNNING:
                return nextStatus == FINISHED || nextStatus == CANCELED;
            case FINISHED:
                return nextStatus == PAID || nextStatus == CANCELED;
            case PAID:
            case CANCELED:
                return false;
            default:
                return false;
        }
    }
}