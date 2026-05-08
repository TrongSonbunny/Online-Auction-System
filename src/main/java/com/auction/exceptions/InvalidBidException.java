package com.auction.exceptions;

/**
 * Exception khi giá thầu không hợp lệ.
 */
public class InvalidBidException extends AuctionException {

  public InvalidBidException(String message) {
    super(message);
  }
}