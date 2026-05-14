package com.auction.backend.payment;

import java.util.logging.Logger;

/**
 * Ghi log kết quả payment qua java.util.logging với timestamp.
 *
 * <p>Ba loại log: thành công ({@link #logPaymentSuccess}),
 * hoàn tiền ({@link #logRefundSuccess}), thất bại ({@link #logPaymentFailure}).
 */
public class PaymentLogger {

  private static final Logger logger =
      Logger.getLogger(
          PaymentLogger.class.getName());

  /**
   * Ghi log payment thành công.
   *
   * @param paymentMethod phương thức thanh toán
   * @param amount số tiền
   */
  public void logPaymentSuccess(
      String paymentMethod,
      double amount) {

    logger.info(
        "[PAYMENT SUCCESS] "
            + "Method="
            + paymentMethod
            + ", Amount="
            + amount);
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

    logger.info(
        "[REFUND SUCCESS] "
            + "Method="
            + paymentMethod
            + ", Amount="
            + amount);
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

    logger.warning(
        "[PAYMENT FAILED] "
            + "Method="
            + paymentMethod
            + ", Reason="
            + reason);
  }
}