package com.auction.exceptions;

/** Exception khi giá thầu không hợp lệ. */
public class InvalidBidException extends Exception {

  public InvalidBidException(String message) {
    super(message);
  }
}