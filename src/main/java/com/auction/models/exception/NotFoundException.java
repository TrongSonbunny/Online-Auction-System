package com.auction.models.exception;

/**
 * Exception khi không tìm thấy dữ liệu.
 */
public class NotFoundException extends AuctionException {

    public NotFoundException(String message) {
        super(message);
    }
}