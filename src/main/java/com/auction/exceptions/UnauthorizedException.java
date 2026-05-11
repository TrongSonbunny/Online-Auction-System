package com.auction.exceptions;

/**
 * Exception khi user không có quyền.
 */
public class UnauthorizedException
    extends AuctionException {

  /**
   * Constructor unauthorized exception.
   *
   * @param message nội dung lỗi
   */
  public UnauthorizedException(
      String message) {

    super(message);
  }

  /**
   * Constructor unauthorized exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public UnauthorizedException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}