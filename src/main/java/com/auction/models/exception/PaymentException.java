package com.auction.models.exception;

/**
 * Exception liên quan đến thanh toán.
 */
public class PaymentException extends AuctionException {

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}