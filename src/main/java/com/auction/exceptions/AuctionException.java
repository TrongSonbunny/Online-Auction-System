package com.auction.exceptions;

/**
 * Exception cơ sở (unchecked) cho toàn bộ hệ thống auction.
 *
 * <p>Hierarchy: {@code AuctionException} → {@code AuctionClosedException},
 * {@code BidException} (→ {@code InvalidBidException}),
 * {@code PaymentException}, {@code UnauthorizedException}.
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