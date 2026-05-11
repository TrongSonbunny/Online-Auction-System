package com.auction.backend.payment;

/**
 * Validate payment business rules.
 */
public class PaymentValidator {

  /**
   * Validate số tiền thanh toán.
   *
   * @param amount số tiền
   */
  public void validatePaymentAmount(
      double amount) {

    if (amount <= 0) {
      throw new IllegalArgumentException(
          "Số tiền thanh toán phải lớn hơn 0.");
    }
  }

  /**
   * Validate số dư tài khoản.
   *
   * @param balance số dư
   * @param amount số tiền cần thanh toán
   */
  public void validateBalance(
      double balance,
      double amount) {

    validatePaymentAmount(amount);

    if (balance < amount) {
      throw new IllegalArgumentException(
          "Số dư không đủ để thanh toán.");
    }
  }
}