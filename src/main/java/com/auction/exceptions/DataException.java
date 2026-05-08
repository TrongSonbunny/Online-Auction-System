package com.auction.exceptions;

/**
 * Exception cho lỗi database.
 */
public class DataException extends AuctionException {

  public DataException(String message) {
    super(message);
  }

  public DataException(String message, Throwable cause) {
    super(message, cause);
  }
}