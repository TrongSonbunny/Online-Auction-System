package com.auction.models.payment;

/**
 * Strategy cho các phương thức thanh toán.
 */
public interface PaymentStrategy {
  public void pay(double amount);
}