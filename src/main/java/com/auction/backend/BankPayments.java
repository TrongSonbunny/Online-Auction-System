package com.auction.backend;

/**
 * Thanh toán qua ngân hàng.
 */
public class BankPayments implements PaymentStrategy {
  @Override
  public void pay(double amount) {
    System.out.println("Pay by Bank: " + amount);
  }
}