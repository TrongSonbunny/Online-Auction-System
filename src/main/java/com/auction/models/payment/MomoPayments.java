package com.auction.models.payment;

/**
 * Thanh toán qua ví Momo.
 */
public class MomoPayments implements PaymentStrategy {

  @Override
  public void pay(double amount) {
    System.out.println("Pay by Momo: " + amount);
  }
}