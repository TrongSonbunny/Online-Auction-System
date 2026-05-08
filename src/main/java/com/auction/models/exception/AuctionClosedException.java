package com.auction.models.exception;

/**
 * Exception khi thao tác trên phiên đã đóng.
 */
public class AuctionClosedException extends AuctionException {

    public AuctionClosedException(String message) {
        super(message);
    }
}