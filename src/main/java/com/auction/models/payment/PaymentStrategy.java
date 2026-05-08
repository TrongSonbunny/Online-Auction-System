package com.auction.models.payment;

/**
 * Strategy cho các phương thức thanh toán.
 */
public interface PaymentStrategy {

  /**
     * Thực hiện thanh toán.
     *
     * @param amount Số tiền cần thanh toán
     */
  void pay(double amount);
}