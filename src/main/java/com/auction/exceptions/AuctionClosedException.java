package com.auction.exceptions;

/**
 * Exception khi thao tác trên phiên đã đóng.
 */
public class AuctionClosedException extends AuctionException {

  public AuctionClosedException(String message) {
    super(message);
  }
}