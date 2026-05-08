package com.auction.models.exception;

/**
 * Base exception cho toàn bộ hệ thống Auction.
 */
public class AuctionException extends Exception {

    public AuctionException(String message) {
        super(message);
    }

    public AuctionException(String message, Throwable cause) {
        super(message, cause);
    }
}