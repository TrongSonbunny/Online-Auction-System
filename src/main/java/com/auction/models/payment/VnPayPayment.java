package com.auction.models.payment;

import com.auction.exceptions.PaymentException;

/**
 * Payment strategy cho VNPay.
 */
public class VnPayPayment implements PaymentStrategy {

  private final String accountEmail;

  /**
   * Constructor VNPay payment.
   *
   * @param accountEmail email tài khoản VNPay
   */
  public VnPayPayment(String accountEmail) {

    validateEmail(accountEmail);

    this.accountEmail = accountEmail;
  }

  @Override
  public boolean pay(double amount) {

    validateAmount(amount);

    System.out.println(
        "Thanh toán "
            + amount
            + " bằng VNPay.");

    return true;
  }

  @Override
  public boolean refund(double amount) {

    validateAmount(amount);

    System.out.println(
        "Hoàn tiền "
            + amount
            + " qua VNPay.");

    return true;
  }

  @Override
  public String getPaymentMethodName() {
    return "VNPay Payment";
  }

  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new PaymentException(
          "Số tiền phải lớn hơn 0.");
    }
  }

  private void validateEmail(String email) {

    if (email == null
        || email.isBlank()
        || !email.contains("@")) {

      throw new PaymentException(
          "Email không hợp lệ.");
    }
  }

  public String getAccountEmail() {
    return accountEmail;
  }

  @Override
  public String toString() {

    return "VnPayPayment{"
        + "accountEmail='"
        + accountEmail
        + '\''
        + '}';
  }
}