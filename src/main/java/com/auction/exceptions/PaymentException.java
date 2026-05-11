package com.auction.exceptions;

/**
 * Exception liên quan tới payment.
 */
public class PaymentException
    extends AuctionException {

  /**
   * Constructor payment exception.
   *
   * @param message nội dung lỗi
   */
  public PaymentException(
      String message) {

    super(message);
  }

  /**
   * Constructor payment exception.
   *
   * @param message nội dung lỗi
   * @param cause nguyên nhân lỗi
   */
  public PaymentException(
      String message,
      Throwable cause) {

    super(message, cause);
  }
}