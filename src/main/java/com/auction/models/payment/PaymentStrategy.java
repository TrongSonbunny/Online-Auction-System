package com.auction.models.payment;

/**
 * Strategy xử lý thanh toán.
 */
public interface PaymentStrategy {

  /**
   * Thực hiện thanh toán.
   *
   * @param amount số tiền cần thanh toán
   * @return true nếu thanh toán thành công
   */
  boolean pay(double amount);

  /**
   * Hoàn tiền.
   *
   * @param amount số tiền hoàn
   * @return true nếu hoàn tiền thành công
   */
  boolean refund(double amount);

  /**
   * Lấy tên phương thức thanh toán.
   *
   * @return tên payment method
   */
  String getPaymentMethodName();
}