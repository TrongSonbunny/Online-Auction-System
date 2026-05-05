package com.auction.backend;

/**
 * Strategy cho các phương thức thanh toán.
 */
public interface PaymentStrategy {
  public void pay(double amount);
}