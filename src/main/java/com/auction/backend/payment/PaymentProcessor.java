package com.auction.backend.payment;

import com.auction.exceptions.PaymentException;
import com.auction.models.payment.PaymentStrategy;
import java.util.Objects;

/**
 * Xử lý payment trong hệ thống.
 */
public class PaymentProcessor {

  /**
   * Thực hiện thanh toán.
   *
   * @param paymentStrategy strategy thanh toán
   * @param amount số tiền cần thanh toán
   * @return true nếu thanh toán thành công
   */
  public boolean processPayment(
      PaymentStrategy paymentStrategy,
      double amount) {

    validatePaymentStrategy(paymentStrategy);
    validateAmount(amount);

    return paymentStrategy.pay(amount);
  }

  /**
   * Hoàn tiền.
   *
   * @param paymentStrategy strategy thanh toán
   * @param amount số tiền hoàn
   * @return true nếu hoàn tiền thành công
   */
  public boolean processRefund(
      PaymentStrategy paymentStrategy,
      double amount) {

    validatePaymentStrategy(paymentStrategy);
    validateAmount(amount);

    return paymentStrategy.refund(amount);
  }

  private void validatePaymentStrategy(
      PaymentStrategy paymentStrategy) {

    Objects.requireNonNull(
        paymentStrategy,
        "Payment strategy không được null.");
  }

  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new PaymentException(
          "Số tiền phải lớn hơn 0.");
    }
  }
}