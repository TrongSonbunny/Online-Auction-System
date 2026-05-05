package com.auction.models.exception;

/**
 * Exception cho lỗi database.
 */
public class DatabaseException extends AuctionException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}