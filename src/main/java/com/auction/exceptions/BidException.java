package com.auction.exceptions;

/**
 * Exception liên quan tới bid.
 */
public class BidException
    extends AuctionException {

  /**
   * Constructor bid exception.
   *
   * @param message nội dung lỗi
   */
  public BidException(
      String message) {

    super(message);
  }

  /**
   * Constructor bid exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public BidException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}