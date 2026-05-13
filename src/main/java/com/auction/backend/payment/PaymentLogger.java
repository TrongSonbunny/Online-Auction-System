package com.auction.backend.payment;

import java.time.LocalDateTime;

/**
 * Ghi log kết quả payment ra console với timestamp.
 *
 * <p>Ba loại log: thành công ({@link #logPaymentSuccess}),
 * hoàn tiền ({@link #logRefundSuccess}), thất bại ({@link #logPaymentFailure}).
 */
public class PaymentLogger {

  /**
   * Ghi log payment thành công.
   *
   * @param paymentMethod phương thức thanh toán
   * @param amount số tiền
   */
  public void logPaymentSuccess(
      String paymentMethod,
      double amount) {

    System.out.println(
        "[PAYMENT SUCCESS] "
            + "Method="
            + paymentMethod
            + ", Amount="
            + amount
            + ", Time="
            + LocalDateTime.now());
  }

  /**
   * Ghi log refund thành công.
   *
   * @param paymentMethod phương thức thanh toán
   * @param amount số tiền hoàn
   */
  public void logRefundSuccess(
      String paymentMethod,
      double amount) {

    System.out.println(
        "[REFUND SUCCESS] "
            + "Method="
            + paymentMethod
            + ", Amount="
            + amount
            + ", Time="
            + LocalDateTime.now());
  }

  /**
   * Ghi log payment thất bại.
   *
   * @param paymentMethod phương thức thanh toán
   * @param reason lý do thất bại
   */
  public void logPaymentFailure(
      String paymentMethod,
      String reason) {

    System.out.println(
        "[PAYMENT FAILED] "
            + "Method="
            + paymentMethod
            + ", Reason="
            + reason
            + ", Time="
            + LocalDateTime.now());
  }
}