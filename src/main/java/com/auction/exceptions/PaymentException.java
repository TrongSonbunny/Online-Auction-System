package com.auction.exceptions;

/**
 * Exception ném khi thanh toán thất bại: amount không hợp lệ (âm/bằng 0),
 * số dư không đủ, hoặc thông tin phương thức thanh toán không hợp lệ.
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