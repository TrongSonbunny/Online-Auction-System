package com.auction.exceptions;

/** Exception khi phiên đấu giá kết thúc. */
public class AuctionClosedException extends Exception {

  public AuctionClosedException(String message) {
    super(message);
  }
}