package com.auction.models.exception;

/**
 * Exception khi không đủ quyền.
 */
public class AuthorizationException extends AuctionException {

    public AuthorizationException(String message) {
        super(message);
    }
}