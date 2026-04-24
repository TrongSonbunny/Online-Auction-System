package com.auction.models.payment;

/**
 * Thanh toán qua ví Momo.
 */
public class MomoPayments implements PaymentStrategy {
  public void pay(double amount) {
    System.out.println("Pay by Momo: " + amount);
  }
}
