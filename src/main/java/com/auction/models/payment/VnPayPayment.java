package com.auction.models.payment;

import com.auction.exceptions.PaymentException;

/**
 * Payment strategy thanh toán qua cổng VNPay.
 *
 * <p>Yêu cầu email tài khoản hợp lệ (chứa '@').
 * {@code pay()} và {@code refund()} hiện tại ghi log ra console.
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

  /**
   * Thực hiện thanh toán qua VNPay.
   *
   * @param amount số tiền thanh toán (phải > 0)
   * @return true nếu thành công
   */
  @Override
  public boolean pay(double amount) {

    validateAmount(amount);

    System.out.println(
        "Thanh toán "
            + amount
            + " bằng VNPay.");

    return true;
  }

  /**
   * Hoàn tiền qua VNPay.
   *
   * @param amount số tiền hoàn (phải > 0)
   * @return true nếu thành công
   */
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

  /**
   * Validate số tiền phải lớn hơn 0.
   *
   * @param amount số tiền
   */
  private void validateAmount(double amount) {

    if (amount <= 0) {
      throw new PaymentException(
          "Số tiền phải lớn hơn 0.");
    }
  }

  /**
   * Validate email không null, không blank và phải chứa ký tự '@'.
   *
   * @param email email tài khoản VNPay
   */
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