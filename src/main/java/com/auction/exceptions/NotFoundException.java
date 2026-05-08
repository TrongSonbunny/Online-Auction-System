package com.auction.exceptions;

/**
 * Exception khi không tìm thấy dữ liệu.
 */
public class NotFoundException extends AuctionException {

  public NotFoundException(String message) {
    super(message);
  }
}