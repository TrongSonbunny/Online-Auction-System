package com.auction.exceptions;

/**
 * Exception khi bid không hợp lệ.
 */
public class InvalidBidException
    extends BidException {

  /**
   * Constructor invalid bid exception.
   *
   * @param message nội dung lỗi
   */
  public InvalidBidException(
      String message) {

    super(message);
  }

  /**
   * Constructor invalid bid exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public InvalidBidException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}