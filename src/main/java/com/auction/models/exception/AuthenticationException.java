package com.auction.models.exception;

/**
 * Exception khi xác thực thất bại.
 */
public class AuthenticationException extends AuctionException {

    public AuthenticationException(String message) {
        super(message);
    }
}