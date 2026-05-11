package com.auction.exceptions;

/**
 * Exception cơ sở cho toàn bộ hệ thống auction.
 */
public class AuctionException
    extends RuntimeException {

  /**
   * Constructor exception.
   *
   * @param message nội dung lỗi
   */
  public AuctionException(
      String message) {

    super(message);
  }

  /**
   * Constructor exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public AuctionException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}